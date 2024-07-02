package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.integration.GithubRepositoryDTO;
import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.model.Integration;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.ApplicationRepository;
import com.quashbugs.quash.repo.IntegrationRepository;
import com.quashbugs.quash.repo.ReportRepository;
import com.quashbugs.quash.repo.TeamMemberRepository;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.quashbugs.quash.constants.Constants.GITHUB_API_BASE_URL;

@Service
public class GithubIntegrationService {

    @Value("${spring.github.client_id}")
    private String githubClientId;

    @Value("${spring.github.client_secret}")
    private String githubClientSecret;

    @Value("${spring.github.redirect_url}")
    private String githubRedirectUrl;

    private final TeamMemberRepository teamMemberRepository;

    private final IntegrationRepository integrationRepository;

    private final ReportRepository reportRepository;

    private final ApplicationRepository applicationRepository;

    private final IntegrationService integrationService;

    @Autowired
    public GithubIntegrationService(TeamMemberRepository teamMemberRepository,
                                    IntegrationRepository integrationRepository,
                                    ReportRepository reportRepository,
                                    ApplicationRepository applicationRepository,
                                    IntegrationService integrationService) {
        this.teamMemberRepository = teamMemberRepository;
        this.integrationRepository = integrationRepository;
        this.reportRepository = reportRepository;
        this.applicationRepository = applicationRepository;
        this.integrationService = integrationService;
    }

    public String createGithubOAuthURL() {
        return String.format("https://github.com/login/oauth/authorize?client_id=%s&scope=repo,read:org,write:org,read:user,read:project&allow_signup=true",
                githubClientId);
    }

    public List<GithubRepositoryDTO> listRepositories(User user) {
        try {
            Organisation organisation = getOrganisationByUser(user);
            Integration integration = getIntegration(organisation, "GITHUB");
            String accessToken = (String) integration.getSettings().get("integrationAccessToken");

            HttpHeaders headers = createHeaders(accessToken);
            URI uri = createURI(GITHUB_API_BASE_URL + "/user/repos?type=all&sort=full_name&direction=asc");

            ResponseEntity<GithubRepositoryDTO[]> responseEntity = sendRequest(uri, headers, GithubRepositoryDTO[].class);

            return Optional.ofNullable(responseEntity.getBody())
                    .map(Arrays::asList)
                    .orElseThrow(() -> new HttpClientErrorException(responseEntity.getStatusCode(),
                            "GitHub API request failed with status code: " + responseEntity.getStatusCodeValue()));
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access token expired or invalid. Please re-authenticate with GitHub.");
            } else {
                throw new RuntimeException("Error listing GitHub repositories: " + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error listing GitHub repositories: " + e.getMessage(), e);
        }
    }

    public JSONObject exportGithubIssues(User user, IssuesRequestBodyDTO requestBody) throws Exception {
        try {
            Organisation organisation = getOrganisationByUser(user);
            Integration integration = getIntegration(organisation, "GITHUB");
            String accessToken = (String) integration.getSettings().get("integrationAccessToken");
            List<GithubRepositoryDTO> repositories = listRepositories(user);

            HttpHeaders headers = createHeaders(accessToken);
            JSONObject responseMap = new JSONObject();
            JSONArray requestJSON = prepareGithubData(requestBody);

            for (Object requestJSONItemObj : requestJSON) {
                if (!(requestJSONItemObj instanceof JSONObject requestJSONItem)) continue;

                String title = (String) requestJSONItem.getOrDefault("title", "");
                String body = (String) requestJSONItem.getOrDefault("body", "");
                String repoName = (String) requestJSONItem.getOrDefault("repoName", "");

                GithubRepositoryDTO repository = findRepositoryByName(repositories, repoName);
                String ownerName = repository.getFullName().split("/")[0];

                JSONObject requestBodyMap = new JSONObject();
                requestBodyMap.put("title", title);
                requestBodyMap.put("body", body);

                HttpEntity<Object> requestEntity = new HttpEntity<>(requestBodyMap, headers);
                ResponseEntity<JSONObject> responseEntity = sendRequest(
                        new URI(GITHUB_API_BASE_URL + "/repos/" + ownerName + "/" + repoName + "/issues"),
                        requestEntity,
                        JSONObject.class
                );

                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                    JSONObject responseBody = responseEntity.getBody();
                    responseMap.put("issue_" + responseBody.get("id"), responseBody);
                }
            }

            return responseMap;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access token expired or invalid. Please re-authenticate with GitHub.");
            } else {
                throw new RuntimeException("Error exporting GitHub issues: " + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error exporting GitHub issues: " + e.getMessage(), e);
        }
    }

    private JSONArray prepareGithubData(IssuesRequestBodyDTO requestBody) throws Exception {
        JSONArray issues = new JSONArray();

        for (var issue : requestBody.getIssues()) {
            reportRepository.findById(issue).ifPresent(report -> {
                var integrationKeyMap = applicationRepository.findById(report.getAppId())
                        .map(app -> app.getIntegrationKeyMap().get("GITHUB"))
                        .orElseThrow(() -> new IllegalArgumentException("GITHUB integration keys not properly configured for application."));

                JSONObject issueJSON = new JSONObject();
                issueJSON.put("title", report.getTitle());
                issueJSON.put("body", report.getDescription());
                issueJSON.put("repoName", integrationKeyMap.get("repoName"));
                issues.add(issueJSON);
                report.setExportedOn(new Date());
                reportRepository.save(report);
            });
        }

        return issues;
    }

    public Integration createGitHubIntegration(User user, String code) throws Exception {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", githubClientId);
            body.add("client_secret", githubClientSecret);
            body.add("code", code);
            body.add("redirect_uri", githubRedirectUrl);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = sendRequest(new URI("https://github.com/login/oauth/access_token"), requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("error=")) {
                    Map<String, String> responseParams = parseResponseParameters(responseBody);
                    String error = responseParams.getOrDefault("error", "unknown_error");
                    String errorDescription = responseParams.getOrDefault("error_description", "No description provided.");
                    throw new Exception("GitHub API error: " + error + " - " + errorDescription);
                }

                String accessToken = parseAccessToken(responseBody);
                return integrationService.createOrUpdateIntegration("GITHUB", user, accessToken, "");
            }

            return null;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access token expired or invalid. Please re-authenticate with GitHub.");
            } else {
                throw new RuntimeException("Error creating GitHub integration: " + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating GitHub integration: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    private String parseAccessToken(String responseBody) {
        String[] parts = responseBody.split("&");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2 && keyValue[0].equals("access_token")) {
                return keyValue[1];
            }
        }
        return null; // Access token not found in response
    }

    private Organisation getOrganisationByUser(User user) {
        return Optional.ofNullable(teamMemberRepository.findByUser(user))
                .map(TeamMember::getOrganisation)
                .orElse(null);
    }

    private Integration getIntegration(Organisation organisation, String type) {
        return integrationRepository.findByOrganisationAndIntegrationType(organisation, type)
                .orElseThrow(() -> new IllegalArgumentException(type + " integration not found."));
    }

    private URI createURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error creating GitHub API URI", e);
        }
    }

    private <T> ResponseEntity<T> sendRequest(URI uri, HttpHeaders headers, Class<T> responseType) {
        return new RestTemplate().exchange(new RequestEntity<>(headers, HttpMethod.GET, uri), responseType);
    }

    private <T> ResponseEntity<T> sendRequest(URI uri, HttpEntity<?> requestEntity, Class<T> responseType) {
        return new RestTemplate().exchange(uri, HttpMethod.POST, requestEntity, responseType);
    }

    private GithubRepositoryDTO findRepositoryByName(List<GithubRepositoryDTO> repositories, String repoName) {
        return repositories.stream()
                .filter(repo -> repo.getName().equals(repoName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + repoName));
    }

    private Map<String, String> parseResponseParameters(String responseBody) {
        return Arrays.stream(responseBody.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> URLDecoder.decode(entry[1], StandardCharsets.UTF_8)));
    }
}
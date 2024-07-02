package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;

import static com.quashbugs.quash.constants.Constants.LINEAR;

import com.quashbugs.quash.model.Integration;
import com.quashbugs.quash.model.Organisation;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LinearIntegrationService {

    private final IntegrationService integrationService;

    private final StorageService storageService;

    private final IntegrationRepository integrationRepository;

    private final TeamMemberRepository teamMemberRepository;

    private final ReportRepository reportRepository;

    private final ApplicationRepository applicationRepository;

    @Autowired
    public LinearIntegrationService(IntegrationService integrationService,
                                    StorageService storageService,
                                    IntegrationRepository integrationRepository,
                                    TeamMemberRepository teamMemberRepository,
                                    ReportRepository reportRepository,
                                    ApplicationRepository applicationRepository) {
        this.integrationService = integrationService;
        this.storageService = storageService;
        this.integrationRepository = integrationRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.reportRepository = reportRepository;
        this.applicationRepository = applicationRepository;
    }

    @Value("${spring.linear.auth_endpoint}")
    private String linearAuthEndpoint;

    @Value("${spring.linear.client_id}")
    private String linearClientId;

    @Value("${spring.linear.redirect_uri}")
    private String linearRedirectUri;

    @Value("${spring.linear.client_secret}")
    private String linearClientSecret;

    public String createLinearOAuthURL() {
        return "https://linear.app/oauth/authorize" +
                "?client_id=" + linearClientId +
                "&redirect_uri=" + linearRedirectUri +
                "&scope=read,write,issues:create" +
                "&response_type=code";
    }

    public Integration createLinearIntegration(User user, String code) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", linearClientId);
            body.add("client_secret", linearClientSecret);
            body.add("code", code);
            body.add("redirect_uri", URLDecoder.decode(linearRedirectUri, StandardCharsets.UTF_8));

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(linearAuthEndpoint, HttpMethod.POST, requestEntity, Map.class);
            String accessToken = (String) response.getBody().get("access_token");
            var integration = integrationService.createOrUpdateIntegration(LINEAR, user, accessToken, "");
            setLinearIntegrationProjects(integration, accessToken);
            integration.setExpiryTime(null);
            integrationRepository.save(integration);
            return integration;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void setLinearIntegrationProjects(Integration integration, String accessToken) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            String linearEndpoint = "https://api.linear.app/graphql";

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Use a Map to construct the JSON payload
            Map<String, String> body = new HashMap<>();
            body.put("query", "query Teams { teams { nodes { id name } } }");

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Object> response = restTemplate.exchange(linearEndpoint, HttpMethod.POST, requestEntity, Object.class);

            integrationService.updateIntegration(integration, response);

            response.getBody();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public LinkedHashMap getLinearIntegrationProjects(User user) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integrations = integrationRepository.findAllByOrganisation(organisation);
            Integration integration = integrations.stream()
                    .filter(integration1 -> integration1.getIntegrationType().equals("LINEAR"))
                    .findFirst().get();
            var linearAuthToken = (String) integration.getSettings().get("integrationAccessToken");
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            String linearEndpoint = "https://api.linear.app/graphql";
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(linearAuthToken);

            String graphqlQuery = "query TeamsAndProjects { teams { nodes { id name projects { nodes { id name } } } } }";
            String requestBody = "{\"query\":\"" + graphqlQuery + "\"}";

            //projects
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Object> response = restTemplate.exchange(linearEndpoint, HttpMethod.POST, requestEntity, Object.class);
            integrationService.updateIntegration(integration, response);
            return (LinkedHashMap) response.getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access token expired or invalid. Please re-authenticate with GitHub.");
            } else {
                throw ex;
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private JSONObject prepareLinearIssueJSON(IssuesRequestBodyDTO requestBody) throws Exception {
        JSONArray issues = new JSONArray();
        JSONObject requestJSON = new JSONObject();

        for (var issue : requestBody.getIssues()) {
            var report = reportRepository.findById(issue);

            if (report.isPresent()) {
                var integrationKeyMap = applicationRepository.findById(report.get().getAppId()).get().getIntegrationKeyMap();
                var linearKeyMap = integrationKeyMap.get("LINEAR");
                if (linearKeyMap == null || !linearKeyMap.containsKey("teamId") || !linearKeyMap.containsKey("projectId")) {
                    throw new Exception("LINEAR integration keys not properly configured for application.");
                }
                var teamId = linearKeyMap.get("teamId");
                var projectId = linearKeyMap.get("projectId");

                var bugMediaList = report.get().getListOfMedia();

                JSONObject issueJSON = new JSONObject();
                // Add title
                issueJSON.put("teamId", teamId);
                issueJSON.put("projectId", projectId);
                issueJSON.put("title", report.get().getTitle());
                issueJSON.put("description", report.get().getDescription());
                issueJSON.put("id", report.get().getId());

                // Attachments part in the issueJSON
                JSONArray attachments = new JSONArray();

                // Iterate over bugMediaList to create attachments
                for (var bugMedia : bugMediaList) {
                    JSONObject attachmentMetadata = new JSONObject();
                    attachmentMetadata.put("issueId", report.get().getId());
                    attachmentMetadata.put("title", "Exception");

                    String imageUrl = storageService.generateSignedUrl(bugMedia.getMediaRef());
                    attachmentMetadata.put("url", imageUrl);

                    attachments.add(attachmentMetadata);
                }

                issueJSON.put("attachments", attachments);

                issues.add(issueJSON);
                report.get().setExportedOn(new Date());
                reportRepository.save(report.get());
            }
        }

        requestJSON.put("issueUpdates", issues);
        return requestJSON;
    }

    public JSONArray exportLinearIssues(User user, IssuesRequestBodyDTO requestBody) throws Exception {
        try {
            var requestJSON = prepareLinearIssueJSON(requestBody).get("issueUpdates");
            var accessToken = getLinearAccessToken(user);
            var responseJSON = new JSONArray();
            for (Object requestJSONItemObj : (JSONArray) requestJSON) {
                if (!(requestJSONItemObj instanceof JSONObject)) {
                    continue;
                }
                JSONObject requestJSONItem = (JSONObject) requestJSONItemObj;

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                String linearEndpoint = "https://api.linear.app/graphql";
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(accessToken);

                // Send the query in the request body as JSON
                String requestBodyLinear = buildMutationQuery(requestJSONItem);

                HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyLinear, headers);
                ResponseEntity<Object> responseEntity = restTemplate.exchange(linearEndpoint, HttpMethod.POST, requestEntity, Object.class);

                Object responseBody = responseEntity.getBody();
                responseJSON.add(responseEntity.getBody());

                if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    // Issue created successfully

                    Object imageUrlsObj = requestJSONItem.get("attachments");
                    JSONArray imageUrls = null;
                    if (imageUrlsObj instanceof JSONArray) {
                        imageUrls = (JSONArray) imageUrlsObj;
                    }

                    for (Object imageUrlObj : imageUrls) {
                        if (!(imageUrlObj instanceof JSONObject)) {
                            continue;
                        }

                        JSONObject imageUrlJson = (JSONObject) imageUrlObj;
                        String imageUrl = imageUrlJson.get("url").toString();

                        String id = extractIssueId(responseBody);
                        String requestBodyAttachment = buildAttachmentQuery(id, requestJSONItem.get("id").toString(), imageUrl);

                        HttpEntity<String> requestEntityAttachment = new HttpEntity<>(requestBodyAttachment, headers);
                        ResponseEntity<Object> responseEntityAttachment = restTemplate.exchange(linearEndpoint, HttpMethod.POST, requestEntityAttachment, Object.class);

                        // Handle the response for the attachment creation
                        if (responseEntityAttachment.getStatusCode().equals(HttpStatus.OK)) {
                            responseJSON.add(responseEntityAttachment.getBody());
                        } else {
                            // Handle the case where attachment creation failed
                            // Log an error or take appropriate action
                        }
                    }
                }
            }

            return responseJSON;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access token expired or invalid. Please re-authenticate with Linear.");
            } else {
                throw ex;
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private String getLinearAccessToken(User user) {
        Organisation organisation = teamMemberRepository.findByUser(user).getOrganisation();
        Integration integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, "LINEAR")
                .orElseThrow(() -> new IllegalArgumentException("Linear integration not found."));
        return (String) integration.getSettings().get("integrationAccessToken");
    }

    private String buildMutationQuery(JSONObject requestJSONItem) {
        String title = requestJSONItem.get("title") != "" ? requestJSONItem.get("title").toString() : "";
        String description = requestJSONItem.get("description") != "" ? requestJSONItem.get("description").toString() : "";
        String teamId = requestJSONItem.get("teamId") != "" ? requestJSONItem.get("teamId").toString() : "";
        String projectId = requestJSONItem.get("projectId") != "" ? requestJSONItem.get("projectId").toString() : "";
        StringBuilder mutationQueryBuilder = new StringBuilder("mutation { issueCreate(input: { title: \\\"").append(title).append("\\\"")
                .append(" description: \\\"").append(description).append("\\\"")
                .append(" teamId: \\\"").append(teamId).append("\\\"");

        if (projectId != null && !projectId.isEmpty()) {
            mutationQueryBuilder.append(" projectId: \\\"").append(projectId).append("\\\"");
        }

        mutationQueryBuilder.append(" }) { success issue { id title } } }");

        String mutationQuery = mutationQueryBuilder.toString();
        return "{\"query\":\"" + mutationQuery + "\"}";
    }

    private String extractIssueId(Object responseBody) {
        String id = null;
        Object issueCreateObj = ((LinkedHashMap<String, Object>) responseBody).get("data");
        if (issueCreateObj instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> issueCreateMap = (LinkedHashMap<String, Object>) issueCreateObj;
            Object issueObj = issueCreateMap.get("issueCreate");
            if (issueObj instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> issueMap = (LinkedHashMap<String, Object>) issueObj;
                Object issueIdObj = issueMap.get("issue");
                if (issueIdObj instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> issueIdMap = (LinkedHashMap<String, Object>) issueIdObj;
                    id = (String) issueIdMap.get("id");
                }
            }
        }
        return id;
    }

    private String buildAttachmentQuery(String id, String title, String imageUrl) {

        String attachmentQueryFinal = "mutation { attachmentCreate(input: { issueId: \\\"" + id + "\\\"" +
                " title: \\\"" + title + "\\\"" +
                " url: \\\"" + imageUrl + "\\\"" +
                " }) { success attachment { id } } }"
                ;

        return "{\"query\":\"" + attachmentQueryFinal + "\"}";
    }
}
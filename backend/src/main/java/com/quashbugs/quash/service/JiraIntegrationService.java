package com.quashbugs.quash.service;

import com.quashbugs.quash.constants.Constants;
import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.model.Integration;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.Report;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.ApplicationRepository;
import com.quashbugs.quash.repo.IntegrationRepository;
import com.quashbugs.quash.repo.ReportRepository;
import com.quashbugs.quash.repo.TeamMemberRepository;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static com.quashbugs.quash.constants.Constants.*;

@Service
public class JiraIntegrationService {
    @Value("${spring.frontend.url}")
    private String frontEndBaseUrl;

    @Value("${spring.atlassian.jira.client_id}")
    private String jiraClientId;

    @Value("${spring.atlassian.jira.client_secret}")
    private String jiraClientSecret;

    @Value("${spring.atlassian.jira.auth_endpoint}")
    private String jiraAuthEndpoint;

    @Value("${spring.atlassian.jira.accessible_resource_endpoint}")
    private String jiraAccessibleResourceEndpoint;

    private final IntegrationRepository integrationRepository;

    private final TeamMemberRepository teamMemberRepository;

    private final ReportRepository reportRepository;

    private final StorageService storageService;

    private final IntegrationService integrationService;

    private final ApplicationRepository applicationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraIntegrationService.class);

    @Autowired
    public JiraIntegrationService(
            IntegrationRepository integrationRepository,
            TeamMemberRepository teamMemberRepository,
            ReportRepository reportRepository,
            StorageService storageService,
            IntegrationService integrationService,
            ApplicationRepository applicationRepository) {
        this.integrationRepository = integrationRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.integrationService = integrationService;
        this.applicationRepository = applicationRepository;
    }

    public Integration getAccessFromRefreshToken(String code, User user) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(grant_type, authorization_code);
            body.add(client_id, jiraClientId);
            body.add(client_secret, jiraClientSecret);
            body.add(Constants.code, code);
            body.add(redirect_uri, frontEndBaseUrl + "/settings/integrations?integration=JIRA");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(jiraAuthEndpoint, HttpMethod.POST, requestEntity, Map.class);
            String accessToken = (String) response.getBody().get(access_token);
            String refreshToken = (String) response.getBody().get(refresh_token);
            return integrationService.createOrUpdateIntegration(JIRA, user, accessToken, refreshToken);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void setJiraCloudId(User user) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, JIRA).get();
            integration = isIntegrationExpired(integration);

            var jiraAuthToken = integration.getSettings().get("integrationAccessToken").toString();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jiraAuthToken);
            ResponseEntity<Object> response = restTemplate.exchange(jiraAccessibleResourceEndpoint, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            List requestMap = (List) response.getBody();
            var item = (LinkedHashMap) requestMap.get(0);
            var cloudId = (String) item.get("id");

            var settings = integration.getSettings();
            settings.put("cloudId", cloudId);
            integration.setSettings(settings);
            integrationRepository.save(integration);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private Integration generateAccessToken(Integration integration) throws Exception {
        try {
            var refreshToken = integration.getSettings().get("integrationRefreshToken").toString();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(grant_type, refresh_token);
            body.add(client_id, jiraClientId);
            body.add(client_secret, jiraClientSecret);
            body.add(refresh_token, refreshToken);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(jiraAuthEndpoint, HttpMethod.POST, requestEntity, Map.class);
            String accessToken = (String) response.getBody().get(access_token);
            String newRefreshToken = (String) response.getBody().get(refresh_token);
            return integrationService.createOrUpdateIntegration(JIRA, integration.getOrganisation().getCreatedBy(), accessToken, newRefreshToken);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public List getJiraUsers(User user) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, JIRA).get();
            integration = isIntegrationExpired(integration);

            var jiraAuthToken = integration.getSettings().get("integrationAccessToken").toString();
            var jiraCloudId = integration.getSettings().get("cloudId").toString();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            String usersEndPoint = "https://api.atlassian.com/ex/jira/" + jiraCloudId + "/rest/api/3/users/search";
            headers.setBearerAuth(jiraAuthToken);
            ResponseEntity<Object> response = restTemplate.exchange(usersEndPoint, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            return (List) response.getBody();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void createJiraUsers(User normalUser, List usersResponse) throws Exception {
        for (var jiraUser : usersResponse) {
            try {
                Map item = (Map) jiraUser;
                var displayName = item.get("displayName").toString();
                if (displayName.equals("Quash Integration")) {
                    var organisation = teamMemberRepository.findByUser(normalUser).getOrganisation();
                    var integrationOpt = integrationRepository.findByOrganisationAndIntegrationType(organisation, JIRA);
                    if (integrationOpt.isPresent()) {
                        var integration = integrationOpt.get();
                        var settings = integration.getSettings();
                        settings.put("integrationAgent", item);
                        integration.setSettings(settings);
                        integrationRepository.save(integration);
                    }
                    break;
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new Exception(e);
            }
        }
    }

    public List getJiraIssueTypes(User user, String projectKey) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, JIRA).get();
            integration = isIntegrationExpired(integration);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(projectId, projectKey);
            var jiraAuthToken = integration.getSettings().get("integrationAccessToken").toString();
            var jiraCloudId = integration.getSettings().get("cloudId").toString();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            String issueTypeEndPoint = "https://api.atlassian.com/ex/jira/" + jiraCloudId + "/rest/api/3/issuetype/project?projectId=" + projectKey;
            headers.setBearerAuth(jiraAuthToken);
            ResponseEntity<Object> response = restTemplate.exchange(issueTypeEndPoint, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            return (List) response.getBody();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public LinkedHashMap getJiraProjects(User user) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integrations = integrationRepository.findAllByOrganisation(organisation);
            var integration = integrations.stream()
                    .filter(integration1 -> integration1.getIntegrationType().equals(JIRA))
                    .findFirst().get();
            integration = isIntegrationExpired(integration);

            if (integration.getSettings().get("projects") != null) {
                return (LinkedHashMap) integration.getSettings().get("projects");
            }

            var jiraAuthToken = integration.getSettings().get("integrationAccessToken").toString();
            var jiraCloudId = integration.getSettings().get("cloudId").toString();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            String usersEndPoint = "https://api.atlassian.com/ex/jira/" + jiraCloudId + "/rest/api/3/project/search";
            headers.setBearerAuth(jiraAuthToken);
            ResponseEntity<Object> response = restTemplate.exchange(usersEndPoint, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

            LinkedHashMap responseBody = (LinkedHashMap) response.getBody();
            List<Map<String, Object>> projects = (List<Map<String, Object>>) responseBody.get("values");
            for (Map<String, Object> project : projects) {
                String projectKey = (String) project.get("id");
                List issueTypes = getJiraIssueTypes(user, projectKey);
                project.put(Constants.issueTypes, issueTypes);
            }

            integrationService.updateIntegration(integration, response);
            return responseBody;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public LinkedHashMap exportJiraIssues(User user, IssuesRequestBodyDTO requestBody) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, JIRA).get();
            integration = isIntegrationExpired(integration);

            var requestJSON = prepareIssueJSON(requestBody, organisation, integration);
            var jiraAuthToken = integration.getSettings().get("integrationAccessToken").toString();
            var jiraCloudId = integration.getSettings().get("cloudId").toString();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();

            String usersEndPoint = "https://api.atlassian.com/ex/jira/" + jiraCloudId + "/rest/api/3/issue/bulk";
            headers.setBearerAuth(jiraAuthToken);
            ResponseEntity<Object> response = restTemplate.exchange(usersEndPoint, HttpMethod.POST,
                    new HttpEntity<>(requestJSON, headers), Object.class);
            addIssueAttachments((LinkedHashMap) response.getBody(), requestBody);
            return (LinkedHashMap) response.getBody();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private JSONObject prepareIssueJSON(IssuesRequestBodyDTO requestBody, Organisation organisation, Integration integration) throws Exception {
        JSONArray issues = new JSONArray();
        JSONObject requestJSON = new JSONObject();
        boolean all_issues_found = true;
        for (var issue : requestBody.getIssues()) {
            String reportId = issue.toString();
            var report = reportRepository.findById(reportId);

            if (report.isPresent()) {
                Map<String, Object> integrationAgent = (Map<String, Object>) integration.getSettings().get("integrationAgent");
                String agentId = integrationAgent.get("accountId").toString();
                var integrationKeyMap = applicationRepository.findById(report.get().getAppId()).get().getIntegrationKeyMap();

                JSONObject jiraKeyMap = integrationKeyMap.get(JIRA);
                if (jiraKeyMap == null || !jiraKeyMap.containsKey(projectKey) || !jiraKeyMap.containsKey(issueTypeKey)) {
                    throw new Exception("JIRA integration keys not properly configured for application.");

                }

                var projectKey = jiraKeyMap.get(Constants.projectKey);
                var issueTypeKey = jiraKeyMap.get(Constants.issueTypeKey);
                if (projectKey.equals("")) {
                    throw new Exception("Project Key is not specified for application.");
                }

                if (issueTypeKey.equals("")) {
                    throw new Exception("Issue Type is not selected for this integration.");
                }
                JSONObject issueJSON = new JSONObject();
                JSONObject fieldJSON = new JSONObject();
                // Construct Necessary fields
                JSONObject projectJSON = new JSONObject();

                // Add issue type
                JSONObject issuetypeJSON = new JSONObject();
                issuetypeJSON.put("id", issueTypeKey);
                issueJSON.put("issuetype", issuetypeJSON);

                // Add project
                projectJSON.put("id", projectKey);
                issueJSON.put("project", projectJSON);

                // Add title
                issueJSON.put(summary, report.get().getTitle());

                // Add description
                JSONObject description = getDescriptionJson(report.get());

                issueJSON.put(Constants.description, description);

                // Add labels
                JSONArray labels = new JSONArray();

                labels.add(report.get().getType());
                labels.add(QUASH);

                issueJSON.put(Constants.labels, labels);

                // Add reporter
                JSONObject reporterJSON = new JSONObject();
                reporterJSON.put("id", agentId);
                issueJSON.put(reporter, reporterJSON);


                fieldJSON.put(fields, issueJSON);
                issues.add(fieldJSON);
                // You can use the `requestJSON` for further processing
                report.get().setExportedOn(new Date());
                reportRepository.save(report.get());
            } else {
                all_issues_found = false;
            }
        }
        if (!all_issues_found) {
            issues = new JSONArray();
        }
        requestJSON.put(issueUpdates, issues);
        System.out.println(requestJSON);
        return requestJSON;
    }

    private JSONObject getDescriptionJson(Report report) {
        JSONObject descriptionContent = new JSONObject();
        JSONObject descriptionText = new JSONObject();
        descriptionText.put(text, report.getDescription());
        descriptionText.put(type, text);
        descriptionContent.put(content, new JSONArray().appendElement(descriptionText));
        descriptionContent.put(type, paragraph);

        JSONObject description = new JSONObject();
        description.put(content, new JSONArray().appendElement(descriptionContent));
        description.put(type, doc);
        description.put(version, 1);
        return description;
    }

    private void addIssueAttachments(LinkedHashMap response, IssuesRequestBodyDTO requestBody) throws Exception {
        try {
            List issues = (List) response.get(Constants.issues);
            List errors = (List) response.get(Constants.errors);

            List requestIssues = requestBody.getIssues();
            List<Integer> indexesToRemove = new ArrayList<>(); // To store indexes to be removed

            for (Object errorObj : errors) {
                if (errorObj instanceof Map) {
                    Map error = (Map) errorObj;
                    Integer failedElementNumber = (Integer) error.get(Constants.failedElementNumber);
                    indexesToRemove.add(failedElementNumber); // Collect indexes to remove
                }
            }

            // Remove issues from requestIssues based on indexesToRemove
            List filteredIssues = new JSONArray();
            for (int i = 0, j = 0; j < issues.size() && i < requestIssues.size(); i++) {
                if (!indexesToRemove.contains(i)) {
                    JSONObject issue = new JSONObject();

                    // Assuming each issue in 'issues' is a dictionary-like object
                    LinkedHashMap<String, Object> issueObject = (LinkedHashMap) issues.get(j);
                    Object issueId = issueObject.get("id");

                    issue.put(reportId, requestIssues.get(j));
                    issue.put(jiraIssueId, issueId);

                    filteredIssues.add(issue);
                    j++;
                }
            }
            processJiraAttachments(filteredIssues);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void processJiraAttachments(List filteredIssues) {
        List<Map<String, Object>> issues = filteredIssues;
        for (var issue : issues) {
            String reportId = (String) issue.get(Constants.reportId);
            String jiraIssueId = (String) issue.get(Constants.jiraIssueId);
            if (reportId != null) {

                var report = reportRepository.findById(reportId);
                var bugMediaList = report.get().getListOfMedia();
                var crashLog = report.get().getCrashLog();
                if (bugMediaList != null) {
                    for (var bugMedia : bugMediaList) {
                        try {
                            byte[] mediaBytes = downloadMedia(storageService.generateSignedUrl(bugMedia.getMediaRef()));
                            uploadAttachmentToJira(jiraIssueId, mediaBytes, bugMedia.getId(), report.get());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (crashLog != null) {
                    try {
                        byte[] mediaBytes = downloadMedia(storageService.generateSignedUrl(crashLog.getMediaRef()));
                        uploadAttachmentToJira(jiraIssueId, mediaBytes, crashLog.getId(), report.get());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                // Rest of your processing here
            }
        }
    }

    private byte[] downloadMedia(String mediaUrl) throws IOException {
        URL url = new URL(mediaUrl);
        try (InputStream in = url.openStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    public void uploadAttachmentToJira(String issueIdOrKey, byte[] attachmentBytes, String filename, Report report) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(report.getReportedBy()).getOrganisation();
            var integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, JIRA).get();
            integration = isIntegrationExpired(integration);

            var jiraAuthToken = integration.getSettings().get("integrationAccessToken").toString();
            var jiraCloudId = integration.getSettings().get("cloudId").toString();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Atlassian-Token", "no-check");

            String attachmentEndpoint = "https://api.atlassian.com/ex/jira/" + jiraCloudId + "/rest/api/3/issue/" + issueIdOrKey + "/attachments";
            headers.setBearerAuth(jiraAuthToken);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(createAttachmentMultiPart(attachmentBytes, filename), headers);
            ResponseEntity<Object> response = restTemplate.exchange(attachmentEndpoint, HttpMethod.POST, requestEntity, Object.class);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private MultiValueMap<String, Object> createAttachmentMultiPart(byte[] attachmentBytes, String filename) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        ByteArrayResource resource = new ByteArrayResource(attachmentBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(resource, fileHeaders);

        parts.add("file", fileEntity);

        return parts;
    }

    private Integration isIntegrationExpired(Integration integration) throws Exception {
        Date expiryTime = (Date) integration.getSettings().get("expiryTime");
        if (expiryTime == null || expiryTime.before(new Date())) {
            integration = generateAccessToken(integration);
        }
        return integration;
    }
}
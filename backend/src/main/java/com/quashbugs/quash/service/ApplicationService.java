package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.request.ApplicationRequestBodyDTO;
import com.quashbugs.quash.exceptions.ReportNotFoundException;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.QuashClientApplication;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.ApplicationRepository;
import com.quashbugs.quash.repo.OrganisationRepository;
import com.quashbugs.quash.repo.TeamMemberRepository;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.util.*;

import static com.quashbugs.quash.constants.Constants.*;

@Service
public class ApplicationService {

    private final OrganisationRepository organisationRepository;

    private final ApplicationRepository applicationRepository;

    private final TeamMemberRepository teamMemberRepository;

    private final JwtService jwtService;

    private final ReportsService reportsService;

    private final UtilsService utilsService;

    @Autowired
    public ApplicationService(OrganisationRepository organisationRepository,
                              ApplicationRepository applicationRepository,
                              TeamMemberRepository teamMemberRepository,
                              JwtService jwtService,
                              ReportsService reportsService,
                              UtilsService utilsService) {
        this.organisationRepository = organisationRepository;
        this.applicationRepository = applicationRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.jwtService = jwtService;
        this.reportsService = reportsService;
        this.utilsService = utilsService;
    }

    public boolean isUserPartOfOrganisation(User user, Organisation organisation) {
        var organisationOpt = teamMemberRepository.findByUser(user).getOrganisation();
        return organisationOpt.equals(organisation);
    }

    public QuashClientApplication registerApp(ApplicationRequestBodyDTO requestBody) throws Exception {
        // Fetch the organisation.
        Organisation org = organisationRepository.findOptionalByOrgUniqueKey(requestBody.getOrgUniqueKey())
                .orElseThrow(() -> new InvalidKeyException("Invalid Organisation unique key provided."));

        // Check if the application with the given package name already exists.
        Optional<QuashClientApplication> existingApp = applicationRepository.findByPackageName(requestBody.getPackageName());
        if (existingApp.isPresent()) {
            return existingApp.get();
        }

        // Create and save the new application.
        QuashClientApplication app = new QuashClientApplication();
        app.setAppName(requestBody.getAppName());
        app.setPackageName(requestBody.getPackageName());
        app.setAppType(requestBody.getAppType());
        app.setRegisteredAt(new Date());
        app.setAppStatus(VERIFIED);
        app.setOrganisation(org);
        String reportingToken = jwtService.generateReportingToken(org);
        app.setRegistrationToken(reportingToken);

        applicationRepository.save(app);
        return app;
    }

    public void setProjectKeys(List<Map<String, String>> projectKeyMapBody) throws Exception {
        for (Map<String, String> item : projectKeyMapBody) {
            String applicationId = item.get("appId");
            String integrationType = item.get("integrationType");
            Optional<QuashClientApplication> applicationOpt = applicationRepository.findById(applicationId);

            if (applicationOpt.isEmpty()) {
                throw new Exception("Application not found for ID: " + applicationId);
            }
            QuashClientApplication application = applicationOpt.get();
            switch (integrationType) {
                case JIRA -> updateJIRAIntegration(item, application);
                case SLACK -> updateSLACKIntegration(item, application);
                case LINEAR -> updateLinearIntegration(item, application);
                case GITHUB -> updateGitHubIntegration(item, application);
                default -> throw new IllegalArgumentException("Invalid integration type: " + integrationType);
            }
        }
    }

    private void updateGitHubIntegration(Map<String, String> item, QuashClientApplication application) throws IllegalArgumentException {
        JSONObject integrationJSON = new JSONObject();
        integrationJSON.put("integrationType", GITHUB);
        integrationJSON.put("repoName", item.get("repoName"));

        Map<String, JSONObject> integrationMap = getOrCreateIntegrationMap(application);
        integrationMap.put(GITHUB, integrationJSON);
        application.setIntegrationKeyMap(integrationMap);
        applicationRepository.save(application);
    }

    private void updateLinearIntegration(Map<String, String> item, QuashClientApplication application) throws IllegalArgumentException {
        JSONObject integrationJSON = new JSONObject();
        integrationJSON.put("integrationType", LINEAR);
        integrationJSON.put("teamId", item.get("teamId"));
        integrationJSON.put("projectId", item.get("projectId"));

        Map<String, JSONObject> integrationMap = getOrCreateIntegrationMap(application);
        integrationMap.put(LINEAR, integrationJSON);
        application.setIntegrationKeyMap(integrationMap);
        applicationRepository.save(application);
    }

    private void updateJIRAIntegration(Map<String, String> item, QuashClientApplication application) throws IllegalArgumentException {
        JSONObject integrationJSON = new JSONObject();
        integrationJSON.put("integrationType", JIRA);
        integrationJSON.put("projectKey", item.get("projectKey"));
        integrationJSON.put("issueTypeKey", item.get("issueType"));

        Map<String, JSONObject> integrationMap = getOrCreateIntegrationMap(application);
        integrationMap.put(JIRA, integrationJSON);
        application.setIntegrationKeyMap(integrationMap);
        applicationRepository.save(application);
    }

    private void updateSLACKIntegration(Map<String, String> item, QuashClientApplication application) throws IllegalArgumentException {
        JSONObject integrationJSON = new JSONObject();
        integrationJSON.put("integrationType", SLACK);
        integrationJSON.put("channelId", item.get("channelId"));

        Map<String, JSONObject> integrationMap = getOrCreateIntegrationMap(application);
        integrationMap.put(SLACK, integrationJSON);
        application.setIntegrationKeyMap(integrationMap);
        applicationRepository.save(application);
    }


    private Map<String, JSONObject> getOrCreateIntegrationMap(QuashClientApplication application) {
        Map<String, JSONObject> integrationMap = application.getIntegrationKeyMap();
        if (integrationMap == null) {
            integrationMap = new HashMap<>();
        }
        return integrationMap;
    }

    public void deleteApp(String appId, User user) {
        TeamMember teamMember = teamMemberRepository.findByUser(user);
        Organisation organisation = teamMember.getOrganisation();

        if (!utilsService.doesAppBelongToThisOrg(appId, String.valueOf(organisation.getId()))) {
            throw new IllegalStateException("Organisation mismatch, can't delete app");
        }

        if (!teamMember.getRole().equals(ADMIN)) {
            throw new IllegalStateException("Can't delete app, user is not an admin");
        }

        Optional<QuashClientApplication> toDeleteApp = applicationRepository.findById(appId);
        if (toDeleteApp.isEmpty()) {
            throw new IllegalStateException("App is not present");
        }
        try {
            reportsService.deleteAllReportsByAppId(appId);
            applicationRepository.delete(toDeleteApp.get());
        } catch (ReportNotFoundException e) {
            throw new IllegalStateException("Faulty reports in the app, can't delete");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Organisation getOrganisationFromObject(Object principalObj) {
        if (principalObj instanceof Optional<?> optional) {
            if (optional.isPresent()) {
                Object content = optional.get();
                if (content instanceof User user) {
                    TeamMember teamMember = teamMemberRepository.findByUser(user);
                    return teamMember != null ? teamMember.getOrganisation() : null;
                } else if (content instanceof Organisation) {
                    return (Organisation) content;
                }
            }
        } else {
            if (principalObj instanceof User user) {
                TeamMember teamMember = teamMemberRepository.findByUser(user);
                return teamMember != null ? teamMember.getOrganisation() : null;
            } else if (principalObj instanceof Organisation) {
                return (Organisation) principalObj;
            }
        }
        return null;
    }
}
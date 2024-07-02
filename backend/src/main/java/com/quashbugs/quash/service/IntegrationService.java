package com.quashbugs.quash.service;

import com.quashbugs.quash.model.*;
import com.quashbugs.quash.repo.*;
import com.quashbugs.quash.util.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.quashbugs.quash.constants.Constants.*;

@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;

    private final OrganisationRepository organisationRepository;

    private final TeamMemberRepository teamMemberRepository;

    private final ReportRepository reportRepository;

    private final StorageService storageService;

    private final ApplicationRepository applicationRepository;

    private final SlackIntegrationService slackService;

    private final CryptoService cryptoService;

    @Autowired
    public IntegrationService(
            IntegrationRepository integrationRepository,
            OrganisationRepository organisationRepository,
            TeamMemberRepository teamMemberRepository,
            ReportRepository reportRepository,
            StorageService storageService,
            ApplicationRepository applicationRepository,
            SlackIntegrationService slackService,
            CryptoService cryptoService) {
        this.integrationRepository = integrationRepository;
        this.organisationRepository = organisationRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.applicationRepository = applicationRepository;
        this.slackService = slackService;
        this.cryptoService = cryptoService;
    }

    public Integration createOrUpdateIntegration(String type, User user, String accessToken, String refreshToken) throws Exception {

        try {
            var integrations = integrationRepository.findAllByOrganisation(organisationRepository.findByCreatedBy(user));
            Boolean integrationExists = !integrations.stream()
                    .filter(integration -> integration.getIntegrationType().equals(type))
                    .collect(Collectors.toList()).isEmpty();
            if (integrationExists) {
                Optional<Integration> integration = integrations.stream()
                        .filter(integration1 -> integration1.getIntegrationType().equals(type))
                        .findFirst();

                var settings = integration.get().getSettings();
                if (type.equals(JIRA)) {
                    settings.put("integrationRefreshToken", refreshToken);
                    settings.put("expiryTime", getExpiryTime());
                }
                settings.put("integrationAccessToken", accessToken);

                integration.get().setSettings(settings);
                integrationRepository.save(integration.get());
                return integration.get();
            }

            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            if (organisation == null) {
                throw new Exception("Organisation not found");
            }
            LinkedHashMap<String, Object> settings = new LinkedHashMap<>();
            if (type.equals(JIRA)) {
                settings.put("integrationRefreshToken", refreshToken);
                settings.put("expiryTime", getExpiryTime());
            }
            settings.put("integrationAccessToken", accessToken);
            var integration = Integration.builder()
                    .integrationType(type)
                    .settings(settings)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .isActive(true)
                    .organisation(organisation).build();
            integrationRepository.save(integration);
            return integration;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private Date getExpiryTime() {
        Date currentTime = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);

        calendar.add(Calendar.HOUR_OF_DAY, 1);

        return calendar.getTime();
    }

    protected void updateIntegration(Integration integration, ResponseEntity<Object> response) throws Exception {
        try {
            LinkedHashMap<String, Object> settings = integration.getSettings();

            var resp = response.getBody();
            settings.put(projects, resp);
            integration.setSettings(settings);
            integrationRepository.save(integration);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public byte[] downloadMedia(String mediaUrl) throws IOException {
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

    public List<Integration> getAllIntegrations(User user) throws Exception {
        try {
            var teamMember = teamMemberRepository.findByUser(user);
            var organisation = teamMember.getOrganisation();
            var integrations = integrationRepository.findAllByOrganisation(organisation);
            return integrations;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void deleteIntegration(User user, String integrationId) throws Exception {
        try {
            var isAdmin = teamMemberRepository.findByUser(user).getRole().equals(ADMIN);
            if (isAdmin) {
                var integration = integrationRepository.findById(integrationId).get();
                if (integration.getOrganisation().equals(teamMemberRepository.findByUser(user).getOrganisation())) {
                    try {
//                        Revoke bot access token and then delete integration from db
                        if (integration.getIntegrationType().equals(SLACK)) {
                            var accessToken = integration.getSettings().get("encryptedAccessToken").toString();
                            if (slackService.revokeSlackAuthToken(cryptoService.decrypt(accessToken))) {
                                integrationRepository.delete(integration);
                                deleteKeyMap(user, integration.getIntegrationType());
                            }
                        } else {
                            integrationRepository.delete(integration);
                            deleteKeyMap(user, integration.getIntegrationType());
                        }
                    } catch (Exception e) {
                        throw new Exception(e.getMessage());
                    }
                } else {
                    throw new Exception("User doesn't have this integration");
                }
            } else {
                throw new Exception("User is not an admin");
            }
        } catch (Exception e) {
            throw new Exception("Team member not found");
        }
    }

    private void deleteKeyMap(User user, String integrationType) {
        var org = teamMemberRepository.findByUser(user).getOrganisation();
        if (org == null) {
            throw new IllegalStateException("User's organisation not found.");
        }
        var apps = applicationRepository.findAllByOrganisation(org);
        for (var app : apps) {
            if (app.getIntegrationKeyMap() != null && app.getIntegrationKeyMap().containsKey(integrationType)) {
                // Pass the integrationType to the removeIntegrationKeyFromApp method
                removeIntegrationKeyFromApp(app.getId(), integrationType);
            }
        }
    }

    public void removeIntegrationKeyFromApp(String appId, String integrationKey) {
        Optional<QuashClientApplication> appOpt = applicationRepository.findById(appId);
        if (appOpt.isEmpty()) {
            throw new RuntimeException("Application not found with ID: " + appId);
        }
        QuashClientApplication app = appOpt.get();
        // Remove the specific integration key from the map
        app.getIntegrationKeyMap().remove(integrationKey);

        applicationRepository.save(app);
    }

    public Integration getByUserAndIntegrationType(User user, String type) throws Exception {
        try {
            var organisation = teamMemberRepository.findByUser(user).getOrganisation();
            var integration = integrationRepository.findByOrganisationAndIntegrationType(organisation, type);
            if (integration.isPresent())
                return integration.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return null;
    }

    public Optional<byte[]> downloadCrashFile(String reportId) {
        Optional<Report> reportOptional = reportRepository.findById(reportId);

        if (reportOptional.isEmpty()) {
            return Optional.empty();  // Report not found
        }

        Report report = reportOptional.get();
        CrashLog crashLog = report.getCrashLog();
        if (crashLog == null) {
            return Optional.empty();  // Crash log not found
        }

        String mediaRef = crashLog.getMediaRef();
        String logUrl = storageService.generateSignedUrl(mediaRef);
        try {
            return Optional.of(downloadMedia(logUrl));
        } catch (IOException e) {
            throw new RuntimeException("Error downloading log for CrashLog with id: " + crashLog.getId(), e);
        }
    }
}
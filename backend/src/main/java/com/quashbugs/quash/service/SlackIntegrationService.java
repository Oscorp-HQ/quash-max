package com.quashbugs.quash.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.quashbugs.quash.constants.Constants;
import com.quashbugs.quash.dto.integration.SlackChannelsDTO;
import com.quashbugs.quash.model.*;
import com.quashbugs.quash.repo.ApplicationRepository;
import com.quashbugs.quash.repo.IntegrationRepository;
import com.quashbugs.quash.repo.TeamMemberRepository;
import com.quashbugs.quash.util.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.quashbugs.quash.constants.Constants.*;

@Service
@EnableAsync
public class SlackIntegrationService {

    private final Cache<String, Boolean> eventCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    @Autowired
    WebClient.Builder webClientBuilder;

    @Value("${spring.slack.heimdall_clientId}")
    private String slackClientId;

    @Value("${spring.slack.heimdall_clientSecret}")
    private String slackClientSecret;

    @Value("${spring.slack.redirectUri}")
    private String slackRedirectUri;

    private final CryptoService cryptoService;

    private final TeamMemberRepository teamMemberRepository;

    private final IntegrationRepository integrationRepository;

    private final ApplicationRepository applicationRepository;

    private final StorageService storageService;

    @Autowired
    public SlackIntegrationService(TeamMemberRepository teamMemberRepository,
                                   IntegrationRepository integrationRepository,
                                   ApplicationRepository applicationRepository,
                                   CryptoService cryptoService,
                                   StorageService storageService) {
        this.teamMemberRepository = teamMemberRepository;
        this.integrationRepository = integrationRepository;
        this.applicationRepository = applicationRepository;
        this.cryptoService = cryptoService;
        this.storageService = storageService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackIntegrationService.class);

    public String createSlackOAuthURL() {
        return "https://slack.com/oauth/v2/authorize" +
                "?client_id=" + slackClientId +
                "&redirect_uri=" + slackRedirectUri +
                "&scope=channels:read,chat:write,commands,groups:read,links.embed:write,links:read,links:write,mpim:read,users:read,files:write" +
                "&user_scope=chat:write,files:write,channels:read,groups:read,mpim:read,im:read,users:read,links.embed:write";
    }

    public void createOrUpdateSlackIntegration(User user, String code) throws Exception {
        var organisation = teamMemberRepository.findByUser(user).getOrganisation();
        var integrations = integrationRepository.findAllByOrganisation(organisation);

        Boolean slackIntegrationExists = !integrations.stream()
                .filter(integration -> integration.getIntegrationType().equals(SLACK))
                .collect(Collectors.toList()).isEmpty();

        Map<String, Object> slackData = retrieveSlackDataFromCode(code);

        if (!slackIntegrationExists) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put(encryptedAccessToken, cryptoService.encrypt((String) slackData.get(access_token)));
            map.put(slackUserId, ((Map) slackData.get(authed_user)).get("id"));
            map.put(userId, slackData.get(bot_user_id));
            map.put(teamId, ((Map) slackData.get(team)).get("id"));
            map.put(channels, null);
            var integration = Integration.builder().integrationType(SLACK).isActive(true).createdAt(new Date()).organisation(organisation).settings(map).build();
            integrationRepository.save(integration);
        }
    }

    public Map<String, Object> retrieveSlackDataFromCode(String code) throws Exception {
        WebClient webClient = webClientBuilder.baseUrl("https://slack.com/api").build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(client_id, slackClientId);
        formData.add(client_secret, slackClientSecret);
        formData.add(Constants.code, code);
        formData.add(redirect_uri, slackRedirectUri);

        ResponseEntity<Map> response;

        try {
            response = webClient.post()
                    .uri("/oauth.v2.access")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .toEntity(Map.class)
                    .block();
        } catch (Exception e) {
            throw new Exception("Failed to fetch slack workspace details");
        }

        // Get response and fetch the public channels from bot's access token.
        assert response != null;
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            return responseBody;
        } else {
            throw new Exception("Failed to fetch channel details or invalid response");
        }
    }

    public Map<String, Object> getChannelDetails(String channelId, String accessToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        ResponseEntity<Map> response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/conversations.info")
                        .queryParam("channel", channelId)
                        .build())
                .retrieve()
                .toEntity(Map.class)
                .block();

        if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody.containsKey("ok") && (Boolean) responseBody.get("ok")) {
                Map<String, Object> channelData = (Map<String, Object>) responseBody.get("channel");
                return channelData;
            }
        }
        return null;
    }

    public Integration getSlackIntegrationFromTeamId(String teamId) {
        try {
            var slackIntegrationOpt = integrationRepository.findBySettingsTeamId(teamId);
            if (slackIntegrationOpt.isEmpty()) {
                throw new Exception("No slack team with ths id exists");
            }
            return slackIntegrationOpt.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSlackIntegrationChannels(Integration slackIntegration, String channelId, String accessToken) {
        try {
            var settings = slackIntegration.getSettings();
            var newBotChannel = getChannelDetails(channelId, accessToken);
            var existingChannels = (List<Map<String, Object>>) settings.computeIfAbsent(channels, k -> new ArrayList<>());

            var newChannelId = (String) newBotChannel.get("id");
            boolean channelExists = existingChannels.stream()
                    .anyMatch(channel -> newChannelId.equals(channel.get("id")));

            if (!channelExists) {
                existingChannels.add(newBotChannel);
                slackIntegration.setSettings(settings);
                integrationRepository.save(slackIntegration);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSlackIntegrationToRemoveChannel(Integration slackIntegration, String channelId) {
        try {
            LinkedHashMap<String, Object> settings = slackIntegration.getSettings();
            ArrayList<LinkedHashMap<String, Object>> existingChannels = (ArrayList) settings.get(channels);
            existingChannels.removeIf(channel -> channel != null && channel.get("id").equals(channelId));
            settings.put(channels, existingChannels);
            slackIntegration.setSettings(settings);
            integrationRepository.save(slackIntegration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void checkAndPublishNotificationOnSlack(String appId, Report report) {
        applicationRepository.findById(appId)
                .map(app -> {
                    if (isValidSlackConfiguration(app)) {
                        String channelId = app.getIntegrationKeyMap().get(SLACK).get(Constants.channelId).toString();
                        String token = cryptoService.decrypt(getSlackTokenForThisChannel(app.getOrganisation()));
                        sendReportToSlackAsync(report, channelId, token); // Send asynchronously
                        return true;
                    } else {
                        throw new IllegalStateException("SLACK channels not properly configured for application!");
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("Application not found for ID: " + appId));
    }

    private boolean isValidSlackConfiguration(QuashClientApplication application) {
        return application.getIntegrationKeyMap() != null && application.getIntegrationKeyMap().containsKey(SLACK);
    }

    private void sendReportToSlackAsync(Report report, String channel, String token) {
        CompletableFuture.runAsync(() -> {
            try {
                sendReportToSlack(report, channel, token);
            } catch (IOException e) {
                LOGGER.error("An error occurred: ", e);
            }
        });
    }

    private String getSlackTokenForThisChannel(Organisation organisation) {
        return (String) integrationRepository.findByOrganisationAndIntegrationType(organisation, SLACK)
                .get()
                .getSettings()
                .get(encryptedAccessToken);
    }

    public void sendReportToSlack(Report report, String channel, String token) throws IOException {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json; charset=utf-8")
                .build();

        List<Map<String, Object>> blocks = new ArrayList<>();

        // Header for the report
        blocks.add(createSectionBlock("*Bug Reported on Quash*"));
        blocks.add(createDividerBlock()); // Adds a horizontal line
        // Report Details
        blocks.add(createContextBlock("Date & Time: " + report.getCreatedAt()));
        blocks.add(createContextBlock("Device Information: " + buildDeviceInfo(report.getDeviceMetadata())));

        blocks.add(createDividerBlock());
        blocks.add(createSectionBlock(report.getTitle()));
        if (report.getDescription() != null) {
            blocks.add(createSectionBlock("> " + report.getDescription()));
        }
        blocks.add(createDividerBlock()); // Adds another horizontal line

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channel);
        payload.put("blocks", blocks);

        ResponseEntity<Map> initialResponse = webClient.post()
                .uri("/chat.postMessage")
                .bodyValue(payload)
                .retrieve()
                .toEntity(Map.class)
                .block();

        LOGGER.info("Report sent to channel: {}", channel);
        String threadTs = (String) initialResponse.getBody().get("ts");

        // Handle media
        if (report.getListOfMedia() != null) {
            bugMediaUploadToSlack(report, channel, threadTs, token, webClient);
        }

        // Handle crash logs
        if (report.getCrashLog() != null && report.getCrashLog().getMediaRef() != null) {
            crashLogUploadToSlack(report, channel, threadTs, token, webClient);
        }
    }

    public Map<String, Object> createTextBlock(String message) {
        Map<String, Object> textBlockMap = new HashMap<>();
        textBlockMap.put(type, section);
        Map<String, String> textContentMap = new HashMap<>();
        textContentMap.put(type, mrkdwn);
        textContentMap.put(text, message);
        textBlockMap.put(text, textContentMap);
        return textBlockMap;
    }

    private String buildDeviceInfo(DeviceMetadata deviceMetadata) {
        return "Device Information:" +
                "\n- Device: " + deviceMetadata.getDevice() +
                "\n- OS: " + deviceMetadata.getOs() +
                "\n- Network: " + deviceMetadata.getNetworkType() +
                "\n- Battery: " + deviceMetadata.getBatteryLevel();
    }

    // Create a divider block
    private Map<String, Object> createDividerBlock() {
        Map<String, Object> block = new HashMap<>();
        block.put(type, divider);
        return block;
    }

    // Create a context block
    private Map<String, Object> createContextBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put(type, context);
        List<Map<String, String>> elements = new ArrayList<>();
        elements.add(Map.of(type, mrkdwn, Constants.text, text));
        block.put(Constants.elements, elements);
        return block;
    }

    public boolean revokeSlackAuthToken(String accessToken) throws Exception {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        ResponseEntity<Map> response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/auth.revoke")
                        .build())
                .retrieve()
                .toEntity(Map.class)
                .block();

        if (response != null && response.getBody() != null && response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody.containsKey("ok") && (Boolean) responseBody.get("ok")) {
                return (Boolean) responseBody.get("revoked");
            }
        }
        return false;
    }

    private void bugMediaUploadToSlack(Report report, String channel, String threadTs, String token, WebClient webClient) throws IOException {
        for (BugMedia media : report.getListOfMedia()) {
            if (media.getMediaRef() != null) {
                String mediaUrl = storageService.generateSignedUrl(media.getMediaRef());
                byte[] mediaBytes = downloadMedia(mediaUrl);
                String slackFileUrl = uploadFileToSlack(mediaBytes, media.getMediaRef(), token);
                Map<String, Object> threadedMediaPayload = new HashMap<>();
                threadedMediaPayload.put("channel", channel);
                threadedMediaPayload.put("thread_ts", threadTs);
                Map<String, Object> textBlock = createTextBlock(slackFileUrl);
                threadedMediaPayload.put("blocks", Collections.singletonList(textBlock));
                ResponseEntity<Map> mediaResponse = webClient.post()
                        .uri("/chat.postMessage")
                        .bodyValue(threadedMediaPayload)
                        .retrieve()
                        .toEntity(Map.class)
                        .block();
                System.out.println("Response From Slack: " + mediaResponse.getBody().toString());
                LOGGER.info("Bug media uploaded to SLACK - Response from slack:{}", mediaResponse.getStatusCode().toString());
            }
        }
    }

    private void crashLogUploadToSlack(Report report, String channel, String threadTs, String token, WebClient webClient) throws IOException {
        String crashLogUrl = storageService.generateSignedUrl(report.getCrashLog().getMediaRef());
        byte[] crashLogBytes = downloadMedia(crashLogUrl);
        String slackCrashLogUrl = uploadFileToSlack(crashLogBytes, report.getCrashLog().getMediaRef(), token);
        Map<String, Object> threadedCrashLogPayload = new HashMap<>();
        threadedCrashLogPayload.put("channel", channel);
        threadedCrashLogPayload.put("thread_ts", threadTs);
        Map<String, Object> textBlock = createTextBlock(slackCrashLogUrl);
        threadedCrashLogPayload.put("blocks", Collections.singletonList(textBlock));
        ResponseEntity<Map> crashResponse = webClient.post()
                .uri("/chat.postMessage")
                .bodyValue(threadedCrashLogPayload)
                .retrieve()
                .toEntity(Map.class)
                .block();

        System.out.println("Response From Slack :" + crashResponse.getBody().toString());
        LOGGER.info("Crash log uploaded to SLACK - Response From Slack : {}", crashResponse.getBody().toString());
    }

    public void sendMessageToSlackChannel(String message, String channel, String accessToken) throws Exception {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("channel", channel);
        formData.add(text, message);

        ResponseEntity<Map> response = webClient.post()
                .uri("/chat.postMessage")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toEntity(Map.class)
                .block();

        assert response != null;
        if (response.getStatusCode() != HttpStatus.OK) {
            // Handle the error
            System.out.println("Failed to send message: " + response.getStatusCode());
            throw new Exception("Failed to send message");
        }
    }

    public CompletableFuture<Void> asyncEventProcessor(JsonNode eventCallback) {
        return CompletableFuture.runAsync(() -> {
            try {
                JsonNode event = eventCallback.get("event");
                String eventType = event.get(type).asText();
                String eventCacheKey = generateCacheKey(eventType, event.get("channel").asText());

                Boolean existingCacheKey = eventCache.asMap().putIfAbsent(eventCacheKey, Boolean.TRUE);
                if (existingCacheKey != null) {
                    LOGGER.info("Duplicate event - {} for channel - {}", eventType, event.get("channel").asText());
                    return;
                }
                eventCache.put(eventCacheKey, Boolean.TRUE);

                String teamId = eventCallback.has("team_id") ? eventCallback.get("team_id").asText() : event.get(team).asText();

                Integration slackIntegration = getSlackIntegrationFromTeamId(teamId);
                LinkedHashMap<String, Object> settings = slackIntegration.getSettings();
                var encryptedAccessToken = settings.get(Constants.encryptedAccessToken).toString();
                var botAccessToken = cryptoService.decrypt(encryptedAccessToken);
                var botUserId = settings.get(userId).toString();
                LOGGER.info("Slack Integration fetched successfully");

                switch (eventType) {
                    case member_joined_channel:
                        handleMemberJoinedChannel(event, botAccessToken, botUserId, slackIntegration);
                        break;
                    case channel_left:
                    case group_left:
                        handleChannelOrGroupLeft(eventCallback, botAccessToken, botUserId, slackIntegration);
                        break;
                    default:
                        throw new Exception("Not a membership event for Quash Messenger");
                }
            } catch (Exception e) {
                LOGGER.error("An error occurred: {}", e.getMessage());
            }
        });
    }

    private String generateCacheKey(String eventType, String channel) {
        return eventType + "_" + channel;
    }

    private void handleMemberJoinedChannel(JsonNode event, String botAccessToken, String botUserId, Integration slackIntegration) throws Exception {
        String invitedUserId = event.get("user").asText();
        if (invitedUserId.equals(botUserId)) {
            String channelId = event.get("channel").asText();
            updateSlackIntegrationChannels(slackIntegration, channelId, botAccessToken);
            LOGGER.info("User: {} was added to channel: {}", invitedUserId, channelId);
        } else {
            throw new Exception("Event not for Heimdall");
        }
    }

    private void handleChannelOrGroupLeft(JsonNode eventCallback, String botAccessToken, String botUserId, Integration slackIntegration) throws Exception {
        Optional<String> userLeftOptional = getUserLeft(eventCallback);
        if (userLeftOptional.isPresent()) {
            String userLeftId = userLeftOptional.get();
            if (userLeftId.equals(botUserId)) {
                String channelId = eventCallback.get("event").get("channel").asText();
                updateSlackIntegrationToRemoveChannel(slackIntegration, channelId);
                LOGGER.info("User: {} left from channel: {}", userLeftId, channelId);
            }
        } else {
            // Handle case or log when no user ID is found in authorizations
            throw new Exception("User to be removed not found");
        }
    }

    private Optional<String> getUserLeft(JsonNode eventCallback) throws Exception {
        JsonNode authorizations = eventCallback.get("authorizations");
        if (authorizations == null || !authorizations.isArray() || authorizations.isEmpty()) {
            throw new Exception("Authorizations body is empty or not in expected format");
        }

        for (JsonNode authorization : authorizations) {
            JsonNode userIdNode = authorization.get("user_id");
            if (userIdNode != null && userIdNode.isTextual()) {
                LOGGER.info("User to be removed: {}", userIdNode.asText());
                return Optional.of(userIdNode.asText());
            }
        }
        return Optional.empty();
    }

    private Map<String, Object> createSectionBlock(String textContent) {
        Map<String, Object> section = new HashMap<>();
        section.put(type, Constants.section);
        Map<String, String> text = new HashMap<>();
        text.put(type, mrkdwn);
        text.put(Constants.text, textContent);
        section.put(Constants.text, text);
        return section;
    }

    public String uploadFileToSlack(byte[] fileContent, String filename, String token) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", fileContent)
                .filename(filename)
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);

        ResponseEntity<Map> response = webClient.post()
                .uri("/files.upload")
                .contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .toEntity(Map.class)
                .block();

        if (response != null && response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("ok"))) {
            Map fileMap = (Map) response.getBody().get("file");
            return (String) fileMap.get(permalink);  // or use 'url_private' based on your needs
        }

        return null;  // Handle error scenarios appropriately
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


    public List<SlackChannelsDTO> getSlackChannelsForOrganisation(User user) {
        try {
            // 1. Fetch the organisation associated with the user
            var teamMember = teamMemberRepository.findByUser(user);
            var organisation = teamMember.getOrganisation();

            // 2. Ensure the organisation has a "SLACK" integration
            var slackIntegrationOpt = integrationRepository.findByOrganisationAndIntegrationType(organisation, "SLACK");
            if (slackIntegrationOpt.isEmpty()) {
                throw new Exception("No SLACK integrations found for the organisation");
            }
            // 3. Fetch the channels from the SLACK integration settings
            var slackIntegration = slackIntegrationOpt.get();
            ArrayList<LinkedHashMap<String, Object>> channels = (ArrayList) slackIntegration.getSettings().get("channels");

            if (channels == null) {
                throw new Exception("No channels found for the SLACK integration");
            }

            return channels.stream().map(channelMap ->
                    SlackChannelsDTO.builder()
                            .channelId((String) channelMap.get("id"))
                            .channelName((String) channelMap.get("name"))
                            .build()
            ).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
package com.quashbugs.quash.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quashbugs.quash.dto.integration.SlackEventVerificationResponseDTO;
import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.ReportsService;
import com.quashbugs.quash.service.SlackIntegrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import static com.quashbugs.quash.constants.Constants.type;

@RestController
@RequestMapping("/api/integrations/slack")
@SecurityRequirement(name = "jwtAuth")
public class SlackController {

    private final SlackIntegrationService slackService;

    private final ReportsService reportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackController.class);

    @Autowired
    public SlackController(
            SlackIntegrationService slackService,
            ReportsService reportService) {
        this.slackService = slackService;
        this.reportService = reportService;
    }

    /**
     * This method is used to initiate the OAuth process for integrating with Slack. It returns the Slack OAuth URL that
     * the user needs to visit in order to authorize the integration.
     *
     * @return ResponseEntity<ResponseDTO> The response entity containing the Slack OAuth URL and a success message.
     */
    @GetMapping("/auth")
    public ResponseEntity<ResponseDTO> initiateOAuth() {
        String slackOAuthUrl = slackService.createSlackOAuthURL();

        return new ResponseEntity<>(new ResponseDTO(true, "Slack integration initiated", slackOAuthUrl), HttpStatus.CREATED);
    }

    /**
     * This method handles the callback from the Slack OAuth process and creates or updates the Slack integration for
     * the authenticated user.
     *
     * @param authentication An object representing the authenticated user.
     * @param code           The authorization code received from Slack during the OAuth process.
     * @return A response entity containing a ResponseDTO indicating the result of the Slack integration callback.
     */
    @PostMapping("/oauth/callback")
    public ResponseEntity<ResponseDTO> slackOAuthCallback(Authentication authentication, @RequestParam("code") String code) {
        try {
            User user = (User) authentication.getPrincipal();
            slackService.createOrUpdateSlackIntegration(user, code);
            return new ResponseEntity<>(new ResponseDTO(true, "Slack integration successful", null), HttpStatus.CREATED);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return new ResponseEntity<>(new ResponseDTO(false, "Access token expired or invalid. Please re-authenticate with Slack.", null), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to create Slack integration", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method handles Slack events by verifying the event type and processing it accordingly. It returns a response
     * entity with the appropriate status and message.
     *
     * @param slackEventJson The JSON object representing the Slack event.
     * @return A response entity with the appropriate status and message.
     */
    @PostMapping(value = "/events")
    public ResponseEntity<?> slackEventHandling(@RequestBody JsonNode slackEventJson) {
        try {
            String slackEvent = slackEventJson.get(type).asText();
            if (slackEvent.equals("url_verification")) {
                // If the event type is "url_verification", create a SlackEventVerificationResponseDTO object
                // with the challenge value from the slackEventJson object and return it in a response entity with status OK.
                return new ResponseEntity<>(new SlackEventVerificationResponseDTO(slackEventJson.get("challenge").asText()), HttpStatus.OK);
            } else {
                slackService.asyncEventProcessor(slackEventJson)
                        .thenAccept(result -> {
                            // Log the result or handle success
                            LOGGER.info("Event handled");
                        })
                        .exceptionally(ex -> {
                            // Handle exceptions
                            LOGGER.error("Error processing Slack event", ex);
                            return null;
                        });
            }
            return new ResponseEntity<>(new ResponseDTO(true, "Event received", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to process event", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * The `exportSlackIssues` method in the `IntegrationController` class exports issues to Slack by calling the
     * `exportIssuesToSlack` method of the `reportService` object.
     *
     * @param issues The request body containing the issues to be exported to Slack.
     * @return A response entity containing a response DTO with a success or error message and the result of the export.
     */
    @PostMapping("/export-issues")
    public ResponseEntity<ResponseDTO> exportSlackIssues(@RequestBody IssuesRequestBodyDTO issues) {
        try {
            var result = reportService.exportIssuesToSlack(issues);
            return new ResponseEntity<>(new ResponseDTO(true, "All issues exported successfully", result), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves the Slack channel integrations for a given user's organisation.
     *
     * @param authentication The authentication object containing the user's credentials.
     * @return The response entity containing the success status, message, and the retrieved Slack channel integrations.
     */
    @GetMapping("/fetch-channels")
    public ResponseEntity<ResponseDTO> getSlackIntegrations(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var response = slackService.getSlackChannelsForOrganisation(user);
            return ResponseEntity.ok(new ResponseDTO(true, "Slack Channels Fetched Successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(false, e.getMessage(), null));
        }
    }
}
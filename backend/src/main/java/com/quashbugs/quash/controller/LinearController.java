package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.IntegrationService;
import com.quashbugs.quash.service.LinearIntegrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/integrations/linear")
@SecurityRequirement(name = "jwtAuth")
public class LinearController {

    private final LinearIntegrationService linearIntegrationService;

    private final IntegrationService integrationService;

    @Autowired
    public LinearController(LinearIntegrationService linearIntegrationService,
                            IntegrationService integrationService) {
        this.linearIntegrationService = linearIntegrationService;
        this.integrationService = integrationService;
    }
    /**
     * This method is used to initiate the OAuth process for integrating with Linear. It returns the Linear OAuth URL that
     * the user needs to visit in order to authorize the integration.
     *
     * @return ResponseEntity<ResponseDTO> The response entity containing the Linear OAuth URL and a success message.
     */
    @GetMapping("/auth")
    public ResponseEntity<ResponseDTO> initiateOAuth(Authentication authentication) {
        String linearOAuthUrl = linearIntegrationService.createLinearOAuthURL();
        return new ResponseEntity<>(new ResponseDTO(true, "Linear integration initiated", linearOAuthUrl), HttpStatus.CREATED);
    }

    /**
     * The `linearOAuthCallback` method is responsible for handling the callback from the Linear OAuth process.
     * It creates a Linear integration for the authenticated user if one does not already exist and returns the integration details in the response.
     *
     * @param authentication The authentication object containing the details of the authenticated user.
     * @param code           The authorization code received from the Linear OAuth process.
     * @return ResponseEntity<ResponseDTO> The response containing the result of the Linear integration callback.
     * The response will have a `true` status if the integration was successful, along with the integration details.
     * If there was an error, the response will have a `false` status and an error message.
     */
    @PostMapping("/oauth/callback")
    public ResponseEntity<ResponseDTO> linearOAuthCallback(Authentication authentication, @RequestParam("code") String code) {
        try {
            User user = (User) authentication.getPrincipal();
            var integration = integrationService.getByUserAndIntegrationType(user, "LINEAR");
            if (integration == null) {
                integration = linearIntegrationService.createLinearIntegration(user, code);
            }
            return new ResponseEntity<>(new ResponseDTO(true, "Linear integration successful", integration), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to create Linear integration", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves the projects associated with the Linear integration for the authenticated user.
     *
     * @param authentication The authentication object representing the currently authenticated user.
     * @return The response entity containing the projects retrieved from the Linear integration.
     */
    @GetMapping("/get-projects")
    public ResponseEntity<ResponseDTO> getLinearProjects(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var projects = linearIntegrationService.getLinearIntegrationProjects(user);
            return new ResponseEntity<>(new ResponseDTO(true, "Linear integration projects", projects), HttpStatus.CREATED);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return new ResponseEntity<>(new ResponseDTO(false, "Access token expired or invalid. Please re-authenticate with Linear.", null), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to get Linear integration projects", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Exports Linear issues to an external system.
     *
     * @param authentication The authentication object representing the user.
     * @param requestBody    The request body containing export details.
     * @return ResponseEntity with a ResponseDTO indicating the result of issue export.
     */
    @PostMapping("/export-issues")
    public ResponseEntity<ResponseDTO> exportLinearIssues(Authentication authentication, @RequestBody IssuesRequestBodyDTO requestBody) {
        try {
            User user = (User) authentication.getPrincipal();
            var createdIssues = linearIntegrationService.exportLinearIssues(user, requestBody);
            return new ResponseEntity<>(new ResponseDTO(true, "All issues exported successfully", createdIssues), HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return new ResponseEntity<>(new ResponseDTO(false, "Access token expired or invalid. Please re-authenticate with Linear.", null), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.GithubIntegrationService;
import com.quashbugs.quash.service.IntegrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/integrations/github")
@SecurityRequirement(name = "jwtAuth")
public class GithubController {

    private final GithubIntegrationService githubIntegrationService;

    private final IntegrationService integrationService;

    @Autowired
    public GithubController(GithubIntegrationService githubIntegrationService,
                            IntegrationService integrationService) {
        this.githubIntegrationService = githubIntegrationService;
        this.integrationService = integrationService;
    }

    /**
     * Initiates the authentication process for integrating with GitHub.
     * Returns the GitHub OAuth URL that the user needs to visit in order to authorize the integration.
     *
     * @return ResponseEntity<ResponseDTO> - A response entity containing a response DTO with the success status, message, and the GitHub OAuth URL.
     */
    @GetMapping("/auth")
    public ResponseEntity<ResponseDTO> initiateGithubAuth() {
        String githubOuthUrl = githubIntegrationService.createGithubOAuthURL();
        return new ResponseEntity<>(new ResponseDTO(true, "Github Integration Intitated", githubOuthUrl), HttpStatus.CREATED);
    }

    /**
     * Handles the callback from the GitHub OAuth process. Retrieves the authenticated user, checks if a GitHub integration
     * already exists for the user, and creates a new integration if it doesn't exist. Returns the integration details in the response.
     *
     * @param authentication The authenticated user object.
     * @param code           The authorization code received from GitHub.
     * @return A response entity containing the integration details in the ResponseDTO object.
     */
    @PostMapping("/oauth/callback")
    public ResponseEntity<ResponseDTO> handleGithubCallback(Authentication authentication, @RequestParam("code") String code) {
        try {
            User user = (User) authentication.getPrincipal();
            var integration = integrationService.getByUserAndIntegrationType(user, "GITHUB");
            if (integration == null) {
                integration = githubIntegrationService.createGitHubIntegration(user, code);
            }
            return new ResponseEntity<>(new ResponseDTO(true, "Github integration successful", integration), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to create Github integration", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves all repositories from GitHub for the authenticated user.
     *
     * @param authentication The authentication object representing the currently authenticated user.
     * @return A response entity containing the fetched repositories.
     */
    @GetMapping("/get-repositories")
    public ResponseEntity<ResponseDTO> getAllRepositories(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var repositories = githubIntegrationService.listRepositories(user);
            return new ResponseEntity<>(new ResponseDTO(true, "Respositories fetched successfully", repositories), HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return new ResponseEntity<>(new ResponseDTO(false, "Access token expired or invalid. Please re-authenticate with GitHub.", null), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all repositories from GitHub for the authenticated user.
     *
     * @param authentication The authentication object representing the currently authenticated user.
     * @param requestBody    The IssuesRequestBody object representing the current IssuesRequestBody.
     * @return A response entity containing the fetched repositories.
     */
    @PostMapping("/export-issues")
    public ResponseEntity<ResponseDTO> exportGithubIssues(Authentication authentication, @RequestBody IssuesRequestBodyDTO requestBody) {
        try {
            User user = (User) authentication.getPrincipal();
            var createdIssues = githubIntegrationService.exportGithubIssues(user, requestBody);
            return new ResponseEntity<>(new ResponseDTO(true, "All issues exported successfully", createdIssues), HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return new ResponseEntity<>(new ResponseDTO(false, "Access token expired or invalid. Please re-authenticate with GitHub.", null), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.JiraIntegrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations/jira")
@SecurityRequirement(name = "jwtAuth")
public class JiraController {

    private final JiraIntegrationService jiraIntegrationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraController.class);

    @Autowired
    public JiraController(JiraIntegrationService jiraIntegrationService) {
        this.jiraIntegrationService = jiraIntegrationService;
    }

    /**
     * Handles the callback for Jira integration authentication.
     *
     * @param authentication The authentication object representing the user.
     * @param code           The authentication code received from Jira.
     * @return ResponseEntity with a ResponseDTO indicating the result of Jira integration.
     */
    @GetMapping("/callback")
    public ResponseEntity<ResponseDTO> jiraIntegrationCallback(Authentication authentication, @RequestParam("code") String code) {
        try {
            User user = (User) authentication.getPrincipal();
            var integration = jiraIntegrationService.getAccessFromRefreshToken(code, user);
            LOGGER.info("Access from refresh token done");
            jiraIntegrationService.setJiraCloudId(user);
            LOGGER.info("Access from refresh token done");
            return new ResponseEntity<>(new ResponseDTO(true, "Jira integration successful ", integration), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves Jira users and adds them to the integration.
     *
     * @param authentication The authentication object representing the user.
     * @return ResponseEntity with a ResponseDTO containing the Jira users.
     */
    @GetMapping("/get-users")
    public ResponseEntity<ResponseDTO> addJiraUsers(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var usersResponse = jiraIntegrationService.getJiraUsers(user);
            jiraIntegrationService.createJiraUsers(user, usersResponse);
            Map<String, Object> map = new HashMap<>();
            map.put("users", usersResponse);
            return new ResponseEntity<>(new ResponseDTO(true, "All users fetched successfully", map), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves Jira projects.
     *
     * @param authentication The authentication object representing the user.
     * @return ResponseEntity with a ResponseDTO containing the Jira projects.
     */

    @GetMapping("/get-projects")
    public ResponseEntity<ResponseDTO> getJiraProjects(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var projectsResponse = jiraIntegrationService.getJiraProjects(user);
            Map<String, Object> map = new HashMap<>();
            map.put("projects", projectsResponse);
            return new ResponseEntity<>(new ResponseDTO(true, "All projects fetched successfully", map), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Exports Jira issues to an external system.
     *
     * @param authentication The authentication object representing the user.
     * @param requestBody    The request body containing export details.
     * @return ResponseEntity with a ResponseDTO indicating the result of issue export.
     */
    @PostMapping("/export-issues")
    public ResponseEntity<ResponseDTO> exportJiraIssues(Authentication authentication, @RequestBody IssuesRequestBodyDTO requestBody) {
        try {
            User user = (User) authentication.getPrincipal();
            var createdIssues = jiraIntegrationService.exportJiraIssues(user, requestBody);
            return new ResponseEntity<>(new ResponseDTO(true, "All issues exported successfully", createdIssues), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
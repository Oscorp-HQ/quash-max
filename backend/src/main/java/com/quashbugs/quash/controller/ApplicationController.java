package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.miscellaneous.UserListDetailsDTO;
import com.quashbugs.quash.dto.request.ApplicationRequestBodyDTO;
import com.quashbugs.quash.dto.response.ApplicationResponseDTO;
import com.quashbugs.quash.dto.response.MemberResponseDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.QuashClientApplication;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.ApplicationRepository;
import com.quashbugs.quash.repo.OrganisationRepository;
import com.quashbugs.quash.service.ApplicationService;
import com.quashbugs.quash.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.quashbugs.quash.constants.Constants.VERIFIED;

@RestController
@RequestMapping("/api/app")
@SecurityRequirement(name = "jwtAuth")
public class ApplicationController {

    private final OrganisationRepository organisationRepository;

    private final ApplicationRepository appRepository;

    private final ApplicationService applicationService;

    private final DashboardService dashboardService;

    @Autowired
    public ApplicationController(
            OrganisationRepository organisationRepository,
            ApplicationRepository appRepository,
            ApplicationService applicationService,
            DashboardService dashboardService) {
        this.organisationRepository = organisationRepository;
        this.appRepository = appRepository;
        this.applicationService = applicationService;
        this.dashboardService = dashboardService;
    }

    /**
     * Deletes an application and its associated data.
     *
     * @param appId          The ID of the application to be deleted.
     * @param authentication The user's authentication object.
     * @return The response entity containing the result of the deletion operation.
     */
    @DeleteMapping("/{appId}")
    public ResponseEntity<ResponseDTO> deleteApp(@PathVariable String appId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            applicationService.deleteApp(appId, user);
            return ResponseEntity.ok(new ResponseDTO(true, "App and associated data deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(false, "Error while deleting report: " + e.getMessage(), null));
        }
    }

    /**
     * Registers a new application.
     *
     * @param requestBody The request body containing application information.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @PostMapping("/register-app")
    public ResponseEntity<ResponseDTO> registerApp(@RequestBody ApplicationRequestBodyDTO requestBody) {
        try {
            var app = applicationService.registerApp(requestBody);
            ApplicationResponseDTO appResponse = new ApplicationResponseDTO(app.getId(), app.getPackageName(), app.getAppType(), app.getAppName(), app.getRegistrationToken(), app.getIntegrationKeyMap());
            return ResponseEntity.ok(new ResponseDTO(true, "Application registered successfully.", appResponse));
        } catch (InvalidKeyException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "An error occurred while processing the request." + e.getMessage(), null));
        }
    }

    /**
     * Verifies an application for a given organization.
     *
     * @param orgToken       The organization's unique token.
     * @param authentication The authentication object containing user information.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @GetMapping("/verify-app")
    public ResponseEntity<ResponseDTO> verifyApp(@RequestParam String orgToken, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Organisation> organisationOpt = organisationRepository.findOptionalByOrgUniqueKey(orgToken);
            if (organisationOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(false, "Invalid organization token.", null));
            }
            Organisation org = organisationOpt.get();

            // Check if the authenticated user is part of the organisation
            if (!applicationService.isUserPartOfOrganisation(user, org)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDTO(false, "User is not part of the organization or does not have the required permissions.", null));
            }
            List<QuashClientApplication> apps = appRepository.findAllByOrganisation(org);
            if (!apps.isEmpty()) {
                apps.forEach(app -> app.setAppStatus(VERIFIED));
                return ResponseEntity.ok(new ResponseDTO(true, "Apps are registered and verified for the organization.", apps));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(false, "No apps are linked to this organization.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "An error occurred while processing the request.", null));
        }
    }

    /**
     * Sets project keys for different integrations in an application.
     *
     * @param projectKeyMapBody A list of maps where each map contains the application ID and integration type.
     * @return A response entity containing the result of setting the project keys.
     */
    @PostMapping("/set-project-key")
    public ResponseEntity<ResponseDTO> setProjectKey(@RequestBody List<Map<String, String>> projectKeyMapBody) {
        try {
            applicationService.setProjectKeys(projectKeyMapBody);
            return ResponseEntity.ok(new ResponseDTO(true, "Project Keys set successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "An error occurred while processing the request.", null));
        }
    }

    /**
     * Retrieves organization users for a given organization unique key.
     *
     * @return ResponseEntity with a ResponseDTO containing organization user details.
     */
    @GetMapping("/users")
    public ResponseEntity<ResponseDTO> getOrgUsers(Authentication authentication) {
        try {
            Organisation organisation = applicationService.getOrganisationFromObject(authentication.getPrincipal());
            if (organisation != null) {
                List<MemberResponseDTO> memberResponsDTOS = dashboardService.fetchOrgMembersForOrg(organisation);
                UserListDetailsDTO data = new UserListDetailsDTO();
                data.setOrganisationName(organisation.getName());
                data.setOrganisationUsers(memberResponsDTOS);
                return new ResponseEntity<>(new ResponseDTO(true, "Data fetched successfully", data), HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(false, "No Organisation found for the given token", null));
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred while processing the request", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
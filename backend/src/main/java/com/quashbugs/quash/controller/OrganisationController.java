package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.OrganisationSignUpRequestDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.EmailService;
import com.quashbugs.quash.service.OrganisationService;
import com.quashbugs.quash.service.TeamMemberService;
import com.quashbugs.quash.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organisations")
@SecurityRequirement(name = "jwtAuth")
public class OrganisationController {

    private final OrganisationService organisationService;

    private final UserService userService;

    private final TeamMemberService teamMemberService;

    private final EmailService emailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationController.class);

    @Autowired
    public OrganisationController(
            OrganisationService organisationService,
            UserService userService,
            TeamMemberService teamMemberService, EmailService emailService) {
        this.organisationService = organisationService;
        this.userService = userService;
        this.teamMemberService = teamMemberService;
        this.emailService = emailService;
    }

    /**
     * Creates a new organisation based on the provided request and associates it with the authenticated user.
     *
     * @param authentication The authentication object representing the user.
     * @param request        The organisation sign-up request containing organisation details.
     * @return ResponseEntity with a ResponseDTO indicating the result of organisation creation.
     */
    @PostMapping()
    public ResponseEntity<ResponseDTO> createOrganisation(Authentication authentication, @RequestBody OrganisationSignUpRequestDTO request) { // Updated parameter type
        try {
            User user = (User) authentication.getPrincipal();
            if (user.isEmailVerified()) {
                userService.updateUsersWithOrganisationRole(user, request);
                if (organisationService.getOrganisationByName(request.getOrganisationName()).isPresent()) {
                    return new ResponseEntity<>(new ResponseDTO(false, "An organisation with this name already exists", null), HttpStatus.BAD_REQUEST);
                } else {
                    var organisation = organisationService.createOrganisation(request, user);
                    teamMemberService.addAdminUserToTeam(organisation, user, request.getPhoneNumber());
                    emailService.sendWelcomeEmail(user);
                    LOGGER.info("Welcome mail sent");
                    return new ResponseEntity<>(new ResponseDTO(true, "Organisation was created successfully", organisation), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "Please verify your email address.", null), HttpStatus.BAD_REQUEST);
            }

        } catch (NullPointerException ex) {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not found", null), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a list of all organisations.
     *
     * @return ResponseEntity with a ResponseDTO containing the list of organisations.
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO> getAllOrganisations() { // Updated method name
        try {
            List<Organisation> organisations = organisationService.getAllOrganisations(); // Updated method name
            return new ResponseEntity<>(new ResponseDTO(true, "Organisations retrieved successfully", organisations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to fetch organisations:" + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves details of the organisation associated with the authenticated user.
     *
     * @param authentication The authentication object representing the user.
     * @return ResponseEntity with a ResponseDTO containing the organisation details.
     */
    @GetMapping()
    public ResponseEntity<ResponseDTO> getOrganisation(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            try {
                var organisation = organisationService.getOrganisation(user);
                return new ResponseEntity<>(new ResponseDTO(true, "Organisation details retrieved successfully", organisation), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(new ResponseDTO(false, "Organisation not found for this user: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
            }
        } catch (NullPointerException ex) {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not authenticated", null), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes the organisation associated with the authenticated user.
     *
     * @param authentication The authentication object representing the user.
     * @return ResponseEntity with a ResponseDTO indicating the success of the deletion.
     */
    @DeleteMapping()
    public ResponseEntity<ResponseDTO> deleteOrganisation(Authentication authentication) { // Updated method name
        try {
            User user = (User) authentication.getPrincipal();
            organisationService.deleteOrganisation(user); // Updated method name
            return new ResponseEntity<>(new ResponseDTO(true, "Organisation deleted successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to fetch organisations: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }
}
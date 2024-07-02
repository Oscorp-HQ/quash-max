package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.IntegrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations")
@SecurityRequirement(name = "jwtAuth")
public class IntegrationController {

    private final IntegrationService integrationService;

    @Autowired
    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /**
     * Retrieves all integrations associated with the authenticated user.
     *
     * @param authentication The authentication object representing the user.
     * @return ResponseEntity with a ResponseDTO containing the list of integrations.
     */
    @GetMapping()
    public ResponseEntity<ResponseDTO> getAllIntegrations(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var integrations = integrationService.getAllIntegrations(user);
            return new ResponseEntity<>(new ResponseDTO(true, "Integrations fetched successfully", integrations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes an integration associated with the authenticated user.
     *
     * @param authentication The authentication object representing the user.
     * @param integrationId  The ID of the integration to be deleted.
     * @return ResponseEntity with a ResponseDTO indicating the result of integration deletion.
     */
    @DeleteMapping("")
    public ResponseEntity<ResponseDTO> deleteIntegration(Authentication authentication, @RequestParam("integrationId") String integrationId) {
        try {
            User user = (User) authentication.getPrincipal();
            integrationService.deleteIntegration(user, integrationId);
            return new ResponseEntity<>(new ResponseDTO(true, "Integration was deleted successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
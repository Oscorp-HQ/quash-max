package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.response.DashboardResponseDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.TeamMemberRepository;
import com.quashbugs.quash.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "jwtAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public DashboardController(DashboardService dashboardService, TeamMemberRepository teamMemberRepository) {
        this.dashboardService = dashboardService;
        this.teamMemberRepository = teamMemberRepository;
    }

    /**
     * Retrieves dashboard data for the user's organization.
     *
     * @param authentication The authentication object representing the user.
     * @return ResponseEntity with a ResponseDTO containing the dashboard data.
     */
    @GetMapping("/organisation")
    public ResponseEntity<ResponseDTO> getDashboardData(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var org = teamMemberRepository.findByUser(user).getOrganisation();
            if (org == null) {
                return new ResponseEntity<>(new ResponseDTO(false, "Unauthorized access", null), HttpStatus.UNAUTHORIZED);
            }
            DashboardResponseDTO data = dashboardService.fetchDashboardDataForOrg(org);
            return new ResponseEntity<>(new ResponseDTO(true, "Data fetched successfully", data), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Error: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
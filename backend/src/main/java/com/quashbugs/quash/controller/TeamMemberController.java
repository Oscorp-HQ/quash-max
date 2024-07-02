package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.InviteMembersRequestBodyDTO;
import com.quashbugs.quash.dto.response.MemberResponseDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.TeamMemberRepository;
import com.quashbugs.quash.service.TeamMemberService;
import com.quashbugs.quash.service.UserService;
import com.quashbugs.quash.service.UtilsService;
import com.quashbugs.quash.util.EmailUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.quashbugs.quash.constants.Constants.ADMIN;

@RestController
@RequestMapping("/api/team-members")
@SecurityRequirement(name = "jwtAuth")
public class TeamMemberController {

    private final TeamMemberRepository teamMemberRepository;

    private final TeamMemberService teamMemberService;

    private final UtilsService utilsService;

    private final UserService userService;

    @Autowired
    public TeamMemberController(
            TeamMemberRepository teamMemberRepository,
            TeamMemberService teamMemberService,
            UtilsService utilsService,
            UserService userService) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamMemberService = teamMemberService;
        this.utilsService = utilsService;
        this.userService = userService;
    }

    /**
     * Invites team members to an organization.
     *
     * @param authentication The user's authentication information.
     * @param requestBody    The request body containing email addresses of team members to invite.
     * @return ResponseEntity with a ResponseDTO indicating the success of the invitation.
     */
    @PostMapping(value = "/invite")
    public ResponseEntity<ResponseDTO> inviteTeamMember(Authentication authentication, @RequestBody InviteMembersRequestBodyDTO requestBody) {
        try {
            User owner = (User) authentication.getPrincipal();

            if (requestBody.getEmailList().isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseDTO(false, "Email list is empty", null));
            }
            if (requestBody.getEmailList().stream().anyMatch(email -> !EmailUtils.isValidEmail(email))) {
                throw new IllegalArgumentException("One or more emails are invalid!");
            }

            if (requestBody.getEmailList().stream().anyMatch(email -> !utilsService.extractDomain(owner.getWorkEmail()).equals(utilsService.extractDomain(email)))) {
                throw new IllegalArgumentException("All emails should have the same domain!");
            }

            if (userService.checkUsersExist(requestBody.getEmailList())) {
                return ResponseEntity.badRequest().body(new ResponseDTO(false, "One or more users already exist.", null));
            }
            TeamMember teamMember = teamMemberRepository.findByUser(owner);
            if (teamMember == null || teamMember.getOrganisation() == null) {
                return ResponseEntity.ok().body(new ResponseDTO(false, "User is not associated with any organisation", null));
            }

            Set<String> existingEmails = teamMemberRepository.findByOrganisation(teamMember.getOrganisation()).stream()
                    .map(tm -> tm.getUser().getWorkEmail())
                    .collect(Collectors.toSet());

            List<String> emailsToInvite = requestBody.getEmailList().stream()
                    .filter(email -> !existingEmails.contains(email))
                    .collect(Collectors.toList());

            var response = teamMemberService.inviteTeamMembers(owner, emailsToInvite);
            ArrayList<MemberResponseDTO> invitedMembers = new ArrayList<MemberResponseDTO>();
            for (TeamMember memberResponse : response) {
                invitedMembers.add(convertToOrgMemberDTO(memberResponse));
            }
            return ResponseEntity.ok().body(new ResponseDTO(true, "Team member invited successfully", invitedMembers));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null));
        }
    }

    /**
     * Converts a TeamMember entity to a MemberResponse DTO.
     *
     * @param member The TeamMember entity to convert.
     * @return A MemberResponse DTO containing member details.
     */
    private MemberResponseDTO convertToOrgMemberDTO(TeamMember member) {
        MemberResponseDTO dto = new MemberResponseDTO();
        dto.setId(member.getUser().getId());
        dto.setTeamMemberId(member.getId());
        dto.setName(member.getUser().getFullName());
        dto.setEmail(member.getUser().getWorkEmail());
        dto.setHasAcceptedInvite(member.isHasAccepted());
        dto.setAdmin(Objects.equals(member.getRole(), ADMIN));
        return dto;
    }

    /**
     * Retrieves the details of the authenticated team member.
     *
     * @param authentication The user's authentication information.
     * @return ResponseEntity with a ResponseDTO containing the team member's details.
     */
    @GetMapping
    public ResponseEntity<ResponseDTO> getTeamMember(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            TeamMember teamMember = teamMemberRepository.findByUser(user);
            return new ResponseEntity<>(new ResponseDTO(true, "Team member details fetched successfully", teamMember), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves details of all team members.
     *
     * @return A list of all team members.
     */
    @GetMapping("/all")
    public List<TeamMember> getAllTeamMembers() {
        return teamMemberRepository.findAll();
    }

    /**
     * Retrieves the details of a specific team member by ID.
     *
     * @param authentication The user's authentication information.
     * @param id             The ID of the team member to retrieve.
     * @return ResponseEntity with a ResponseDTO containing the team member's details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO> getTeamMemberById(Authentication authentication, @PathVariable String id) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(new ResponseDTO(false, "Authentication required", null), HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<TeamMember> teamMember = teamMemberRepository.findById(id);
            return teamMember.map(member -> new ResponseEntity<>(new ResponseDTO(true, "Member details fetched successfully", member), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(new ResponseDTO(false, "Member not found", null), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Unable to fetch member details", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a team member from the organization.
     *
     * @param authentication The user's authentication information.
     * @param id             The ID of the team member to delete.
     * @return ResponseEntity with a ResponseDTO indicating the success of the deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteTeamMember(Authentication authentication, @PathVariable String id) {
        try {
            if (!authentication.isAuthenticated()) {
                return new ResponseEntity<>(new ResponseDTO(false, "Authentication required", null), HttpStatus.UNAUTHORIZED);
            }
            User user = (User) authentication.getPrincipal();
            TeamMember teamMember = teamMemberRepository.findByUser(user);
            if (teamMember != null) {
                if (teamMember.getRole().equals(ADMIN)) {
                    var member = teamMemberRepository.findById(id);
                    if (member.isPresent()) {
                        User teamMemberUser = member.get().getUser();
                        teamMemberRepository.deleteById(id);
                        userService.deleteUser(teamMemberUser);
                    }
                    return new ResponseEntity<>(new ResponseDTO(true, "Member removed from the team", null), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseDTO(false, "Only admins can remove the user", null), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "Team member doesn't exist", null), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "User doesn't exist or an error encountered", null), HttpStatus.BAD_REQUEST);
        }
    }
}
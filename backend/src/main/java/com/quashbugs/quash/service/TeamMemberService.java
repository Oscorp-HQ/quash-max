package com.quashbugs.quash.service;

import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.TeamMemberRepository;
import com.quashbugs.quash.repo.UserRepository;
import static com.quashbugs.quash.constants.Constants.ADMIN;
import static com.quashbugs.quash.constants.Constants.INVITE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    private final UserRepository userRepository;

    private final AuthenticationService authenticationService;

    @Autowired
    public TeamMemberService(TeamMemberRepository teamMemberRepository,
                             UserRepository userRepository,
                             AuthenticationService authenticationService) {
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    public TeamMember createTeamMember(Organisation organisation, User user, String role, String phoneNumber) {
        TeamMember existingTeamMember = teamMemberRepository.findByOrganisationAndUser(organisation, user);
        if (existingTeamMember != null) {
            return existingTeamMember;
        } else {
            user.setShouldNavigateToDashboard(user.getFullName() != null);
            userRepository.save(user);
            TeamMember teamMember = TeamMember.builder()
                    .organisation(organisation)
                    .user(user)
                    .phoneNumber(phoneNumber)
                    .role(role)
                    .joinedAt(new Date())
                    .build();
            teamMemberRepository.save(teamMember);
            return teamMember;
        }
    }

    public List<TeamMember> inviteTeamMembers(User owner, List<String> emailsToInvite) {
        ArrayList<TeamMember> teamMembers = new ArrayList<>();
        var organisation = teamMemberRepository.findByUser(owner).getOrganisation();
        for (String email : emailsToInvite) {
            teamMembers.add(createTeamMember(organisation, createUser(email), "MEMBER", null));
            authenticationService.sendInviteEmail(email, owner);
        }
        return teamMembers;
    }

    private User createUser(String email) {
        var user = User.builder()
                .signUpType(INVITE)
                .createdAt(new Date())
                .workEmail(email)
                .emailVerified(true)
                .shouldNavigateToDashboard(false)
                .build();
        return userRepository.save(user);
    }

    public List<TeamMember> findAllByOrganisation(Organisation organisation) {
        return teamMemberRepository.findByOrganisation(organisation);
    }

    public void saveTeamMember(TeamMember teamMember) {
        teamMemberRepository.save(teamMember);
    }

    public TeamMember findTeamMemberByOrganisation(User user) {
        return teamMemberRepository.findByUser(user);
    }

    public void addAdminUserToTeam(Organisation organisation, User user, String phoneNumber) {
        createTeamMember(organisation, user, ADMIN, phoneNumber);
    }
}
package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.response.ApplicationResponseDTO;
import com.quashbugs.quash.dto.response.DashboardResponseDTO;
import com.quashbugs.quash.dto.response.MemberResponseDTO;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.QuashClientApplication;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.repo.ApplicationRepository;
import com.quashbugs.quash.repo.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.quashbugs.quash.constants.Constants.ADMIN;

@Service
public class DashboardService {

    private final TeamMemberRepository teamMemberRepository;

    private final ApplicationRepository applicationRepository;

    @Autowired
    public DashboardService(TeamMemberRepository teamMemberRepository, ApplicationRepository applicationRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.applicationRepository = applicationRepository;
    }


    public DashboardResponseDTO fetchDashboardDataForOrg(Organisation org) {
        DashboardResponseDTO response = new DashboardResponseDTO();
        if (org != null) {
            response.setOrgId(String.valueOf(org.getId()));
            response.setOrgName(org.getName());
            response.setOrganisationKey(org.getOrgUniqueKey());
            response.setOrgAbb(org.getOrgAbbreviation());
            response.setOrgCreatedAt(org.getCreatedAt());
            List<MemberResponseDTO> members = fetchOrgMembersForOrg(org);
            List<ApplicationResponseDTO> applicationResponseDTO = fetchOrgApps(org);
            response.setOrganisationApps(applicationResponseDTO);
            response.setOrgMembers(members);
        }
        return response;
    }

    private List<ApplicationResponseDTO> fetchOrgApps(Organisation organisation) {
        return applicationRepository.findAllByOrganisation(organisation)
                .stream()
                .map(this::convertToAppDTO)
                .collect(Collectors.toList());
    }

    public List<MemberResponseDTO> fetchOrgMembersForOrg(Organisation org) {
        return teamMemberRepository.findByOrganisation(org)
                .stream()
                .map(this::convertToOrgMemberDTO)
                .collect(Collectors.toList());
    }

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

    private ApplicationResponseDTO convertToAppDTO(QuashClientApplication app) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setAppId(app.getId());
        dto.setAppName(app.getAppName());
        dto.setPackageName(app.getPackageName());
        dto.setAppType(app.getAppType());
        dto.setReportingToken(app.getRegistrationToken());
        dto.setIntegrationKeyMap(app.getIntegrationKeyMap());
        return dto;
    }
}
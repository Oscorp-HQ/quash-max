package com.quashbugs.quash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private String orgId;
    private String orgName;
    private String orgAbb;
    private Date orgCreatedAt;
    private String organisationKey;
    private List<MemberResponseDTO> orgMembers;
    private List<ApplicationResponseDTO> organisationApps;

}
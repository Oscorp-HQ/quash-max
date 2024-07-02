package com.quashbugs.quash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDTO {
    private String id;
    private String name;
    private String email;
    private String teamMemberId;
    private boolean isAdmin;
    private boolean hasAcceptedInvite;

}

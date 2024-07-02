package com.quashbugs.quash.dto.miscellaneous;

import com.quashbugs.quash.dto.response.MemberResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDetailsDTO {

    private String organisationName;
    private List<MemberResponseDTO> organisationUsers;
}

package com.quashbugs.quash.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationSignUpRequestDTO {

    private String fullName;
    private String organisationRole;
    private String organisationName;
    private String phoneNumber;
}

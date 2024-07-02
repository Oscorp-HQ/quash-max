package com.quashbugs.quash.dto.integration;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SlackEventVerificationResponseDTO {

    private String challenge;
}

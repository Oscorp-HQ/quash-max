package com.quashbugs.quash.dto.integration;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackChannelsDTO {

    private String channelId;
    private String channelName;
}

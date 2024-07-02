package com.quashbugs.quash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponseDTO {
    private String appId;
    private String packageName;
    private String appType;
    private String appName;
    private String reportingToken;
    private Map<String, JSONObject> integrationKeyMap;
}
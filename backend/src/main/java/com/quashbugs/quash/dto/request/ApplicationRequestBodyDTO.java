package com.quashbugs.quash.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestBodyDTO {
    private String packageName;
    private String appType;
    private String appName;
    private String version;
    private String orgUniqueKey;
}

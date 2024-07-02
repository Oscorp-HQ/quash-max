package com.quashbugs.quash.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "network-logs")
public class NetworkLog {
    private String reportId;
    private String requestUrl;
    private String requestMethod;
    private Map<String, String> requestHeaders;
    private String requestBody;
    private Integer responseCode;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private Long durationMs;
    private String errorMessage;
    private String exceptionMessage;
    private String exceptionStackTrace;
    private String timeStamp;
}

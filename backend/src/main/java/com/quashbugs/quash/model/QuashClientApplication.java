package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class QuashClientApplication {
    @Id
    private String id;
    private String appName;
    private String packageName;
    private String appType;
    private Date registeredAt;
    private String registrationToken;
    private String refreshRegistrationToken;
    private String appStatus;
    private Map<String, JSONObject> integrationKeyMap;
    @DBRef
    private Organisation organisation;

}

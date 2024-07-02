package com.quashbugs.quash.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.LinkedHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Document(collection = "integrations")
public class Integration {

    @Id
    private String id;
    @DBRef
    private Organisation organisation;
    private String integrationType;
    private LinkedHashMap<String, Object> settings = new LinkedHashMap<>();
    private String integrationRefreshToken;
    private String integrationAccessToken;
    private String integrationCloudId;
    private Date expiryTime;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    // Constructors, getters, setters
}

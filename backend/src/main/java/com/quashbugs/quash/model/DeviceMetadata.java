package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "device_metadata")
public class DeviceMetadata {
    private String id;
    private String device;  // e.g. "Samsung Galaxy S21"
    private String os;  // e.g. "Android 11"
    private String screenResolution;  // e.g. "2400x1080"
    private String networkType;  // Enum
    private String batteryLevel;  // e.g. 75 (represents 75%)
    private String memoryUsage;
    @DBRef
    private Organisation organisation;


}
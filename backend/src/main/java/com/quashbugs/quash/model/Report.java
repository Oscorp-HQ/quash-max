package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reports")
public class Report {
    @Id
    private String id;
    private String title;
    private String description;
    @DBRef
    private User reportedBy;
    private String type;
    private String source;
    private String status;
    private Date createdAt;
    @DBRef
    private List<BugMedia> listOfMedia;
    @DBRef
    private List<GifBitmap> listOfGif;
    @DBRef
    private CrashLog crashLog;
    @DBRef
    private DeviceMetadata deviceMetadata;
    private String appId;
    private Date exportedOn;
    private Date updatedAt;
    private String priority;
    private GifStatus gifStatus = GifStatus.NOT_INITIATED;
}
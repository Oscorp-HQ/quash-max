package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crash-logs")
public class CrashLog {
    @Id
    private String id;
    private String logUrl;
    private String mediaRef;
    @DBRef
    private String bugId;
    private Date createdAt;
}
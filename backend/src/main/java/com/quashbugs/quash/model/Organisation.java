package com.quashbugs.quash.model;

import jakarta.persistence.Transient;
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
@Document(collection = "organisations")
public class Organisation {

    @Transient
    public static final String SEQUENCE_NAME = "organisation_sequence";

    @Id
    private long id; // Change the type to Long for numeric ID

    private String orgAbbreviation;
    private String orgUniqueKey;
    private String name;
    private String profileImage;
    private String coverImage;
    @DBRef
    private User createdBy;
    private Date createdAt;
    private Date lastActive;
    protected Boolean shouldSend24HrMail = true;
    protected Boolean shouldSend72HrMail = true;

    // Other methods, if any
}

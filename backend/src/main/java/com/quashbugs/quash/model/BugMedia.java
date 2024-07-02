package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bug-media")
public class BugMedia {
    @Id
    private String id;
    private String bugId;
    private String mediaRef;
    private String mediaUrl;
    private Date createdAt;
    private MediaType mediaType;
}

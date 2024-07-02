package com.quashbugs.quash.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "thread-uploads")
public class ChatUploads {

    private String id;
    private String mediaRef;
    private String mediaType;
    @DBRef
    private ChatThread chatThread;
}

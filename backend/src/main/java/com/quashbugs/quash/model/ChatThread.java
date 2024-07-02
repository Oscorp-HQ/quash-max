package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "threads")
public class ChatThread {
    private String id;
    private String posterId;
    private String messages;
    private ArrayList<String> mentions;
    private String timestamp;
    @DBRef
    private Report report;

}

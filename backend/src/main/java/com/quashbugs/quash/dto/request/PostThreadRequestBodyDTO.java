package com.quashbugs.quash.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostThreadRequestBodyDTO {

    private String reportId;
    private String messages;
    private String posterId;
    private ArrayList<String> mentions;
    private ArrayList<MultipartFile> attachments;
}

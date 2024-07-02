package com.quashbugs.quash.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequestDTO {
    private String title;
    private String description;
    private String status;
    private String type;
    private String priority;
    private List<MultipartFile> newMediaFiles;
    private List<String> mediaToRemoveIds;

}
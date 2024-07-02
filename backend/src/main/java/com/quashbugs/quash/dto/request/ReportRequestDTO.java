package com.quashbugs.quash.dto.request;

import com.quashbugs.quash.model.DeviceMetadata;
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
public class ReportRequestDTO {
    private String title;
    private String description;
    private String type;
    private String source;
    private String priority;
    private List<MultipartFile> mediaFiles;
    private MultipartFile crashLog;
    private String reporterId;
    private String appId;
    private DeviceMetadata deviceMetadata;

}

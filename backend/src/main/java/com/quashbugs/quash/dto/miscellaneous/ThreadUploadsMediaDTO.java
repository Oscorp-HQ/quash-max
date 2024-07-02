package com.quashbugs.quash.dto.miscellaneous;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadUploadsMediaDTO {

    private String url;
    private String mediaType;
}

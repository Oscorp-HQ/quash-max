package com.quashbugs.quash.dto.response;


import com.quashbugs.quash.dto.miscellaneous.ThreadUploadsMediaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatThreadResponseDTO {
    private String id;
    private String posterId;
    private String messages;
    private ArrayList<String> mentions;
    private String timestamp;
    private ArrayList<ThreadUploadsMediaDTO> uploads;
}

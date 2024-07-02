package com.quashbugs.quash.dto.request;


import com.quashbugs.quash.model.NetworkLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkLogRequestBodyDTO {

    List<NetworkLog> networkLogs;
}

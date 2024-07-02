package com.quashbugs.quash.dto.response;

import com.quashbugs.quash.dto.miscellaneous.MetaDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaginatedResponseDTO<T> {
    private List<T> reports;
    private MetaDataDTO meta;

}
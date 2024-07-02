package com.quashbugs.quash.dto.miscellaneous;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public
class MetaDataDTO {

    private int currentPage;
    private int totalPages;
    private long totalRecords;
    private int perPage;

}
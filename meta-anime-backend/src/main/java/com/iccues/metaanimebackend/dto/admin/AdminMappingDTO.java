package com.iccues.metaanimebackend.dto.admin;

import com.iccues.metaanimebackend.entity.MappingInfo;
import com.iccues.metaanimebackend.entity.Platform;

public record AdminMappingDTO(
        Long mappingId,
        Long animeId,
        Platform sourcePlatform,
        String platformId,
        Double rawScore,
        MappingInfo mappingInfo
) {
}

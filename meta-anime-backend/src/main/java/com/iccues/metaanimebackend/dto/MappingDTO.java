package com.iccues.metaanimebackend.dto;

import com.iccues.metaanimebackend.entity.Platform;

public record MappingDTO(
        Long mappingId,
        Platform sourcePlatform,
        String platformId,
        Double rawScore
) {
}

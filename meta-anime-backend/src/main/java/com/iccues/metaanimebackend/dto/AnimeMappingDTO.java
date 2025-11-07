package com.iccues.metaanimebackend.dto;

public record AnimeMappingDTO(
        Long mappingId,
        String sourcePlatform,
        String platformId,
        Double rawScore
) {
}

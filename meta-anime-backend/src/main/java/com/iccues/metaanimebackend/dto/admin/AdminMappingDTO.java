package com.iccues.metaanimebackend.dto.admin;

public record AdminMappingDTO(
        Long mappingId,
        Long animeId,
        String sourcePlatform,
        String platformId,
        Double rawScore,
        MappingInfo mappingInfo
) {
}

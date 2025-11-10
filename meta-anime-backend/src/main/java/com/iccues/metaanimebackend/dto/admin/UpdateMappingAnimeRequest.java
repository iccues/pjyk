package com.iccues.metaanimebackend.dto.admin;

public record UpdateMappingAnimeRequest(
        Long mappingId,
        Long animeId  // null 表示解除关联
) {
}

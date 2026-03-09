package com.yukiani.dto.admin;

import com.yukiani.entity.MappingInfo;
import com.yukiani.entity.Platform;

public record AdminMappingDTO(
        Long mappingId,
        Long animeId,
        Platform sourcePlatform,
        String platformId,
        Double rawScore,
        MappingInfo mappingInfo
) {
}

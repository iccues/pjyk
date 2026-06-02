package com.yukiani.server.dto.admin;

import com.yukiani.server.entity.MappingInfo;
import com.yukiani.server.entity.Platform;

public record AdminMappingDTO(
        Long mappingId,
        Long animeId,
        Platform sourcePlatform,
        String platformId,
        Double rawScore,
        MappingInfo mappingInfo
) {
}

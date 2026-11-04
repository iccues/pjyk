package com.yukiani.server.dto.admin;

import com.yukiani.server.entity.MappingInfo;
import com.yukiani.server.entity.PlatformKey;

public record AdminMappingDTO(
        Long mappingId,
        Long animeId,
        PlatformKey platformKey,
        Double rawScore,
        MappingInfo mappingInfo
) {
}

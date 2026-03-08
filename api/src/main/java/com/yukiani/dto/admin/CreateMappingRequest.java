package com.yukiani.dto.admin;

import com.yukiani.entity.Platform;

public record CreateMappingRequest(
        Platform sourcePlatform,
        String platformId
) {
}

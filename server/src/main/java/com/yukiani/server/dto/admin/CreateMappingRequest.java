package com.yukiani.server.dto.admin;

import com.yukiani.server.entity.Platform;

public record CreateMappingRequest(
        Platform sourcePlatform,
        String platformId
) {
}

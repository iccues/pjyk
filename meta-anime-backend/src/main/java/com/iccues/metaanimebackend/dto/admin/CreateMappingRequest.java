package com.iccues.metaanimebackend.dto.admin;

import com.iccues.metaanimebackend.entity.Platform;

public record CreateMappingRequest(
        Platform sourcePlatform,
        String platformId
) {
}

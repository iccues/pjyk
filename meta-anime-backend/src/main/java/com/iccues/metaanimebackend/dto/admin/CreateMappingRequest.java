package com.iccues.metaanimebackend.dto.admin;

public record CreateMappingRequest(
        String sourcePlatform,
        String platformId
) {
}

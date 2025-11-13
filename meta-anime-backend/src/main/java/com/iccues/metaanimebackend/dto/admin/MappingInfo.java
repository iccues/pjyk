package com.iccues.metaanimebackend.dto.admin;

import com.iccues.metaanimebackend.entity.AnimeTitles;

import java.time.LocalDate;

public record MappingInfo(
        AnimeTitles title,
        String coverImage,
        LocalDate startDate
) {
}

package com.iccues.metaanimebackend.dto.admin;

import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.ReviewStatus;

import java.time.LocalDate;
import java.util.List;

public record AdminAnimeDTO(
        Long animeId,
        AnimeTitles title,
        String coverImage,
        LocalDate startDate,
        Double averageScore,
        ReviewStatus reviewStatus,
        List<AdminMappingDTO> mappings
) {
}

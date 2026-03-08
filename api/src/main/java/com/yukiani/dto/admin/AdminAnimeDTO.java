package com.yukiani.dto.admin;

import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.ReviewStatus;

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

package com.yukiani.server.dto.admin;

import com.yukiani.server.entity.AnimeTitles;
import com.yukiani.server.entity.ReviewStatus;

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

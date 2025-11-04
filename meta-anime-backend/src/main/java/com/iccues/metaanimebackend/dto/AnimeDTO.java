package com.iccues.metaanimebackend.dto;

import java.time.LocalDate;
import java.util.List;

public record AnimeDTO(
        Long animeId,
        AnimeTitlesDTO title,
        String coverImage,
        LocalDate startDate,
        Double averageScore,
        List<AnimeMappingDTO> mappings
) {
}

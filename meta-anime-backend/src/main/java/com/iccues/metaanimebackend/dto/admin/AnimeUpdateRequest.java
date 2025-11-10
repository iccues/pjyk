package com.iccues.metaanimebackend.dto.admin;

import com.iccues.metaanimebackend.entity.AnimeTitles;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnimeUpdateRequest(
        @NotNull
        Long animeId,
        AnimeTitles title,
        String coverImage,
        LocalDate startDate
) {
}

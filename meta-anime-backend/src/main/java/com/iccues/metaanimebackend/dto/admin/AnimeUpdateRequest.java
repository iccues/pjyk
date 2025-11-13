package com.iccues.metaanimebackend.dto.admin;

import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnimeUpdateRequest(
        @NotNull
        Long animeId,
        AnimeTitles title,
        String coverImage,
        ReviewStatus reviewStatus,
        LocalDate startDate
) {
}

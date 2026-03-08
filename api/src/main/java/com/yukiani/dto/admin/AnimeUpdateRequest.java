package com.yukiani.dto.admin;

import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.ReviewStatus;
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

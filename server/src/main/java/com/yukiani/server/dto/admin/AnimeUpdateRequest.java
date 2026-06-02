package com.yukiani.server.dto.admin;

import com.yukiani.server.entity.AnimeTitles;
import com.yukiani.server.entity.ReviewStatus;
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

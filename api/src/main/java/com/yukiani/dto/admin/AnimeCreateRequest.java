package com.yukiani.dto.admin;

import com.yukiani.entity.AnimeTitles;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnimeCreateRequest(
        AnimeTitles title,
        String coverImage,
        LocalDate startDate
) {
}

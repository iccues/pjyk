package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.LocalDateRange;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.entity.SortBy;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.spec.AnimeSpec;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * 处理公开的动画查询逻辑
 */
@Service
public class AnimeQueryService {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    SeasonService seasonService;

    /**
     * 获取动画列表（公开API）
     * 只返回已审核通过的动画
     */
    public Page<Anime> getAnimeList(Integer year, Season season, int pageNumber, int pageSize, SortBy sortBy) {
        LocalDateRange dateRange = seasonService.getStartDateRange(year, season);
        Specification<Anime> spec = Specification.allOf(
                AnimeSpec.startDateBetween(dateRange),
                AnimeSpec.reviewStatusEquals(ReviewStatus.APPROVED),
                AnimeSpec.orderBy(sortBy)
        );
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return animeRepository.findAll(spec, pageRequest);
    }
}

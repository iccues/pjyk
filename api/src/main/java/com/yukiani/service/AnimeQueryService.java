package com.yukiani.service;

import com.yukiani.entity.Anime;
import com.yukiani.entity.LocalDateRange;
import com.yukiani.entity.ReviewStatus;
import com.yukiani.entity.Season;
import com.yukiani.entity.SortBy;
import com.yukiani.repo.AnimeRepository;
import com.yukiani.spec.AnimeSpec;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
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
        int limitedPageSize = Math.min(pageSize, 60);

        LocalDateRange dateRange = seasonService.getStartDateRange(year, season);
        Specification<Anime> spec = Specification.allOf(
                AnimeSpec.startDateBetween(dateRange),
                AnimeSpec.reviewStatusEquals(ReviewStatus.APPROVED),
                AnimeSpec.orderBy(sortBy)
        );
        PageRequest pageRequest = PageRequest.of(pageNumber, limitedPageSize);
        return animeRepository.findAll(spec, pageRequest);
    }

    public Page<Anime> getAnimeListBySearch(
            String query,
            Integer pageNumber,
            Integer pageSize
    ) {
        int limitedPageSize = Math.min(pageSize, 60);

        Specification<Anime> spec = Specification.allOf(
                AnimeSpec.similarTitle(query),
                AnimeSpec.reviewStatusEquals(ReviewStatus.APPROVED)
        );
        PageRequest pageRequest = PageRequest.of(pageNumber, limitedPageSize);
        return animeRepository.findAll(spec, pageRequest);
    }
}

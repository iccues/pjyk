package com.yukiani.service;

import com.yukiani.entity.Anime;
import com.yukiani.entity.LocalDateRange;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.ReviewStatus;
import com.yukiani.entity.Season;
import com.yukiani.exception.ResourceNotFoundException;
import com.yukiani.repo.AnimeRepository;
import com.yukiani.repo.MappingRepository;
import com.yukiani.spec.AnimeSpec;
import jakarta.annotation.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 处理管理员的动画 CRUD 操作
 */
@Service
public class AnimeManageService {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    MappingRepository mappingRepository;

    @Resource
    SeasonService seasonService;

    @Resource
    private AnimeAggregationService animeAggregationService;

    /**
     * 获取动画列表（管理员，支持按审核状态筛选）
     */
    public List<Anime> getAnimeList(ReviewStatus reviewStatus, Integer year, Season season) {
        LocalDateRange dateRange = seasonService.getStartDateRange(year, season);
        Specification<Anime> spec = Specification.allOf(
                AnimeSpec.reviewStatusEquals(reviewStatus),
                AnimeSpec.startDateBetween(dateRange),
                AnimeSpec.orderByScoreNullLast()
        );
        return animeRepository.findAll(spec);
    }

    /**
     * 创建动画
     */
    @Transactional
    public Anime createAnime(Anime anime) {
        return animeRepository.save(anime);
    }

    /**
     * 更新动画
     */
    @Transactional
    public Anime updateAnime(Long animeId, Consumer<Anime> updater) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new ResourceNotFoundException("Anime", animeId));
        updater.accept(anime);
        return animeRepository.save(anime);
    }

    /**
     * 删除动画（包括解除映射关联）
     */
    @Transactional
    public void deleteAnime(Long animeId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new ResourceNotFoundException("Anime", animeId));

        // 解除所有映射的关联
        List<Mapping> mappingsCopy = new ArrayList<>(anime.getMappings());
        mappingsCopy.forEach(mapping -> {
            animeAggregationService.removeMappingWithMetrics(anime, mapping);
            mappingRepository.save(mapping);
        });

        animeRepository.delete(anime);
    }

    @Transactional
    public void deleteNonApprovedAnimes() {
        animeRepository.deleteAllByReviewStatusIsNot(ReviewStatus.APPROVED);
        mappingRepository.deleteAllByAnimeIsNull();
    }
}

package com.yukiani.server.service;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.exception.ResourceAlreadyExistsException;
import com.yukiani.server.exception.ResourceNotFoundException;
import com.yukiani.server.repo.AnimeRepository;
import com.yukiani.server.repo.MappingRepository;
import com.yukiani.server.service.fetch.AbstractAnimeFetchService;
import com.yukiani.server.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 处理管理端平台 Mapping 的查询、关联、创建和删除。
 */
@Service
public class MappingManageService {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    MappingRepository mappingRepository;

    @Resource
    FetchService fetchService;

    @Resource
    AnimeAggregationService animeAggregationService;

    public List<Mapping> getUnmappedMappingList() {
        return mappingRepository.findAllByAnimeIsNull();
    }

    /**
     * 将 Mapping 关联到目标动画，或在 animeId 为空时解除关联。
     *
     * @param animeId   目标动画的 animeId；为空时解除关联
     * @throws ResourceNotFoundException Mapping 或目标动画不存在时抛出
     */
    @Transactional
    public Mapping updateMappingAnime(Long mappingId, Long animeId) {
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping", mappingId));

        Anime currentAnime = mapping.getAnime();

        if (currentAnime != null) {
            animeAggregationService.removeMappingWithMetrics(currentAnime, mapping);
        }

        if (animeId != null) {
            Anime targetAnime = animeRepository.findById(animeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Anime", animeId));
            animeAggregationService.addMappingWithMetrics(targetAnime, mapping);
        }

        return mappingRepository.save(mapping);
    }

    /**
     * 删除 Mapping，并刷新原关联动画的聚合数据。
     *
     * @throws ResourceNotFoundException Mapping 不存在时抛出
     */
    @Transactional
    public void deleteMapping(Long mappingId) {
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping", mappingId));

        Anime relatedAnime = mapping.getAnime();

        if (relatedAnime != null) {
            animeAggregationService.removeMappingWithMetrics(relatedAnime, mapping);
        }

        mappingRepository.delete(mapping);
    }

    /**
     * 从外部平台即时抓取并创建 Mapping。
     *
     * @throws ResourceAlreadyExistsException 相同平台 Mapping 已存在时抛出
     */
    public Mapping createMapping(Platform platform, String platformId) {
        Mapping existingMapping = mappingRepository.findByPlatformAndPlatformId(
                platform, platformId);

        if (existingMapping != null) {
            throw new ResourceAlreadyExistsException("Mapping",
                    platform.name() + " - " + platformId);
        }

        AbstractAnimeFetchService fetchServiceImpl = fetchService.getFetchService(platform);
        return fetchServiceImpl.fetchAndSaveMapping(platformId);
    }
}

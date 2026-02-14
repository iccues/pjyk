package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.exception.ResourceAlreadyExistsException;
import com.iccues.metaanimebackend.exception.ResourceNotFoundException;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 处理管理员的映射管理操作
 */
@Service
public class MappingManageService {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    MappingRepository mappingRepository;

    @Resource
    MetricService metricService;

    @Resource
    FetchService fetchService;

    @Resource
    AnimeAggregationService animeAggregationService;

    /**
     * 获取未关联的映射列表
     */
    public List<Mapping> getUnmappedMappingList() {
        return mappingRepository.findAllByAnimeIsNull();
    }

    /**
     * 更新映射的动画关联
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
     * 删除映射
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
     * 从平台创建映射
     */
    public Mapping createMapping(Platform sourcePlatform, String platformId) {
        Mapping existingMapping = mappingRepository.findBySourcePlatformAndPlatformId(
                sourcePlatform, platformId);

        if (existingMapping != null) {
            throw new ResourceAlreadyExistsException("Mapping",
                    sourcePlatform.name() + " - " + platformId);
        }

        AbstractAnimeFetchService fetchServiceImpl = fetchService.getFetchService(sourcePlatform);
        return fetchServiceImpl.fetchAndSaveMapping(platformId);
    }
}

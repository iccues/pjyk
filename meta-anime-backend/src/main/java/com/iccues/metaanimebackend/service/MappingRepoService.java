package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MappingRepoService {
    @Resource
    MappingRepository repo;

    @Resource
    MetricService metricService;

    @Transactional
    public void saveOrUpdate(Mapping m) {
        var existing = repo.findBySourcePlatformAndPlatformId(m.getSourcePlatform(), m.getPlatformId());
        if (existing != null) {
            existing.setRawScore(m.getRawScore());
            existing.setNormalizedScore(m.getNormalizedScore());
            existing.setMappingInfo(m.getMappingInfo());
            existing.setUpdateTime(m.getUpdateTime());
            existing.setRawPopularity(m.getRawPopularity());
            existing.setNormalizedPopularity(m.getNormalizedPopularity());

            Anime anime = existing.getAnime();
            if (anime != null) {
                metricService.calculateMetric(anime);
            }

            repo.save(existing);
        } else {
            repo.save(m);
        }
    }
}

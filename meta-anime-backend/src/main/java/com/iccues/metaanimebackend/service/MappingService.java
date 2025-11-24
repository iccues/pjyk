package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MappingService {
    @Resource
    MappingRepository repo;

    @Transactional
    public void saveOrUpdate(Mapping m) {
        log.debug("Saving or updating mapping: platform={}, platformId={}",
                  m.getSourcePlatform(), m.getPlatformId());
        var existing = repo.findBySourcePlatformAndPlatformId(m.getSourcePlatform(), m.getPlatformId());
        if (existing != null) {
            log.debug("Updating existing mapping id={}", existing.getMappingId());
            existing.setRawScore(m.getRawScore());
            existing.setNormalizedScore(m.getNormalizedScore());
            existing.setRawJSON(m.getRawJSON());
            existing.setUpdateTime(m.getUpdateTime());
            repo.save(existing);
        } else {
            log.debug("Creating new mapping");
            repo.save(m);
        }
    }
}

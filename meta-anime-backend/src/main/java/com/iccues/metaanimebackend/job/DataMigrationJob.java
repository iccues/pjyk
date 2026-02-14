package com.iccues.metaanimebackend.job;

import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据迁移工具：重新计算 Mapping 的归一化数据
 * 使用场景：
 * 1. 修改了 application.yml 中的平台配置参数（popularity-multiplier, score-mean, score-std 等）
 * 2. 需要将现有数据按新的参数重新归一化
 * 使用方法：
 * 1. 取消注释需要执行的方法上的 @PostConstruct 注解
 * 2. 启动应用，任务会自动执行
 * 3. 执行完成后，重新注释掉 @PostConstruct 注解
 * 注意：此任务会遍历所有 Mapping 并更新数据库，请谨慎使用
 */
@Slf4j
@Service
public class DataMigrationJob {
    @Resource
    MappingRepository mappingRepository;
    @Resource
    FetchService fetchService;

    /**
     * 重新计算所有 Mapping 的 normalizedPopularity
     * 使用场景：修改了 popularity-multiplier 配置后
     */
    // @PostConstruct
    @Transactional
    public void recalculateAllPopularity() {
        log.info("开始重新计算所有 Mapping 的 normalizedPopularity...");

        List<Mapping> mappingList = mappingRepository.findAll();
        int total = mappingList.size();
        int updated = 0;

        for (Mapping mapping : mappingList) {
            Platform platform = mapping.getSourcePlatform();
            AbstractAnimeFetchService implFetchService = fetchService.getFetchService(platform);
            Double raw = mapping.getRawPopularity();

            if (raw == null) {
                continue;
            }

            Double normalized = implFetchService.normalizePopularity(raw);
            mapping.setNormalizedPopularity(normalized);
            mappingRepository.save(mapping);
            updated++;
        }

        log.info("重新计算 popularity 完成！总计: {}, 更新: {}", total, updated);
    }

    /**
     * 重新计算所有 Mapping 的 normalizedScore
     * 使用场景：修改了 score-mean 或 score-std 配置后
     */
//    @PostConstruct
    @Transactional
    public void recalculateAllScores() {
        log.info("开始重新计算所有 Mapping 的 normalizedScore...");

        List<Mapping> mappingList = mappingRepository.findAll();
        int total = mappingList.size();
        int updated = 0;

        for (Mapping mapping : mappingList) {
            Platform platform = mapping.getSourcePlatform();
            AbstractAnimeFetchService implFetchService = fetchService.getFetchService(platform);
            Double rawScore = mapping.getRawScore();

            if (rawScore == null || rawScore <= 0) {
                mapping.setNormalizedScore(null);
                continue;
            }

            Double normalizedScore = implFetchService.normalizeScore(rawScore);
            mapping.setNormalizedScore(normalizedScore);
            mappingRepository.save(mapping);
            updated++;
        }

        log.info("重新计算 score 完成！总计: {}, 更新: {}", total, updated);
    }
}

package com.yukiani.server.service;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.MappingMetricHistory;
import com.yukiani.server.repo.MappingRepository;
import com.yukiani.server.repo.MappingMetricHistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 保存平台 Mapping，并维护关联动画的聚合指标和平台指标历史。
 */
@Service
public class MappingRepoService {
    @Resource
    MappingRepository repo;

    @Resource
    MetricService metricService;

    @Resource
    MappingMetricHistoryRepository mappingMetricHistoryRepository;

    /**
     * 按平台 natural key 新增或更新 Mapping，并在更新现有 Mapping 后刷新关联动画指标。
     *
     * <p>每次保存都会追加一条原始指标快照，包括指标未变化或两项指标均为空的情况。
     * Mapping 与快照在同一事务中写入。</p>
     *
     * @return 保存后受管理的 Mapping。新增时不是传入的对象本身：Mapping 的 version 初始为 0，
     *         save 会按合并处理并返回新的实例，调用方应使用返回值
     */
    @Transactional
    public Mapping saveOrUpdate(Mapping m) {
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

            existing = repo.save(existing);
            recordMetricHistory(existing);
            return existing;
        }

        m = repo.save(m);
        recordMetricHistory(m);
        return m;
    }

    /** 将 Mapping 当前的原始指标和更新时间保存为历史记录。 */
    private void recordMetricHistory(Mapping mapping) {
        mappingMetricHistoryRepository.save(new MappingMetricHistory(
                mapping.getSourcePlatform(),
                mapping.getPlatformId(),
                mapping.getRawScore(),
                mapping.getRawPopularity(),
                mapping.getUpdateTime()
        ));
    }
}

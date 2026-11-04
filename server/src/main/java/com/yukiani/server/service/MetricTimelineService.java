package com.yukiani.server.service;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.MappingMetricHistory;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.PlatformKey;
import com.yukiani.server.repo.MappingMetricHistoryRepository;
import com.yukiani.server.service.fetch.FetchService;
import com.yukiani.server.util.MetricDateUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 按当前平台配置计算历史归一化指标和动画综合指标。
 *
 * <p>日期边界由 {@link MetricDateUtil} 定义，同日多次观测取最后一次，时间相同时取 historyId 最大者。</p>
 * <p>综合指标沿用各平台最近一次观测（LOCF），仅在区间内的观测日生成采样点。</p>
 */
@Service
public class MetricTimelineService {

    /** 平台单日观测，date 为观测日期。 */
    public record MappingPoint(LocalDate date, Double rawScore, Double normalizedScore, Double rawPopularity, Double normalizedPopularity) {
    }

    /** 动画综合指标，date 为参与计算的最新观测日期。 */
    public record AnimePoint(LocalDate date, Double averageScore, Double popularity) {
    }

    @Resource
    MappingMetricHistoryRepository historyRepository;

    @Resource
    MetricService metricService;

    @Resource
    FetchService fetchService;

    /**
     * 查询日期区间（含首尾）内的逐日平台观测，按日期升序，不补点；无观测时返回空列表。
     */
    @Transactional(readOnly = true)
    public List<MappingPoint> mappingHistory(PlatformKey key, LocalDate from, LocalDate to) {
        return seriesInRange(List.of(key), from, to)
                .getOrDefault(key, List.of()).stream()
                .map(this::toMappingPoint)
                .toList();
    }

    /**
     * 查询指定日期结束时的最新平台观测，无观测时返回 {@code null}。
     */
    @Transactional(readOnly = true)
    public MappingPoint mappingMetricAt(PlatformKey key, LocalDate at) {
        MappingMetricHistory history = latestBefore(List.of(key), MetricDateUtil.endOf(at)).get(key);
        return history == null ? null : toMappingPoint(history);
    }

    /**
     * 计算指定日期结束时的综合指标，无观测时返回 {@code null}。
     */
    @Transactional(readOnly = true)
    public AnimePoint animeMetricAt(Collection<PlatformKey> keys, LocalDate at) {
        Map<PlatformKey, MappingMetricHistory> resolved = latestBefore(keys, MetricDateUtil.endOf(at));
        return resolved.isEmpty() ? null : toAnimePoint(resolved.values());
    }

    /**
     * 计算日期区间（含首尾）内的综合指标序列，按日期升序；无观测时返回空列表。
     *
     * <p>区间前的观测仅提供沿用值，起点取值可通过 {@link #animeMetricAt} 查询。</p>
     */
    @Transactional(readOnly = true)
    public List<AnimePoint> animeHistory(Collection<PlatformKey> keys, LocalDate from, LocalDate to) {
        if (keys.isEmpty()) {
            return List.of();
        }

        // 带入区间前各平台的最后一次观测。
        Map<PlatformKey, MappingMetricHistory> resolved =
                latestBefore(keys, MetricDateUtil.startOf(from));
        Map<PlatformKey, List<MappingMetricHistory>> ranged = seriesInRange(keys, from, to);

        // 合并各平台观测日期，作为综合指标的采样日期。
        Map<LocalDate, List<MappingMetricHistory>> byDate = new TreeMap<>();
        for (List<MappingMetricHistory> series : ranged.values()) {
            for (MappingMetricHistory observation : series) {
                byDate.computeIfAbsent(MetricDateUtil.dateOf(observation.getRecordedAt()), d -> new ArrayList<>())
                        .add(observation);
            }
        }

        List<AnimePoint> points = new ArrayList<>(byDate.size());
        for (List<MappingMetricHistory> observations : byDate.values()) {
            for (MappingMetricHistory observation : observations) {
                resolved.put(
                        observation.getPlatformKey(),
                        observation);
            }
            points.add(toAnimePoint(resolved.values()));
        }

        return points;
    }

    /**
     * 将按时间、historyId 升序的单条序列折叠为逐日观测，每天保留最后一条。
     */
    private List<MappingMetricHistory> collapseByDate(List<MappingMetricHistory> observations) {
        // 覆盖同日观测时保留日期顺序。
        Map<LocalDate, MappingMetricHistory> byDate = new LinkedHashMap<>();
        for (MappingMetricHistory observation : observations) {
            byDate.put(MetricDateUtil.dateOf(observation.getRecordedAt()), observation);
        }
        return List.copyOf(byDate.values());
    }

    /** 批量查询指定时刻之前各序列的最新观测。 */
    private Map<PlatformKey, MappingMetricHistory> latestBefore(
            Collection<PlatformKey> keys, Instant atExclusive) {
        Map<PlatformKey, MappingMetricHistory> resolved = new HashMap<>();
        historyRepository.queryLatestBefore(keys, atExclusive).forEach(history ->
                resolved.put(history.getPlatformKey(), history));
        return resolved;
    }

    /** 批量查询日期区间（含首尾）内的观测，按序列分组并折叠为日期升序的逐日观测。 */
    private Map<PlatformKey, List<MappingMetricHistory>> seriesInRange(
            Collection<PlatformKey> keys, LocalDate from, LocalDate to) {
        Map<PlatformKey, List<MappingMetricHistory>> ranged = new HashMap<>();
        historyRepository.querySeries(keys, MetricDateUtil.startOf(from), MetricDateUtil.endOf(to))
                .forEach(history -> ranged
                        .computeIfAbsent(history.getPlatformKey(),
                                key -> new ArrayList<>())
                        .add(history));

        ranged.replaceAll((key, observations) -> collapseByDate(observations));
        return ranged;
    }

    /**
     * 根据各平台观测计算综合指标。
     *
     * @param histories 各序列采用的观测，不能为空
     */
    private AnimePoint toAnimePoint(Collection<MappingMetricHistory> histories) {
        // 通过临时实体复用现有指标计算。
        Anime anime = new Anime();
        histories.forEach(history -> anime.addMapping(toMapping(history)));
        metricService.calculateMetric(anime);

        LocalDate date = histories.stream()
                .map(history -> MetricDateUtil.dateOf(history.getRecordedAt()))
                .max(Comparator.naturalOrder())
                .orElseThrow();

        return new AnimePoint(date, anime.getAverageScore(), anime.getPopularity());
    }

    /** 从历史原始指标构造临时 Mapping，并按当前配置计算归一化指标。 */
    private Mapping toMapping(MappingMetricHistory history) {
        Platform platform = history.getPlatformKey().getPlatform();
        Mapping mapping = new Mapping();
        mapping.setPlatformKey(history.getPlatformKey());
        mapping.setRawScore(history.getRawScore());
        mapping.setRawPopularity(history.getRawPopularity());
        fetchService.getFetchService(platform).recalculateMetrics(mapping);
        return mapping;
    }

    private MappingPoint toMappingPoint(MappingMetricHistory history) {
        Mapping mapping = toMapping(history);
        return new MappingPoint(
                MetricDateUtil.dateOf(history.getRecordedAt()),
                mapping.getRawScore(), mapping.getNormalizedScore(),
                mapping.getRawPopularity(), mapping.getNormalizedPopularity());
    }
}

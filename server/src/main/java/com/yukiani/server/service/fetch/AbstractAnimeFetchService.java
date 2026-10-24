package com.yukiani.server.service.fetch;

import com.yukiani.server.config.PlatformConfig;
import com.yukiani.server.config.PlatformConfigProperties;
import com.yukiani.server.entity.*;
import com.yukiani.server.exception.FetchFailedException;
import com.yukiani.server.service.MappingRepoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

/**
 * 定义外部平台动画数据的抓取、字段提取、指标归一化和持久化模板。
 *
 * <p>平台实现只负责 API 调用与 field mapping，公共流程负责构造并幂等保存 Mapping。</p>
 */
@Slf4j
public abstract class AbstractAnimeFetchService {
    @Resource
    protected MappingRepoService mappingRepoService;

    @Resource
    protected PlatformConfigProperties platformConfigProperties;

    protected abstract Platform getPlatform();

    /**
     * 提取开播日期。
     *
     * @return 平台数据中的开播日期；缺失或无效时返回 {@code null}
     */
    protected abstract LocalDate extractStartDate(JsonNode jsonNode);

    protected abstract AnimeTitles extractTitles(JsonNode jsonNode);

    /**
     * 提取封面地址。
     *
     * @return 平台数据中的封面地址；缺失时返回 {@code null}
     */
    protected abstract String extractCoverImage(JsonNode jsonNode);

    /**
     * 提取平台侧 platformId。
     *
     * @return platformId；缺失时返回 {@code null}
     */
    protected abstract String extractPlatformId(JsonNode jsonNode);

    /**
     * 提取平台原始评分。
     *
     * @return 平台原始评分；无有效评分时返回 {@code null}
     */
    protected abstract Double extractRawScore(JsonNode jsonNode);

    /**
     * 按平台先验均值和归一化热度对原始评分进行平滑。
     *
     * <p>未配置先验强度时返回原始评分。</p>
     *
     * @param rawScore             平台原始评分
     * @param normalizedPopularity 以平台热度中位数为 10000 归一化后的热度
     * @return 平滑后的评分；原始评分无效时返回 {@code null}
     */
    public Double adjustScore(Double rawScore, double normalizedPopularity) {
        if (rawScore == null || rawScore <= 0) {
            return null;
        }

        PlatformConfig config = platformConfigProperties.getConfig(getPlatform());
        Double priorStrength = config.getScorePriorStrength();
        if (priorStrength == null || priorStrength <= 0) {
            return rawScore;
        }

        double validPopularity = Math.max(normalizedPopularity, 0);
        return (validPopularity * rawScore + priorStrength * config.getScoreMean())
                / (validPopularity + priorStrength);
    }

    /**
     * 使用平台均值和标准差将评分映射到以 50 为中心的统一尺度。
     *
     * @return 归一化评分；原始评分无效时返回 {@code null}
     */
    public Double normalizeScore(Double rawScore) {
        if (rawScore == null || rawScore <= 0) {
            return null;
        }
        PlatformConfig config = platformConfigProperties.getConfig(getPlatform());
        double mean = config.getScoreMean();
        double std = config.getScoreStd();
        double z = (rawScore - mean) / std;
        return 50 + (z * (100 / 6.0));
    }

    /**
     * 提取平台原始热度。
     *
     * @return 平台原始热度；缺失时由平台实现返回零
     */
    protected abstract double extractRawPopularity(JsonNode jsonNode);

    /**
     * 使用平台热度中位数将原始热度缩放到统一尺度。
     */
    public double normalizePopularity(double rawPopularity) {
        PlatformConfig config = platformConfigProperties.getConfig(getPlatform());
        double median = config.getPopularityMedian();
        return rawPopularity / median * 10000;
    }

    protected MappingInfo extractMappingInfo(JsonNode jsonNode) {
        return new MappingInfo(
                extractTitles(jsonNode),
                extractCoverImage(jsonNode),
                extractStartDate(jsonNode)
        );
    }

    /**
     * 抓取指定年份和季度的全部平台数据。
     *
     * @param season 开播季度；为空时抓取全年
     */
    protected abstract List<JsonNode> fetchMappingJson(int year, Season season);

    /**
     * 按 platformId 抓取单条数据。
     *
     * @return 平台原始数据；平台未返回内容时返回 {@code null}
     */
    protected abstract JsonNode fetchSingleMappingJson(String platformId);

    /**
     * 从单条平台数据构造 Mapping 并计算归一化指标，不做校验也不保存。
     *
     * <p>platformId 和开播日期可能为 {@code null}，由调用方按需检查后再保存。</p>
     */
    Mapping buildMapping(JsonNode jsonNode) {
        Mapping mapping = new Mapping(getPlatform(), extractPlatformId(jsonNode), extractMappingInfo(jsonNode));
        mapping.setRawPopularity(extractRawPopularity(jsonNode));
        mapping.setRawScore(extractRawScore(jsonNode));
        recalculateMetrics(mapping);
        return mapping;
    }

    /**
     * 使用当前平台配置，从 Mapping 原始指标重新计算归一化指标。
     *
     * @param mapping 与当前抓取服务属于同一平台的 Mapping
     * @throws IllegalArgumentException Mapping 平台与当前抓取服务不一致时抛出
     */
    public void recalculateMetrics(Mapping mapping) {
        if (mapping.getSourcePlatform() != getPlatform()) {
            throw new IllegalArgumentException("Mapping 平台与 FetchService 不匹配");
        }

        Double rawPopularity = mapping.getRawPopularity();
        Double normalizedPopularity = rawPopularity == null
                ? null
                : normalizePopularity(rawPopularity);
        mapping.setNormalizedPopularity(normalizedPopularity);

        double popularityForAdjustment = normalizedPopularity == null ? 0 : normalizedPopularity;
        Double adjustedScore = adjustScore(mapping.getRawScore(), popularityForAdjustment);
        mapping.setNormalizedScore(normalizeScore(adjustedScore));
    }

    /**
     * 抓取并保存指定年份和季度的全部平台 Mapping，缺少 platformId 或开播日期的条目会被跳过。
     *
     * @param season 开播季度；为空时抓取全年
     * @throws FetchFailedException 平台请求或数据处理失败时抛出
     */
    @Transactional
    public void fetchAndSaveMappings(int year, Season season) {
        try {
            List<JsonNode> mediaList = fetchMappingJson(year, season);
            for (JsonNode jsonNode : mediaList) {
                Mapping mapping = buildMapping(jsonNode);
                // 缺少 platformId 或开播日期的条目无法参与主数据关联，不必保存。
                if (mapping.getPlatformId() == null || mapping.getMappingInfo().getStartDate() == null) {
                    continue;
                }
                mappingRepoService.saveOrUpdate(mapping);
            }
        } catch (WebClientResponseException e) {
            log.warn("Failed to fetch mappings from {} for year={}, season={}: {} {}",
                    getPlatform(), year, season, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FetchFailedException(getPlatform(),
                    String.format("year=%d, season=%s", year, season), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching mappings from {} for year={}, season={}: {}",
                    getPlatform(), year, season, e.getMessage());
            throw new FetchFailedException(getPlatform(),
                    String.format("year=%d, season=%s", year, season), e);
        }
    }

    /**
     * 抓取并保存指定平台资源；正常返回即表示请求的 Mapping 已保存。
     *
     * <p>开播日期缺失时照常保存，主数据关联流程会自行忽略没有开播日期的 Mapping。</p>
     *
     * @throws FetchFailedException 平台请求失败、返回的 platformId 与请求不一致或数据处理失败时抛出
     */
    @Transactional
    public Mapping fetchAndSaveMapping(String platformId) {
        try {
            JsonNode jsonNode = fetchSingleMappingJson(platformId);
            if (jsonNode == null) {
                log.warn("{} API returned null for platformId={}", getPlatform(), platformId);
                throw new FetchFailedException(getPlatform(), platformId);
            }
            Mapping mapping = buildMapping(jsonNode);
            // 平台返回其他条目或缺少 platformId 时，保存它并不能更新请求的 Mapping。
            if (!platformId.equals(mapping.getPlatformId())) {
                log.warn("{} returned platformId={} for requested platformId={}",
                        getPlatform(), mapping.getPlatformId(), platformId);
                throw new FetchFailedException(getPlatform(), platformId);
            }
            return mappingRepoService.saveOrUpdate(mapping);
        } catch (WebClientResponseException e) {
            log.warn("Failed to fetch mapping from {} for platformId={}: {} {}",
                    getPlatform(), platformId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FetchFailedException(getPlatform(), platformId, e);
        } catch (FetchFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching mapping from {} for platformId={}: {}",
                    getPlatform(), platformId, e.getMessage());
            throw new FetchFailedException(getPlatform(), platformId, e);
        }
    }
}

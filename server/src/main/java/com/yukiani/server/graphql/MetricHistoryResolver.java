package com.yukiani.server.graphql;

import com.yukiani.server.generated.types.Anime;
import com.yukiani.server.generated.types.AnimeMetricPoint;
import com.yukiani.server.generated.types.Mapping;
import com.yukiani.server.generated.types.MappingMetricPoint;
import com.yukiani.server.service.MetricTimelineService;
import com.yukiani.server.entity.PlatformKey;
import jakarta.annotation.Resource;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 按需解析 Anime 和 Mapping 的指标历史字段。
 *
 * <p>日期按 Asia/Shanghai 时区划分，归一化与加权使用当前平台配置。</p>
 */
@Controller
public class MetricHistoryResolver {

    @Resource
    private MetricTimelineService metricTimelineService;

    @Resource
    private GraphQLTypeMapper graphQLTypeMapper;

    /**
     * 查询区间内有观测日期的综合指标，按日期升序。
     *
     * <p>各平台沿用最近一次观测，不在区间起点补点；起点取值可通过 {@link #animeMetricAt} 查询。</p>
     *
     * @param anime 父级动画，使用其 mappings 确定参与计算的平台序列
     * @param from  起始日期，ISO 日期格式（如 2026-09-14），含当天
     * @param to    结束日期，ISO 日期格式，含当天；区间最多 90 天
     * @return 综合指标序列；区间内无观测时为空列表
     * @throws IllegalArgumentException 日期无效、from 晚于 to 或区间超过 90 天
     */
    @SchemaMapping(typeName = "Anime", field = "metricHistory")
    public List<AnimeMetricPoint> animeMetricHistory(
            Anime anime,
            @Argument String from,
            @Argument String to) {

        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");
        requireValidRange(fromDate, toDate);

        return metricTimelineService.animeHistory(platformKeys(anime), fromDate, toDate)
                .stream()
                .map(graphQLTypeMapper::toGraphQLAnimeMetricPoint)
                .toList();
    }

    /**
     * 查询指定日期结束时的综合指标，各平台采用该日结束前的最新观测。
     *
     * @param anime 父级动画，使用其 mappings 确定参与计算的平台序列
     * @param at    目标日期，ISO 日期格式
     * @return 综合指标，date 为参与计算的最新观测日期，可能早于 at；无观测时为 {@code null}
     * @throws IllegalArgumentException 日期无效
     */
    @SchemaMapping(typeName = "Anime", field = "metricAt")
    public AnimeMetricPoint animeMetricAt(
            Anime anime,
            @Argument String at) {

        var point = metricTimelineService.animeMetricAt(platformKeys(anime), parseDate(at, "at"));
        return graphQLTypeMapper.toGraphQLAnimeMetricPoint(point);
    }

    /**
     * 查询区间内的逐日平台观测，按日期升序。
     *
     * <p>同日多次观测取最后一次，不补点。</p>
     *
     * @param mapping 父级 Mapping，使用其 platformKey 确定平台序列
     * @param from    起始日期，ISO 日期格式（如 2026-09-14），含当天
     * @param to      结束日期，ISO 日期格式，含当天；区间最多 90 天
     * @return 平台观测序列；区间内无观测时为空列表
     * @throws IllegalArgumentException 日期无效、from 晚于 to 或区间超过 90 天
     */
    @SchemaMapping(typeName = "Mapping", field = "metricHistory")
    public List<MappingMetricPoint> mappingMetricHistory(
            Mapping mapping,
            @Argument String from,
            @Argument String to) {

        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");
        requireValidRange(fromDate, toDate);

        return metricTimelineService.mappingHistory(platformKey(mapping), fromDate, toDate)
                .stream()
                .map(graphQLTypeMapper::toGraphQLMappingMetricPoint)
                .toList();
    }

    /**
     * 查询指定日期结束时的最新平台观测。
     *
     * @param mapping 父级 Mapping，使用其 platformKey 确定平台序列
     * @param at      目标日期，ISO 日期格式
     * @return 平台观测，date 为观测日期，可能早于 at；无观测时为 {@code null}
     * @throws IllegalArgumentException 日期无效
     */
    @SchemaMapping(typeName = "Mapping", field = "metricAt")
    public MappingMetricPoint mappingMetricAt(
            Mapping mapping,
            @Argument String at) {

        var point = metricTimelineService.mappingMetricAt(platformKey(mapping), parseDate(at, "at"));
        return graphQLTypeMapper.toGraphQLMappingMetricPoint(point);
    }

    /** 提取动画各平台的序列键。 */
    private List<PlatformKey> platformKeys(Anime anime) {
        return anime.getMappings().stream()
                .map(this::platformKey)
                .toList();
    }

    private PlatformKey platformKey(Mapping mapping) {
        return graphQLTypeMapper.toPlatformKey(mapping.getPlatformKey());
    }

    /** 解析 ISO 日期，解析失败时在异常中标明参数名。 */
    private LocalDate parseDate(String value, String argumentName) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "参数 %s 不是合法的 ISO 日期: %s".formatted(argumentName, value));
        }
    }

    private void requireValidRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("参数 from 不能晚于 to");
        }
        if (ChronoUnit.DAYS.between(from, to) >= 90) {
            throw new IllegalArgumentException("查询区间最多包含 90 天");
        }
    }
}

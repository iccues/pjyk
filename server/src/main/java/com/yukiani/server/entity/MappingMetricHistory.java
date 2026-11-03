package com.yukiani.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 外部平台 Mapping 的原始评分与热度快照。
 *
 * <p>快照通过 {@link PlatformKey} 归属于平台条目，不与 Mapping 建立外键，
 * 因此 Mapping 删除后仍会保留。归一化指标不随快照存储，查询时根据当前配置计算。</p>
 */
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class MappingMetricHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long historyId;

    /** 历史快照所属的平台条目标识。 */
    @Embedded
    PlatformKey platformKey;

    /** 平台原始评分；本次采样无有效评分时为 {@code null}。 */
    Double rawScore;

    /** 平台原始热度；本次采样未提供时为 {@code null}。 */
    Double rawPopularity;

    /** 采样时间，取自 Mapping 的 updateTime。 */
    Instant recordedAt;

    public MappingMetricHistory(PlatformKey platformKey,
                                Double rawScore, Double rawPopularity, Instant recordedAt) {
        this.platformKey = platformKey;
        this.rawScore = rawScore;
        this.rawPopularity = rawPopularity;
        this.recordedAt = recordedAt;
    }
}

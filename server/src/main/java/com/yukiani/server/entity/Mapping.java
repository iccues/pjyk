package com.yukiani.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;

import java.time.Instant;

/**
 * 外部平台 Mapping Entity，保存 Anime 关联及平台原始指标。
 */
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class Mapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mappingId;

    @Version
    Integer version = 0;

    /** 关联的动画主数据；尚未关联时为 {@code null}。 */
    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    Anime anime;

    /**
     * 获取关联动画的 animeId，避免触发 Anime Entity 的完整序列化。
     *
     * @return animeId；未关联动画时返回 {@code null}
     */
    public Long getAnimeId() {
        if (anime == null) return null;
        return anime.getAnimeId();
    }

    /** 与 {@link #platformId} 共同组成平台 Mapping 的 natural key。 */
    @NaturalId
    @Enumerated(EnumType.STRING)
    Platform platform;

    /** 来源平台中的 platformId，与 {@link #platform} 共同组成 natural key。 */
    @NaturalId
    String platformId;

    /** 来源平台返回的原始评分，无有效评分时为 {@code null}。 */
    Double rawScore;

    /** 来源平台评分经平台规则处理并转换到统一尺度后的结果，无有效评分时为 {@code null}。 */
    Double normalizedScore;

    /** 来源平台返回的原始热度指标。 */
    Double rawPopularity;

    /** 按来源平台热度基准转换后的统一尺度热度。 */
    Double normalizedPopularity;

    /** 最近一次抓取到的平台标题、封面和开播日期快照。 */
    @Embedded
    MappingInfo mappingInfo;

    /** 最近一次成功抓取并保存该平台 Mapping 的时间。 */
    Instant updateTime;

    /**
     * 创建外部平台 Mapping 并记录首次更新时间。
     */
    public Mapping(Platform platform, String platformId, MappingInfo mappingInfo) {
        this.platform = platform;
        this.platformId = platformId;
        this.mappingInfo = mappingInfo;
        this.updateTime = Instant.now();
    }

    /**
     * 基于主键判定实体相等，避免 Anime 反向引用参与比较导致的递归。
     *
     * <p>主键为空的瞬时实体仅与自身相等，从而区分尚未持久化的不同对象。</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mapping other)) return false;
        Long id = getMappingId();
        return id != null && id.equals(other.getMappingId());
    }

    /** 返回按类固定的哈希值，保证主键赋值前后及代理对象间的一致性。 */
    @Override
    public int hashCode() {
        return Mapping.class.hashCode();
    }
}

package com.yukiani.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 动画主数据 Entity，聚合来自多个外部平台的 Mapping 信息。
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Anime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long animeId;

    @Version
    Integer version = 0;

    /** 默认保持非空，历史数据加载后也会补齐空对象。 */
    @Embedded
    AnimeTitles title = new AnimeTitles();

    /** 加载历史数据后补齐可能为空的标题对象。 */
    @PostLoad
    protected void postLoad() {
        if (title == null) {
            title = new AnimeTitles();
        }
    }

    String coverImage;

    /** 动画开播日期；平台未提供且未人工维护时为 {@code null}。 */
    LocalDate startDate;

    /** 各平台归一化评分按配置权重计算的加权平均值，无有效评分时为 {@code null}。 */
    Double averageScore;

    /** 各平台归一化热度按配置权重累加得到的综合热度。 */
    Double popularity;

    /** 新动画默认为待审核，只有审核通过后才会出现在公开查询中。 */
    @Enumerated(EnumType.STRING)
    ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @OneToMany(mappedBy = "anime",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            fetch = FetchType.LAZY)
    @BatchSize(size = 60)
    List<Mapping> mappings = new ArrayList<>();

    /**
     * 维持 Anime 与 Mapping 的双向关联；Mapping 原有关联会先被解除。
     */
    public void addMapping(Mapping mapping) {
        if (mapping.getAnime() != null) {
            mapping.getAnime().removeMapping(mapping);
        }
        this.mappings.add(mapping);
        mapping.setAnime(this);
    }

    /**
     * 解除 Anime 与 Mapping 的双向关联。
     */
    public void removeMapping(Mapping mapping) {
        this.mappings.remove(mapping);
        mapping.setAnime(null);
    }

    /**
     * 查找动画在指定平台上的 Mapping。
     *
     * @return 平台 Mapping；不存在时返回 {@code null}
     */
    public Mapping getMappingByPlatform(Platform platform) {
        for (Mapping mapping : getMappings()) {
            if (mapping.getPlatform() == platform) {
                return mapping;
            }
        }
        return null;
    }

    /**
     * 基于主键判定实体相等，避免关联集合参与比较导致的递归。
     *
     * <p>主键为空的瞬时实体仅与自身相等，从而区分尚未持久化的不同对象。</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Anime other)) return false;
        Long id = getAnimeId();
        return id != null && id.equals(other.getAnimeId());
    }

    /** 返回按类固定的哈希值，保证主键赋值前后及代理对象间的一致性。 */
    @Override
    public int hashCode() {
        return Anime.class.hashCode();
    }
}

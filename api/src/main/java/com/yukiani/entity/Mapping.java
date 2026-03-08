package com.yukiani.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class Mapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mappingId;

    @Version
    Integer version = 0;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "anime_id")
    Anime anime;

    public Long getAnimeId() {
        if (anime == null) return null;
        return anime.getAnimeId();
    }

    @NaturalId
    @Enumerated(EnumType.STRING)
    Platform sourcePlatform;

    @NaturalId
    String platformId;

    Double rawScore;
    Double normalizedScore;

    Double rawPopularity;
    Double normalizedPopularity;

    @Embedded
    MappingInfo mappingInfo;

    Instant updateTime;

    public Mapping(Platform sourcePlatform, String platformId, MappingInfo mappingInfo) {
        this.sourcePlatform = sourcePlatform;
        this.platformId = platformId;
        this.mappingInfo = mappingInfo;
        this.updateTime = Instant.now();
    }
}

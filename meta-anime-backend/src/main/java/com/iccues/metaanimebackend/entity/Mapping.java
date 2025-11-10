package com.iccues.metaanimebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class Mapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mappingId;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "anime_id")
    Anime anime;

    public Long getAnimeId() {
        if (anime == null) return null;
        return anime.getAnimeId();
    }

//    public void setAnimeId(Long animeId) {}

    @NaturalId
    String sourcePlatform;

    @NaturalId
    String platformId;

    Double rawScore;
    Double normalizedScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    JsonNode rawJSON;

    Instant updateTime;

    public Mapping(String sourcePlatform, String platformId, JsonNode rawJSON) {
        this.sourcePlatform = sourcePlatform;
        this.platformId = platformId;
        this.rawJSON = rawJSON;
        this.updateTime = Instant.now();
    }
}

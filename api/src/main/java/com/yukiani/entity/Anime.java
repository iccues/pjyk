package com.yukiani.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Anime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long animeId;

    @Version
    Integer version = 0;

    @Embedded
    AnimeTitles title;

    String coverImage;

    LocalDate startDate;

    Double averageScore;

    Double popularity;

    @Enumerated(EnumType.STRING)
    ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @OneToMany(mappedBy = "anime",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            fetch = FetchType.LAZY)
    List<Mapping> mappings = new ArrayList<>();

    public void addMapping(Mapping mapping) {
        if (mapping.getAnime() != null) {
            mapping.getAnime().removeMapping(mapping);
        }
        this.mappings.add(mapping);
        mapping.setAnime(this);
    }

    public void removeMapping(Mapping mapping) {
        this.mappings.remove(mapping);
        mapping.setAnime(null);
    }

    public Mapping getMappingByPlatform(Platform platform) {
        for (Mapping mapping : getMappings()) {
            if (mapping.getSourcePlatform() == platform) {
                return mapping;
            }
        }
        return null;
    }
}

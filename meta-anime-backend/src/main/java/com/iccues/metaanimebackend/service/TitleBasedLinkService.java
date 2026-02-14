package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.MappingInfo;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TitleBasedLinkService {
    @Resource
    protected AnimeRepoService animeRepoService;

    @Resource
    protected AnimeRepository animeRepository;
    @Resource
    protected MappingRepository mappingRepository;

    @Resource
    AnimeAggregationService animeAggregationService;

    @Transactional
    Anime findOrCreateAnime(MappingInfo mappingInfo) {
        Anime existing = animeRepoService.findAnime(
                mappingInfo.getStartDate(),
                mappingInfo.getTitle()
        );

        if (existing != null) {
            return existing;
        }

        Anime anime = animeRepoService.createAnime(mappingInfo);
        return animeRepository.save(anime);
    }

    @Transactional
    public void linkMappingToAnime(Mapping mapping) {
        if (mapping.getAnime() == null && mapping.getMappingInfo().getStartDate() != null) {
            Anime anime = findOrCreateAnime(mapping.getMappingInfo());
            animeAggregationService.addMappingIfAbsent(anime, mapping);
            animeRepository.save(anime);
        }
    }

    @Transactional
    public void linkAllOrphanedMappings() {
        List<Mapping> mappings = mappingRepository.findAllByAnimeIsNullAndMappingInfo_StartDateIsNotNull();
        for (Mapping mapping : mappings) {
            linkMappingToAnime(mapping);
        }
    }
}

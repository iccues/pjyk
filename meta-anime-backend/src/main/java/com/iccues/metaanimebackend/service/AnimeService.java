package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AnimeService {

    @Resource
    AnimeRepository repo;

    @Resource
    TitleMatchingService titleMatchingService;

    List<Anime> findAnimeAroundDate(LocalDate date) {
        if (date == null) {
            log.debug("Date is null, returning empty list");
            return List.of();
        }
        LocalDate start = date.minusDays(1);
        LocalDate end = date.plusDays(1);
        log.debug("Finding anime between {} and {}", start, end);
        return repo.findByStartDateBetween(start, end);
    }

    public Anime findAnime(LocalDate date, AnimeTitles titles) {
        log.debug("Finding anime with date: {} and title: {}", date, titles.getTitleNative());
        List<Anime> list = findAnimeAroundDate(date);

        for (Anime anime : list) {
            if (titleMatchingService.areTitlesSimilar(anime.getTitle().getTitleNative(), titles.getTitleNative())) {
                log.debug("Found matching anime: {}, merging titles", anime.getTitle().getTitleNative());
                anime.getTitle().merge(titles);
                return anime;
            }
        }

        log.debug("No matching anime found, creating new anime");
        Anime anime = new Anime();
        anime.setStartDate(date);
        anime.setTitle(titles);
        return anime;
    }
}

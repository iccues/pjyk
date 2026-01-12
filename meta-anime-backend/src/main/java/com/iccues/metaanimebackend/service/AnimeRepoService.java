package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnimeRepoService {

    @Resource
    AnimeRepository repo;

    @Resource
    TitleMatchingService titleMatchingService;

    List<Anime> findAnimeAroundDate(LocalDate date) {
        if (date == null) {
            return List.of();
        }
        LocalDate start = date.minusDays(1);
        LocalDate end = date.plusDays(1);
        return repo.findByStartDateBetween(start, end);
    }

    @Transactional
    public Anime findAnime(LocalDate date, AnimeTitles titles) {
        List<Anime> list = findAnimeAroundDate(date);

        for (Anime anime : list) {
            if (titleMatchingService.areTitlesSimilar(anime.getTitle().getTitleNative(), titles.getTitleNative())) {
                anime.getTitle().merge(titles);
                return anime;
            }
        }

        Anime anime = new Anime();
        anime.setStartDate(date);
        anime.setTitle(titles);
        return anime;
    }
}

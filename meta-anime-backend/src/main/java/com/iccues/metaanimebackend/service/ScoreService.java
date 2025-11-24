package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ScoreService {

    @Resource
    AnimeRepository animeRepository;

    @Transactional
    public void calculateAllAverageScore() {
        log.info("Starting to calculate average scores for all anime");
        List<Anime> list = animeRepository.findAll();
        log.debug("Found {} anime to process", list.size());

        for (Anime anime : list) {
            calculateAverageScore(anime);
        }
        log.info("Finished calculating average scores for all anime");
    }

    @Transactional
    public void calculateAverageScore(Anime anime) {
        String animeTitle = (anime.getTitle() != null && anime.getTitle().getTitleNative() != null)
                          ? anime.getTitle().getTitleNative()
                          : "id:" + anime.getAnimeId();
        log.debug("Calculating average score for anime: {}", animeTitle);
        double totalScore = 0.0;
        int i = 0;

        for (Mapping mapping : anime.getMappings()) {
            Double normalizedScore = mapping.getNormalizedScore();
            if (normalizedScore != null && normalizedScore > 0) {
                totalScore += normalizedScore;
                i++;
            }
        }

        if (i > 0) {
            double avgScore = totalScore / i;
            anime.setAverageScore(avgScore);
            log.debug("Average score for anime '{}': {} (from {} mappings)",
                      animeTitle, avgScore, i);
        } else  {
            anime.setAverageScore(null);
            log.debug("No valid scores found for anime '{}'", animeTitle);
        }
    }
}

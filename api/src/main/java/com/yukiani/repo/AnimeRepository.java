package com.yukiani.repo;

import com.yukiani.entity.Anime;
import com.yukiani.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long>, JpaSpecificationExecutor<Anime> {
    List<Anime> findByStartDateBetween(LocalDate startDateAfter, LocalDate startDateBefore);
    List<Anime> findAllByReviewStatus(ReviewStatus reviewStatus);
    void deleteAllByReviewStatusIsNot(ReviewStatus reviewStatus);

    Anime findByAnimeIdAndReviewStatus(Long animeId, ReviewStatus reviewStatus);
}

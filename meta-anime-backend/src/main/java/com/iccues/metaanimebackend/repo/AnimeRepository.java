package com.iccues.metaanimebackend.repo;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.ReviewStatus;
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
}

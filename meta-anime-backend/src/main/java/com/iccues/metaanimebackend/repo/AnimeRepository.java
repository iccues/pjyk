package com.iccues.metaanimebackend.repo;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {
    List<Anime> findByStartDateBetween(LocalDate startDateAfter, LocalDate startDateBefore);

    @Query("SELECT a FROM Anime a WHERE a.reviewStatus = 'APPROVED' ORDER BY a.averageScore DESC NULLS LAST")
    List<Anime> findAllDisplay();

    List<Anime> findByReviewStatus(ReviewStatus reviewStatus);
}

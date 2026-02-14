package com.iccues.metaanimebackend.spec;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.LocalDateRange;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.entity.SortBy;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AnimeSpec {
    public static Specification<Anime> reviewStatusEquals(ReviewStatus reviewStatus) {
        return (root, query, criteriaBuilder) -> {
            if (reviewStatus == null) return null;
            return criteriaBuilder.equal(root.get("reviewStatus"), reviewStatus);
        };
    }

    public static Specification<Anime> startDateBetween(LocalDateRange range) {
        return (root, query, criteriaBuilder) -> {
            if (range == null) return null;
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), range.start()));
            predicates.add(criteriaBuilder.lessThan(root.get("startDate"), range.end()));
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Anime> orderByScoreNullLast() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) return null;
            query.orderBy(
                    criteriaBuilder.asc(criteriaBuilder.isNull(root.get("averageScore"))),
                    criteriaBuilder.desc(root.get("averageScore")));
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Anime> orderByPopularityNullLast() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) return null;
            query.orderBy(
                    criteriaBuilder.asc(criteriaBuilder.isNull(root.get("popularity"))),
                    criteriaBuilder.desc(root.get("popularity")));
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Anime> orderBy(SortBy sortBy) {
        return switch (sortBy) {
            case SortBy.SCORE -> orderByScoreNullLast();
            case SortBy.POPULARITY -> orderByPopularityNullLast();
        };
    }

    public static Specification<Anime> orderById() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) return null;
            query.orderBy(criteriaBuilder.asc(root.get("id")));
            return criteriaBuilder.conjunction();
        };
    }
}

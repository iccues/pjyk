package com.yukiani.spec;

import com.yukiani.entity.Anime;
import com.yukiani.entity.LocalDateRange;
import com.yukiani.entity.ReviewStatus;
import com.yukiani.entity.SortBy;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
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

    public static Specification<Anime> similarTitle(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) return null;

            Path<?> titlePath = root.get("title");
            float threshold = 0.1f;

            List<String> fields = List.of("titleNative", "titleRomaji", "titleEn", "titleCn");

            // WHERE: 任意 title 字段相似度超过阈值
            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                Expression<Float> sim = cb.function(
                        "similarity", Float.class,
                        titlePath.get(field), cb.literal(query));
                predicates.add(cb.greaterThan(sim, threshold));
            }

            // ORDER BY GREATEST(similarity(...)) DESC
            if (cq != null && !Long.class.equals(cq.getResultType())) {
                @SuppressWarnings("unchecked")
                Expression<Float>[] simExprs = fields.stream()
                        .map(field -> cb.function(
                                "similarity", Float.class,
                                titlePath.get(field), cb.literal(query)))
                        .toArray(Expression[]::new);
                Expression<Float> greatest = cb.function("GREATEST", Float.class, simExprs);
                cq.orderBy(cb.desc(greatest));
            }

            return cb.or(predicates.toArray(Predicate[]::new));
        };
    }
}

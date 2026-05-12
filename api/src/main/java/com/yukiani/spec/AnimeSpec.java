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

    public static Specification<Anime> similarTitle(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) return null;

            Path<?> titlePath = root.get("title");
            float threshold = 0.0f;

            List<String> fields = List.of("titleNative", "titleRomaji", "titleEn", "titleCn");

            // WHERE: 任意 title 字段相似度超过阈值，或包含子串（解决短词嵌入长词中 trigram 无交集的问题）
            String likePattern = "%" + keyword + "%";
            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                Expression<Float> sim = criteriaBuilder.function(
                        "word_similarity", Float.class,
                        criteriaBuilder.literal(keyword), titlePath.get(field));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.greaterThan(sim, threshold),
                        criteriaBuilder.like(titlePath.get(field), likePattern)
                ));
            }

            // ORDER BY GREATEST(similarity(...)) DESC
            if (query != null && !Long.class.equals(query.getResultType())) {
                @SuppressWarnings("unchecked")
                Expression<Float>[] simExprs = fields.stream()
                        .map(field -> criteriaBuilder.function(
                                "word_similarity", Float.class,
                                criteriaBuilder.literal(keyword), titlePath.get(field)))
                        .toArray(Expression[]::new);
                Expression<Float> greatest = criteriaBuilder.function("GREATEST", Float.class, simExprs);
                query.orderBy(criteriaBuilder.desc(greatest));
            }

            return criteriaBuilder.or(predicates.toArray(Predicate[]::new));
        };
    }
}

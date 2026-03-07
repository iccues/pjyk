package com.iccues.metaanimebackend.graphql;

import com.iccues.generated.types.Season;
import com.iccues.generated.types.SortBy;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.generated.types.AnimePage;
import com.iccues.metaanimebackend.service.AnimeQueryService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL Query Resolver for Anime queries
 */
@Controller
public class AnimeQueryResolver {

    @Resource
    private AnimeQueryService animeQueryService;

    @Resource
    private GraphQLTypeMapper graphQLTypeMapper;

    @QueryMapping
    public AnimePage animeList(
            @Argument Integer year,
            @Argument Season season,
            @Argument Integer pageNumber,
            @Argument Integer pageSize,
            @Argument SortBy sortBy) {

        Page<Anime> animePage = animeQueryService.getAnimeList(
                year,
                graphQLTypeMapper.toSeason(season),
                pageNumber,
                pageSize,
                graphQLTypeMapper.toSortBy(sortBy)
        );

        return graphQLTypeMapper.toAnimePage(animePage);
    }
}

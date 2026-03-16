package com.yukiani.graphql;

import com.yukiani.generated.types.Season;
import com.yukiani.generated.types.SortBy;
import com.yukiani.generated.types.Anime;
import com.yukiani.generated.types.AnimePage;
import com.yukiani.service.AnimeQueryService;
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

        var animePage = animeQueryService.getAnimeList(
                year,
                graphQLTypeMapper.toSeason(season),
                pageNumber,
                pageSize,
                graphQLTypeMapper.toSortBy(sortBy)
        );

        return graphQLTypeMapper.toAnimePage(animePage);
    }

    @QueryMapping
    public AnimePage animeListBySearch(
            @Argument String query,
            @Argument Integer pageNumber,
            @Argument Integer pageSize
    ) {
        var animePage = animeQueryService.getAnimeListBySearch(
                query,
                pageNumber,
                pageSize
        );

        return graphQLTypeMapper.toAnimePage(animePage);
    }

    @QueryMapping
    public Anime anime(
            @Argument Long animeId
    ) {
        var anime = animeQueryService.getAnimeById(animeId);
        return graphQLTypeMapper.toGraphQLAnime(anime);
    }
}

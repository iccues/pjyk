package com.yukiani.server.graphql;

import com.yukiani.server.generated.types.Season;
import com.yukiani.server.generated.types.SortBy;
import com.yukiani.server.generated.types.Anime;
import com.yukiani.server.generated.types.AnimePage;
import com.yukiani.server.service.AnimeQueryService;
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
            @Argument String keyword,
            @Argument Integer pageNumber,
            @Argument Integer pageSize
    ) {
        var animePage = animeQueryService.getAnimeListBySearch(
                keyword,
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

package com.yukiani.server.graphql;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.PlatformKey;
import com.yukiani.server.generated.types.AnimePage;
import com.yukiani.server.generated.types.PageInfo;
import com.yukiani.server.entity.Season;
import com.yukiani.server.entity.SortBy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 使用 MapStruct 在 JPA Entity 与 GraphQL Type 之间转换，并统一处理 ID 和 Date 格式。
 */
@Mapper(componentModel = "spring")
public interface GraphQLTypeMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    SortBy toSortBy(com.yukiani.server.generated.types.SortBy sortBy);
    Season toSeason(com.yukiani.server.generated.types.Season season);

    default AnimePage toAnimePage(Page<Anime> page) {
        List<com.yukiani.server.generated.types.Anime> content =
                page.getContent().stream()
                        .map(this::toGraphQLAnime)
                        .toList();

        return AnimePage.newBuilder()
                .content(content)
                .pageInfo(toPageInfo(page))
                .build();
    }

    PageInfo toPageInfo(Page<?> page);

    @Mapping(target = "animeId", source = "animeId", qualifiedByName = "longToString")
    @Mapping(target = "startDate", source = "startDate", qualifiedByName = "localDateToString")
    com.yukiani.server.generated.types.Anime toGraphQLAnime(Anime anime);

    @Mapping(target = "mappingId", source = "mappingId", qualifiedByName = "longToString")
    com.yukiani.server.generated.types.Mapping toGraphQLMapping(
            com.yukiani.server.entity.Mapping mapping);

    com.yukiani.server.generated.types.PlatformKey toGraphQLPlatformKey(PlatformKey platformKey);

    @Named("longToString")
    default String longToString(Long id) {
        return id != null ? id.toString() : null;
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}

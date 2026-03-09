package com.yukiani.graphql;

import com.yukiani.entity.Anime;
import com.yukiani.generated.types.AnimePage;
import com.yukiani.generated.types.PageInfo;
import com.yukiani.entity.Season;
import com.yukiani.entity.SortBy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MapStruct mapper for converting JPA entities to GraphQL types.
 * Handles automatic field mapping and custom conversions (ID: Long→String, Date: LocalDate→String).
 */
@Mapper(componentModel = "spring")
public interface GraphQLTypeMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    SortBy toSortBy(com.yukiani.generated.types.SortBy sortBy);
    Season toSeason(com.yukiani.generated.types.Season season);

    default AnimePage toAnimePage(Page<Anime> page) {
        List<com.yukiani.generated.types.Anime> content =
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
    com.yukiani.generated.types.Anime toGraphQLAnime(Anime anime);

    @Mapping(target = "mappingId", source = "mappingId", qualifiedByName = "longToString")
    com.yukiani.generated.types.Mapping toGraphQLMapping(
            com.yukiani.entity.Mapping mapping);

    @Named("longToString")
    default String longToString(Long id) {
        return id != null ? id.toString() : null;
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}

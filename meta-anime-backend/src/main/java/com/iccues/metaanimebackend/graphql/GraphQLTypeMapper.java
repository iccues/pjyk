package com.iccues.metaanimebackend.graphql;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.generated.types.AnimePage;
import com.iccues.generated.types.PageInfo;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.entity.SortBy;
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

    SortBy toSortBy(com.iccues.generated.types.SortBy sortBy);
    Season toSeason(com.iccues.generated.types.Season season);

    default AnimePage toAnimePage(Page<Anime> page) {
        List<com.iccues.generated.types.Anime> content =
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
    com.iccues.generated.types.Anime toGraphQLAnime(Anime anime);

    @Mapping(target = "mappingId", source = "mappingId", qualifiedByName = "longToString")
    com.iccues.generated.types.Mapping toGraphQLMapping(
            com.iccues.metaanimebackend.entity.Mapping mapping);

    @Named("longToString")
    default String longToString(Long id) {
        return id != null ? id.toString() : null;
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}

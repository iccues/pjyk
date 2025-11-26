package com.iccues.metaanimebackend.mapper;

import com.iccues.metaanimebackend.dto.admin.*;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class AdminAnimeMapper {

    @Resource
    protected FetchService fetchService;

    public abstract List<AdminMappingDTO> toMappingDtoList(List<Mapping> mappingList);

    public abstract AdminAnimeDTO toAnimeDto(Anime anime);
    public abstract List<AdminAnimeDTO> toAnimeDtoList(List<Anime> animeList);

    @org.mapstruct.Mapping(target = "mappingInfo", expression = "java(extractMappingInfo(mapping))")
    public abstract AdminMappingDTO toMappingDto(Mapping mapping);

    protected MappingInfo extractMappingInfo(Mapping mapping) {
        if (mapping == null || mapping.getSourcePlatform() == null) {
            return null;
        }

        AbstractAnimeFetchService service = fetchService.getFetchServiceByName(mapping.getSourcePlatform());
        if (service == null) {
            return null;
        }

        return service.getMappingInfo(mapping);
    }

    @org.mapstruct.Mapping(target = "animeId", ignore = true)
    @org.mapstruct.Mapping(target = "version", ignore = true)
    @org.mapstruct.Mapping(target = "averageScore", ignore = true)
    @org.mapstruct.Mapping(target = "reviewStatus", ignore = true)
    @org.mapstruct.Mapping(target = "mappings", ignore = true)
    public abstract Anime requestToAnime(AnimeCreateRequest animeCreateRequest);

    @org.mapstruct.Mapping(target = "version", ignore = true)
    @org.mapstruct.Mapping(target = "averageScore", ignore = true)
    @org.mapstruct.Mapping(target = "mappings", ignore = true)
    @org.mapstruct.Mapping(target = "animeId", ignore = true)
    public abstract void updateAnimeByRequest(AnimeUpdateRequest animeUpdateRequest, @MappingTarget Anime anime);
}

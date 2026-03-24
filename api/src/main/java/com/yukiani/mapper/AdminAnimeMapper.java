package com.yukiani.mapper;

import com.yukiani.dto.admin.*;
import com.yukiani.entity.Anime;
import com.yukiani.entity.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class AdminAnimeMapper {

    public abstract List<AdminMappingDTO> toMappingDtoList(List<Mapping> mappingList);

    public abstract AdminAnimeDTO toAnimeDto(Anime anime);
    public abstract List<AdminAnimeDTO> toAnimeDtoList(List<Anime> animeList);

    public abstract AdminMappingDTO toMappingDto(Mapping mapping);

    @org.mapstruct.Mapping(target = "animeId", ignore = true)
    @org.mapstruct.Mapping(target = "version", ignore = true)
    @org.mapstruct.Mapping(target = "averageScore", ignore = true)
    @org.mapstruct.Mapping(target = "reviewStatus", ignore = true)
    @org.mapstruct.Mapping(target = "mappings", ignore = true)
    @org.mapstruct.Mapping(target = "popularity", ignore = true)
    public abstract Anime requestToAnime(AnimeCreateRequest animeCreateRequest);

    @org.mapstruct.Mapping(target = "version", ignore = true)
    @org.mapstruct.Mapping(target = "averageScore", ignore = true)
    @org.mapstruct.Mapping(target = "mappings", ignore = true)
    @org.mapstruct.Mapping(target = "animeId", ignore = true)
    @org.mapstruct.Mapping(target = "popularity", ignore = true)
    public abstract void updateAnimeByRequest(AnimeUpdateRequest animeUpdateRequest, @MappingTarget Anime anime);
}

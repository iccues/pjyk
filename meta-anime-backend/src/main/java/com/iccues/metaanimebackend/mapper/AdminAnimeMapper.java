package com.iccues.metaanimebackend.mapper;

import com.iccues.metaanimebackend.dto.admin.AdminAnimeDTO;
import com.iccues.metaanimebackend.dto.admin.AdminMappingDTO;
import com.iccues.metaanimebackend.dto.admin.AnimeCreateRequest;
import com.iccues.metaanimebackend.dto.admin.AnimeUpdateRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AdminAnimeMapper {
    List<AdminMappingDTO> toMappingDtoList(List<Mapping> mappingList);

    AdminAnimeDTO toAnimeDto(Anime anime);
    List<AdminAnimeDTO> toAnimeDtoList(List<Anime> animeList);

    AdminMappingDTO toMappingDto(Mapping mapping);

    Anime requestToAnime(AnimeCreateRequest animeCreateRequest);

    @org.mapstruct.Mapping(target = "animeId", ignore = true)
    void updateAnimeByRequest(AnimeUpdateRequest animeUpdateRequest, @MappingTarget Anime anime);
}

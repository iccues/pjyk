package com.iccues.metaanimebackend.mapper;

import com.iccues.metaanimebackend.dto.admin.AdminAnimeDTO;
import com.iccues.metaanimebackend.dto.admin.AdminMappingDTO;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminAnimeMapper {
    List<AdminMappingDTO> toMappingDtoList(List<Mapping> mappingList);

    AdminAnimeDTO toAnimeDto(Anime anime);
    List<AdminAnimeDTO> toAnimeDtoList(List<Anime> animeList);

    AdminMappingDTO toMappingDto(Mapping mapping);
}

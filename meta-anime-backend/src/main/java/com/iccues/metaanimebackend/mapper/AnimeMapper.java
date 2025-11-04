package com.iccues.metaanimebackend.mapper;

import com.iccues.metaanimebackend.dto.AnimeDTO;
import com.iccues.metaanimebackend.entity.Anime;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnimeMapper {
    AnimeMapper INSTANCE = Mappers.getMapper(AnimeMapper.class);

    AnimeDTO toDto(Anime anime);
    List<AnimeDTO> toDtoList(List<Anime> animeList);

    Anime toEntity(AnimeDTO dto);
}

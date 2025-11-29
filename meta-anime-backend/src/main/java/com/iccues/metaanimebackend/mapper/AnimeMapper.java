package com.iccues.metaanimebackend.mapper;

import com.iccues.metaanimebackend.dto.AnimeDTO;
import com.iccues.metaanimebackend.entity.Anime;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnimeMapper {
    AnimeDTO toDto(Anime anime);
}

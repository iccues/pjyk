package com.yukiani.server.repo;

import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MappingRepository extends JpaRepository<Mapping, Long> {
    Mapping findBySourcePlatformAndPlatformId(Platform sourcePlatform, String platformId);

    List<Mapping> findAllByAnimeIsNull();
    List<Mapping> findAllByAnimeIsNullAndMappingInfo_StartDateIsNotNull();

    void deleteAllByAnimeIsNull();
}

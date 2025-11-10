package com.iccues.metaanimebackend.repo;

import com.iccues.metaanimebackend.entity.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MappingRepository extends JpaRepository<Mapping, Long> {
    Mapping findBySourcePlatformAndPlatformId(String sourcePlatform, String platformId);

    List<Mapping> findAllByAnimeIsNull();
}

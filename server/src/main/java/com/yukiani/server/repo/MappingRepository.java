package com.yukiani.server.repo;

import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MappingRepository extends JpaRepository<Mapping, Long> {
    Mapping findBySourcePlatformAndPlatformId(Platform sourcePlatform, String platformId);

    List<Mapping> findAllByAnimeIsNull();
    List<Mapping> findAllByAnimeIsNullAndMappingInfo_StartDateIsNotNull();

    void deleteAllByAnimeIsNull();

    /**
     * 查询同步候选：所属动画处于指定审核状态、今天还没有成功抓取过的 Mapping。
     *
     * @param todayStart 今天零点对应的时刻；此后成功抓取过的 Mapping 今天不会到期
     */
    @Query("""
            select new com.yukiani.server.repo.SyncCandidate(
                m.mappingId, a.animeId, a.startDate, m.sourcePlatform, m.platformId, m.updateTime)
            from Mapping m join m.anime a
            where a.reviewStatus = :reviewStatus
              and m.updateTime < :todayStart
            """)
    List<SyncCandidate> findSyncCandidates(ReviewStatus reviewStatus, Instant todayStart);
}

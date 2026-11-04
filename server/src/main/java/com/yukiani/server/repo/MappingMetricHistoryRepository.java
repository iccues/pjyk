package com.yukiani.server.repo;

import com.yukiani.server.entity.MappingMetricHistory;
import com.yukiani.server.entity.PlatformKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface MappingMetricHistoryRepository extends JpaRepository<MappingMetricHistory, Long> {
    /** 查询 [from, until) 内的观测，按序列、时间和 historyId 升序。 */
    @Transactional(readOnly = true)
    @Query("""
            select h from MappingMetricHistory h
            where h.platformKey in :keys and h.recordedAt >= :from and h.recordedAt < :until
            order by h.platformKey.platform, h.platformKey.platformId, h.recordedAt, h.historyId
            """)
    List<MappingMetricHistory> querySeries(@Param("keys") Collection<PlatformKey> keys,
                                           @Param("from") Instant from,
                                           @Param("until") Instant toExclusive);

    /** 查询 until 之前各序列的最新观测，时间相同时取 historyId 最大者。 */
    @Transactional(readOnly = true)
    @Query("""
            select h from MappingMetricHistory h
            where h.platformKey in :keys and h.recordedAt < :until
              and not exists (
                select newer.historyId from MappingMetricHistory newer
                where newer.platformKey = h.platformKey and newer.recordedAt < :until
                  and (newer.recordedAt > h.recordedAt
                       or (newer.recordedAt = h.recordedAt and newer.historyId > h.historyId))
              )
            """)
    List<MappingMetricHistory> queryLatestBefore(@Param("keys") Collection<PlatformKey> keys,
                                                 @Param("until") Instant atExclusive);
}

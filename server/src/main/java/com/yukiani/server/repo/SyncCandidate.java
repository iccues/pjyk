package com.yukiani.server.repo;

import com.yukiani.server.entity.Platform;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 同步候选 Mapping 的投影，只包含判断是否到期和发起抓取所需的字段。
 *
 * @param animeId    关联动画的 animeId，用于分片
 * @param startDate  关联动画的开播日期
 * @param updateTime Mapping 最近一次成功抓取并保存的时间
 */
public record SyncCandidate(Long mappingId, Long animeId, LocalDate startDate, Platform sourcePlatform,
                            String platformId, Instant updateTime) {
}

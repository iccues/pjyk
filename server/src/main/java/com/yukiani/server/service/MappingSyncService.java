package com.yukiani.server.service;

import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.ReviewStatus;
import com.yukiani.server.repo.MappingRepository;
import com.yukiani.server.repo.SyncCandidate;
import com.yukiani.server.service.fetch.FetchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 按开播时间和 animeId 分片安排 Mapping 同步，每轮按平台串行刷新到期的 Mapping。
 *
 * <p>是否到期只由数据库中的 {@code update_time} 推算，进程重启不会遗漏任务。
 * 内存中仅保存连续失败 Mapping 的退避状态，丢失后这些 Mapping 只是会提前重试。</p>
 */
@Service
@Slf4j
public class MappingSyncService {

    /** 划分同步日、计算开播天数和分片时使用的时区。 */
    static final ZoneId SYNC_ZONE = ZoneId.of("Asia/Shanghai");

    /** 首次失败后的退避时长，之后每次连续失败翻倍。 */
    static final Duration BACKOFF_BASE = Duration.ofHours(1);

    /** 单条 Mapping 退避时长的上限。 */
    static final Duration BACKOFF_MAX = Duration.ofDays(7);

    @Resource
    MappingRepository mappingRepository;

    @Resource
    FetchService fetchService;

    /** 同一平台每次请求前的等待时间，避免触发平台限流。 */
    Duration requestInterval = Duration.ofMillis(2500);

    /** 按 mappingId 记录连续失败的 Mapping 及其退避状态；成功或不再到期时移除。 */
    final Map<Long, Backoff> backoffs = new ConcurrentHashMap<>();

    /**
     * 单条 Mapping 的退避状态。
     *
     * @param delay         本次退避时长，再次失败时翻倍
     * @param nextAttemptAt 最早可再次尝试的时间
     */
    record Backoff(Duration delay, Instant nextAttemptAt) {
    }

    /**
     * 执行一轮同步：筛选到期且不在退避期内的 Mapping，平台之间并行、平台内串行抓取。
     */
    public void runSyncRound() {
        runSyncRound(Instant.now());
    }

    void runSyncRound(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, SYNC_ZONE);
        // 以整分钟作为本轮基准，使退避到期时刻与整点触发的轮次对齐。
        Instant roundStart = now.truncatedTo(ChronoUnit.MINUTES);

        List<SyncCandidate> due = mappingRepository
                .findSyncCandidates(ReviewStatus.APPROVED, today, today.atStartOfDay(SYNC_ZONE).toInstant())
                .stream()
                .filter(candidate -> isDue(candidate, today))
                .toList();

        // 不再到期的 Mapping 已经同步成功或不再需要同步，其失败记录随之作废。
        Set<Long> dueIds = due.stream().map(SyncCandidate::mappingId).collect(Collectors.toSet());
        backoffs.keySet().retainAll(dueIds);

        // 有失败记录的排在最后，避免持续失败的条目每轮都先被请求、触发熔断而挡住其他条目；
        // 同组内按 update_time 从旧到新。
        List<SyncCandidate> toSync = due.stream()
                .filter(candidate -> !inBackoff(candidate.mappingId(), roundStart))
                .sorted(Comparator.comparing((SyncCandidate candidate) -> backoffs.containsKey(candidate.mappingId()))
                        .thenComparing(SyncCandidate::updateTime))
                .toList();
        log.info("Mapping sync round - Due: {}, In backoff: {}", due.size(), due.size() - toSync.size());

        // 平台之间互不影响，各用一个线程；关闭执行器时会等待全部平台处理完毕。
        try (ExecutorService executor = Executors.newCachedThreadPool()) {
            toSync.stream()
                    .collect(Collectors.groupingBy(SyncCandidate::sourcePlatform))
                    .forEach((platform, candidates) ->
                            executor.execute(() -> syncPlatform(platform, candidates, roundStart)));
        }
    }

    /**
     * 判断 Mapping 今天是否需要同步。
     *
     * <p>按开播天数确定同步间隔，再按 animeId 分片找到不晚于今天的最近一个排班日。
     * 排班日零点之后没有成功抓取过即为到期，因此排班日失败的 Mapping 会持续到期直到成功。</p>
     */
    static boolean isDue(SyncCandidate candidate, LocalDate today) {
        long day = today.toEpochDay();
        int interval = syncIntervalDays(day - candidate.startDate().toEpochDay());
        // 排班日满足 day % interval == animeId % interval，这里取不晚于今天的最近一个。
        long scheduledDay = day - Math.floorMod(day - candidate.animeId(), interval);
        Instant scheduledStart = LocalDate.ofEpochDay(scheduledDay).atStartOfDay(SYNC_ZONE).toInstant();
        return candidate.updateTime().isBefore(scheduledStart);
    }

    /** 开播越久同步越稀疏：30 天内每天，90 天内隔天，180 天内每周，之后每 30 天。 */
    private static int syncIntervalDays(long daysSinceStart) {
        if (daysSinceStart < 30) {
            return 1;
        }
        if (daysSinceStart < 90) {
            return 2;
        }
        if (daysSinceStart < 180) {
            return 7;
        }
        return 30;
    }

    private boolean inBackoff(Long mappingId, Instant roundStart) {
        Backoff backoff = backoffs.get(mappingId);
        return backoff != null && roundStart.isBefore(backoff.nextAttemptAt());
    }

    /** 交给平台层串行同步本平台的 Mapping，再按结果更新单条退避。 */
    private void syncPlatform(Platform platform, List<SyncCandidate> candidates, Instant roundStart) {
        PlatformSyncRunner runner = new PlatformSyncRunner(
                platform, fetchService.getFetchService(platform), requestInterval);
        PlatformSyncRunner.Result result = runner.run(candidates);
        result.succeeded().forEach(backoffs::remove);
        result.failed().forEach(id -> recordFailure(id, roundStart));
    }

    /** 记录一次失败：首次退避 1 小时，之后每次翻倍，最长 7 天。 */
    private void recordFailure(Long mappingId, Instant roundStart) {
        backoffs.compute(mappingId, (id, previous) -> {
            Duration delay = previous == null ? BACKOFF_BASE : previous.delay().multipliedBy(2);
            if (delay.compareTo(BACKOFF_MAX) > 0) {
                delay = BACKOFF_MAX;
            }
            return new Backoff(delay, roundStart.plus(delay));
        });
    }
}

package com.yukiani.server.service;

import com.yukiani.server.entity.Platform;
import com.yukiani.server.repo.SyncCandidate;
import com.yukiani.server.service.fetch.AbstractAnimeFetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 在一轮同步中串行处理单个平台的到期 Mapping：控制请求间隔，平台连续故障时熔断。
 *
 * <p>5xx、429、408 与网络错误、超时属于平台级失败。连续达到阈值即熔断，停止本平台本轮剩余的 Mapping；
 * 其余失败说明平台仍有响应，会让连续计数清零。熔断只决定本轮是否继续请求，所有失败都照常交给轮次层计入单条退避，
 * 否则持续失败的条目会因为不进退避而每轮都排在最前面，反复触发熔断。</p>
 *
 * <p>状态只存在于一次 {@link #run} 调用中，不跨轮保存。</p>
 */
@Slf4j
class PlatformSyncRunner {

    /** 连续平台级失败达到该次数时熔断。 */
    static final int CIRCUIT_THRESHOLD = 5;

    private final Platform platform;
    private final AbstractAnimeFetchService service;
    private final Duration requestInterval;

    /**
     * @param requestInterval 每次请求前的等待时间，避免触发平台限流
     */
    PlatformSyncRunner(Platform platform, AbstractAnimeFetchService service, Duration requestInterval) {
        this.platform = platform;
        this.service = service;
        this.requestInterval = requestInterval;
    }

    /**
     * 本平台一轮处理的结果，供轮次层更新单条退避。
     *
     * @param succeeded 同步成功的 mappingId
     * @param failed    同步失败、应计入单条退避的 mappingId
     */
    record Result(List<Long> succeeded, List<Long> failed) {
    }

    /** 按给定顺序串行同步。 */
    Result run(List<SyncCandidate> candidates) {
        long startTime = System.currentTimeMillis();
        List<Long> succeeded = new ArrayList<>();
        List<Long> failed = new ArrayList<>();
        int consecutivePlatformFailures = 0;
        int attempted = 0;

        for (SyncCandidate candidate : candidates) {
            if (consecutivePlatformFailures >= CIRCUIT_THRESHOLD) {
                log.warn("Mapping sync [{}] circuit opened after {} consecutive platform failures",
                        platform, CIRCUIT_THRESHOLD);
                break;
            }
            try {
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                // 应用关闭等情况下停止本轮；未处理的 Mapping 仍然到期，下一轮继续。
                Thread.currentThread().interrupt();
                break;
            }
            attempted++;

            switch (sync(candidate)) {
                case SUCCESS -> {
                    succeeded.add(candidate.mappingId());
                    consecutivePlatformFailures = 0;
                }
                case ITEM_FAILURE -> {
                    failed.add(candidate.mappingId());
                    consecutivePlatformFailures = 0;
                }
                case PLATFORM_FAILURE -> {
                    failed.add(candidate.mappingId());
                    consecutivePlatformFailures++;
                }
            }
        }

        log.info("Mapping sync [{}] completed - Total: {}, Success: {}, Failed: {}, Skipped: {}, Duration: {}ms",
                platform, candidates.size(), succeeded.size(), attempted - succeeded.size(),
                candidates.size() - attempted, System.currentTimeMillis() - startTime);
        return new Result(succeeded, failed);
    }

    /**
     * 判断失败是否归因于平台：5xx、429、408 响应，或网络错误、超时等未收到响应的请求异常。
     *
     * <p>只认明确列出的类型，其余失败一律算条目级，避免 404 等条目问题误触发熔断。</p>
     */
    static boolean isPlatformFailure(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof WebClientRequestException) {
                return true;
            }
            if (t instanceof WebClientResponseException r) {
                HttpStatusCode status = r.getStatusCode();
                return status.is5xxServerError()
                        || status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)
                        || status.isSameCodeAs(HttpStatus.REQUEST_TIMEOUT);
            }
        }
        return false;
    }

    /** 单条 Mapping 的同步结果。 */
    private enum Outcome {
        SUCCESS,
        /** 条目自身的问题，例如平台上已删除、数据不完整。 */
        ITEM_FAILURE,
        /** 平台故障或限流，计入连续失败次数。 */
        PLATFORM_FAILURE
    }

    /** 抓取并保存单条 Mapping；{@code fetchAndSaveMapping} 正常返回即表示已保存。 */
    private Outcome sync(SyncCandidate candidate) {
        try {
            service.fetchAndSaveMapping(candidate.platformId());
            return Outcome.SUCCESS;
        } catch (Exception e) {
            log.warn("Failed to sync mapping {}:{} - {}", platform, candidate.platformId(), e.getMessage());
            return isPlatformFailure(e) ? Outcome.PLATFORM_FAILURE : Outcome.ITEM_FAILURE;
        }
    }
}

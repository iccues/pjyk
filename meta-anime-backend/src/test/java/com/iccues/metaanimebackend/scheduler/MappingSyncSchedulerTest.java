package com.iccues.metaanimebackend.scheduler;

import com.iccues.metaanimebackend.service.MappingSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MappingSyncSchedulerTest {

    @MockitoBean
    private MappingSyncService mappingSyncService;

    @Resource
    private MappingSyncScheduler scheduler;

    // ============ 基础功能测试 ============

    @Test
    public void testScheduleDailyMappingCollection_Success() {
        // 准备：Mock service 方法不抛出异常
        doNothing().when(mappingSyncService).collectMappingsForSync();

        // 执行：调用定时任务方法
        scheduler.scheduleDailyMappingCollection();

        // 验证：service 方法被调用一次
        verify(mappingSyncService, times(1)).collectMappingsForSync();
    }

    @Test
    public void testScheduleMappingSync_Success() {
        // 准备：Mock service 方法不抛出异常
        doNothing().when(mappingSyncService).processPendingMappings();

        // 执行：调用定时任务方法
        scheduler.scheduleMappingSync();

        // 验证：service 方法被调用一次
        verify(mappingSyncService, times(1)).processPendingMappings();
    }

    // ============ 异常处理测试 ============

    @Test
    public void testScheduleDailyMappingCollection_ExceptionHandling() {
        // 准备：Mock service 方法抛出异常
        doThrow(new RuntimeException("Database connection failed"))
                .when(mappingSyncService).collectMappingsForSync();

        // 执行：调用定时任务方法
        // 验证：不应该抛出异常（异常被捕获）
        assertDoesNotThrow(() -> scheduler.scheduleDailyMappingCollection());

        // 验证：service 方法被调用一次
        verify(mappingSyncService, times(1)).collectMappingsForSync();
    }

    @Test
    public void testScheduleMappingSync_ExceptionHandling() {
        // 准备：Mock service 方法抛出异常
        doThrow(new RuntimeException("External API timeout"))
                .when(mappingSyncService).processPendingMappings();

        // 执行：调用定时任务方法
        // 验证：不应该抛出异常（异常被捕获）
        assertDoesNotThrow(() -> scheduler.scheduleMappingSync());

        // 验证：service 方法被调用一次
        verify(mappingSyncService, times(1)).processPendingMappings();
    }

    // ============ Service 交互测试 ============

    @Test
    public void testScheduleDailyMappingCollection_CallsCorrectServiceMethod() {
        // 准备：重置 mock
        reset(mappingSyncService);
        doNothing().when(mappingSyncService).collectMappingsForSync();

        // 执行：调用定时任务
        scheduler.scheduleDailyMappingCollection();

        // 验证：只调用了 collectMappingsForSync，没有调用 processPendingMappings
        verify(mappingSyncService, times(1)).collectMappingsForSync();
        verify(mappingSyncService, never()).processPendingMappings();
    }

    @Test
    public void testScheduleMappingSync_CallsCorrectServiceMethod() {
        // 准备：重置 mock
        reset(mappingSyncService);
        doNothing().when(mappingSyncService).processPendingMappings();

        // 执行：调用定时任务
        scheduler.scheduleMappingSync();

        // 验证：只调用了 processPendingMappings，没有调用 collectMappingsForSync
        verify(mappingSyncService, times(1)).processPendingMappings();
        verify(mappingSyncService, never()).collectMappingsForSync();
    }

    // ============ 并发和重入测试 ============

    @Test
    public void testScheduleMethodsAreThreadSafe() throws InterruptedException {
        // 准备：Mock service 方法
        doNothing().when(mappingSyncService).collectMappingsForSync();
        doNothing().when(mappingSyncService).processPendingMappings();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        // 执行：并发调用两个定时任务方法
        executor.submit(() -> {
            try {
                scheduler.scheduleDailyMappingCollection();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                scheduler.scheduleMappingSync();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                scheduler.scheduleDailyMappingCollection();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                scheduler.scheduleMappingSync();
            } finally {
                latch.countDown();
            }
        });

        // 等待所有任务完成
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证：所有任务都完成了
        assertTrue(completed, "All tasks should complete within timeout");

        // 验证：每个方法都被调用了2次
        verify(mappingSyncService, times(2)).collectMappingsForSync();
        verify(mappingSyncService, times(2)).processPendingMappings();
    }

    @Test
    public void testMultipleInvocations() {
        // 准备：Mock service 方法
        doNothing().when(mappingSyncService).collectMappingsForSync();
        doNothing().when(mappingSyncService).processPendingMappings();

        // 执行：多次调用定时任务方法
        scheduler.scheduleDailyMappingCollection();
        scheduler.scheduleDailyMappingCollection();
        scheduler.scheduleDailyMappingCollection();

        scheduler.scheduleMappingSync();
        scheduler.scheduleMappingSync();

        // 验证：每个方法被正确调用次数
        verify(mappingSyncService, times(3)).collectMappingsForSync();
        verify(mappingSyncService, times(2)).processPendingMappings();
    }

    // ============ init 方法测试（注释掉的 @PostConstruct）============

    @Test
    public void testInit_Success() {
        // 准备：Mock service 方法
        doNothing().when(mappingSyncService).collectMappingsForSync();
        doNothing().when(mappingSyncService).processPendingMappings();

        // 执行：调用 init 方法
        scheduler.init();

        // 验证：两个 service 方法都被调用
        verify(mappingSyncService, times(1)).collectMappingsForSync();
        verify(mappingSyncService, times(1)).processPendingMappings();
    }

    @Test
    public void testInit_ExceptionHandling() {
        // 准备：Mock service 方法抛出异常
        doThrow(new RuntimeException("Initialization failed"))
                .when(mappingSyncService).collectMappingsForSync();

        // 执行和验证：不应该抛出异常
        assertDoesNotThrow(() -> scheduler.init());

        // 验证：即使第一个方法失败，也尝试调用了
        verify(mappingSyncService, times(1)).collectMappingsForSync();
        // processPendingMappings 可能不会被调用，因为异常在 collectMappingsForSync 时就抛出了
    }

    @Test
    public void testInit_PartialFailure() {
        // 准备：第一个成功，第二个失败
        doNothing().when(mappingSyncService).collectMappingsForSync();
        doThrow(new RuntimeException("Processing failed"))
                .when(mappingSyncService).processPendingMappings();

        // 执行和验证：不应该抛出异常
        assertDoesNotThrow(() -> scheduler.init());

        // 验证：两个方法都被调用
        verify(mappingSyncService, times(1)).collectMappingsForSync();
        verify(mappingSyncService, times(1)).processPendingMappings();
    }
}

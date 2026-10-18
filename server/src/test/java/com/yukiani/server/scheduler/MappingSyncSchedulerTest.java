package com.yukiani.server.scheduler;

import com.yukiani.server.service.MappingSyncService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MappingSyncSchedulerTest {

    @MockitoBean
    private MappingSyncService mappingSyncService;

    @Resource
    private MappingSyncScheduler scheduler;

    @Test
    public void testScheduleMappingSync_RunsOneRound() {
        scheduler.scheduleMappingSync();

        verify(mappingSyncService, times(1)).runSyncRound();
    }

    @Test
    public void testScheduleMappingSync_ExceptionHandling() {
        // 准备：一轮同步抛出异常
        doThrow(new RuntimeException("Database connection failed"))
                .when(mappingSyncService).runSyncRound();

        // 验证：异常被捕获，不影响后续调度
        assertDoesNotThrow(() -> scheduler.scheduleMappingSync());
        verify(mappingSyncService, times(1)).runSyncRound();
    }
}

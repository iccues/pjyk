package com.yukiani.server.controller;

import com.yukiani.server.service.MetricRecalculationService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminMaintenanceControllerTest {

    @Resource
    private MockMvc mockMvc;

    @MockitoBean
    private MetricRecalculationService metricRecalculationService;

    @Test
    void startsMetricRecalculation() throws Exception {
        mockMvc.perform(post("/api/admin/tasks/metric-recalculation"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("指标重算任务已启动"));

        verify(metricRecalculationService).recalculateAllMetrics();
    }
}

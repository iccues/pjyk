package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.config.OidcConfig;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PublicConfigControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private OidcConfig oidcConfig;

    @Test
    public void testGetOidcConfig_ReturnsCorrectConfiguration() throws Exception {
        mockMvc.perform(get("/api/config/oidc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issuer").value(oidcConfig.issuer()))
                .andExpect(jsonPath("$.data.clientId").value(oidcConfig.clientId()));
    }

    @Test
    public void testGetOidcConfig_ReturnsExpectedTestValues() throws Exception {
        // 验证测试环境中的配置值
        mockMvc.perform(get("/api/config/oidc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issuer").value("https://test.example.com"))
                .andExpect(jsonPath("$.data.clientId").value("test-client-id"));
    }

    @Test
    public void testGetOidcConfig_IsPubliclyAccessible() throws Exception {
        // 验证此端点不需要认证即可访问
        mockMvc.perform(get("/api/config/oidc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetOidcConfig_ReturnsJsonContentType() throws Exception {
        mockMvc.perform(get("/api/config/oidc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}

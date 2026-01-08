package com.iccues.metaanimebackend.config;

import lombok.Data;

/**
 * 单个平台的配置参数
 */
@Data
public class PlatformConfig {
    /**
     * 热度归一化乘数
     */
    private double popularityMultiplier = 1.0;

    /**
     * 评分权重
     */
    private int scoreWeight = 1;
}

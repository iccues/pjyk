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

    /**
     * 评分归一化 - 均值（用于 z-score 标准化）
     */
    private double scoreMean = 7.0;

    /**
     * 评分归一化 - 标准差（用于 z-score 标准化）
     */
    private double scoreStd = 1.0;
}

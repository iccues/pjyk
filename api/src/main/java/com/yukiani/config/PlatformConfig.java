package com.yukiani.config;

import lombok.Data;

/**
 * 单个平台的配置参数
 */
@Data
public class PlatformConfig {
    /**
     * 热度权重
     */
    private double popularityWeight = 1;

    /**
     * 热度归一化 - 中位数
     */
    private double popularityMedian = 10000;

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

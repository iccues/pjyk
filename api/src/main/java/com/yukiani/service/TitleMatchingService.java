package com.yukiani.service;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

@Service
public class TitleMatchingService {

    // 1. 将算法实例设为成员变量，以便复用
    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    // 2. 定义一个合理的相似度阈值
    // 这个值需要您通过实验来调整
    private static final double SIMILARITY_THRESHOLD = 0.95;

    /**
     * 检查两个动画标题是否可能指向同一个作品
     * @param title1 标题1
     * @param title2 标题2
     * @return 如果相似度超过阈值，则返回 true
     */
    public boolean areTitlesSimilar(String title1, String title2) {
        if (title1 == null || title2 == null) {
            return false;
        }

        // 3. 标准化是至关重要的一步
        String normalized1 = normalize(title1);
        String normalized2 = normalize(title2);

        // 如果标准化后完全一致，直接返回 true
        if (normalized1.equals(normalized2)) {
            return true;
        }

        // 4. 计算相似度分数
        double score = similarity.apply(normalized1, normalized2);

        // 5. 根据阈值返回结果
        return score >= SIMILARITY_THRESHOLD;
    }

    /**
     * 辅助方法：标准化标题
     * 1. 转为小写
     * 2. 移除所有非字母和数字的字符
     */
    private String normalize(String input) {
        // 使用正则表达式移除所有标点、空格等
        return input;
//        return input.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}

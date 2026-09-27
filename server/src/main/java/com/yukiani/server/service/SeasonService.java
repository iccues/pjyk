package com.yukiani.server.service;

import com.yukiani.server.entity.LocalDateRange;
import com.yukiani.server.entity.Season;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 将年份和季度转换为数据库查询使用的开播日期范围。
 */
@Service
public class SeasonService {
    /**
     * 计算查询的左闭右开日期范围，将自然季度或自然年整体前移一个月以容纳跨月排期。
     *
     * @param year   开播年份；为空时返回 {@code null}
     * @param season 开播季度；为空时覆盖全年
     * @return 查询日期范围；年份为空时返回 {@code null}
     */
    public LocalDateRange getStartDateRange(Integer year, Season season) {
        if (year == null) {
            return null;
        }

        if (season == null) {
            LocalDate start = LocalDate.of(year, 1, 1).minusMonths(1);
            LocalDate end = start.plusYears(1);
            return new LocalDateRange(start, end);
        }

        LocalDate start = LocalDate.of(year, season.toMonth(), 1).minusMonths(1);
        LocalDate end = start.plusMonths(3);
        return new LocalDateRange(start, end);
    }
}

package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.LocalDateRange;
import com.iccues.metaanimebackend.entity.Season;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SeasonServiceTest {

    @Resource
    SeasonService seasonService;

    @Test
    public void testGetStartDateRange_WithNullYear() {
        // 测试年份为 null 的情况
        assertNull(seasonService.getStartDateRange(null, null));
        assertNull(seasonService.getStartDateRange(null, Season.FALL));
        assertNull(seasonService.getStartDateRange(null, Season.WINTER));
    }

    @Test
    public void testGetStartDateRange_WithYearOnly() {
        // 测试只有年份，没有季节的情况（返回整年的范围）
        LocalDateRange result = seasonService.getStartDateRange(2025, null);

        assertEquals(LocalDate.parse("2024-12-01"), result.start());
        assertEquals(LocalDate.parse("2025-12-01"), result.end());
    }

    @Test
    public void testGetStartDateRange_WithWinterSeason() {
        // 测试冬季（1月）
        LocalDateRange result = seasonService.getStartDateRange(2025, Season.WINTER);

        // 冬季从前一年12月到3月
        assertEquals(LocalDate.parse("2024-12-01"), result.start());
        assertEquals(LocalDate.parse("2025-03-01"), result.end());
    }

    @Test
    public void testGetStartDateRange_WithSpringSeason() {
        // 测试春季（4月）
        LocalDateRange result = seasonService.getStartDateRange(2025, Season.SPRING);

        // 春季从3月到6月
        assertEquals(LocalDate.parse("2025-03-01"), result.start());
        assertEquals(LocalDate.parse("2025-06-01"), result.end());
    }

    @Test
    public void testGetStartDateRange_WithSummerSeason() {
        // 测试夏季（7月）
        LocalDateRange result = seasonService.getStartDateRange(2025, Season.SUMMER);

        // 夏季从6月到9月
        assertEquals(LocalDate.parse("2025-06-01"), result.start());
        assertEquals(LocalDate.parse("2025-09-01"), result.end());
    }

    @Test
    public void testGetStartDateRange_WithFallSeason() {
        // 测试秋季（10月）
        LocalDateRange result = seasonService.getStartDateRange(2025, Season.FALL);

        // 秋季从9月到12月
        assertEquals(LocalDate.parse("2025-09-01"), result.start());
        assertEquals(LocalDate.parse("2025-12-01"), result.end());
    }

    @Test
    public void testGetStartDateRange_WithDifferentYears() {
        // 测试不同年份
        LocalDateRange result2020 = seasonService.getStartDateRange(2020, Season.SPRING);
        assertEquals(LocalDate.parse("2020-03-01"), result2020.start());
        assertEquals(LocalDate.parse("2020-06-01"), result2020.end());

        LocalDateRange result2030 = seasonService.getStartDateRange(2030, Season.FALL);
        assertEquals(LocalDate.parse("2030-09-01"), result2030.start());
        assertEquals(LocalDate.parse("2030-12-01"), result2030.end());
    }

    @Test
    public void testGetStartDateRange_AllSeasons() {
        // 测试所有季节都能正确计算
        int year = 2024;

        assertNotNull(seasonService.getStartDateRange(year, Season.WINTER));
        assertNotNull(seasonService.getStartDateRange(year, Season.SPRING));
        assertNotNull(seasonService.getStartDateRange(year, Season.SUMMER));
        assertNotNull(seasonService.getStartDateRange(year, Season.FALL));
    }

    @Test
    public void testGetStartDateRange_DateRangeDuration() {
        // 验证季节范围始终是3个月
        LocalDateRange winter = seasonService.getStartDateRange(2025, Season.WINTER);
        LocalDateRange spring = seasonService.getStartDateRange(2025, Season.SPRING);
        LocalDateRange summer = seasonService.getStartDateRange(2025, Season.SUMMER);
        LocalDateRange fall = seasonService.getStartDateRange(2025, Season.FALL);

        // 每个季节的范围都应该是3个月
        assertEquals(3, java.time.temporal.ChronoUnit.MONTHS.between(winter.start(), winter.end()));
        assertEquals(3, java.time.temporal.ChronoUnit.MONTHS.between(spring.start(), spring.end()));
        assertEquals(3, java.time.temporal.ChronoUnit.MONTHS.between(summer.start(), summer.end()));
        assertEquals(3, java.time.temporal.ChronoUnit.MONTHS.between(fall.start(), fall.end()));
    }

    @Test
    public void testGetStartDateRange_YearRangeDuration() {
        // 验证整年范围是12个月
        LocalDateRange yearRange = seasonService.getStartDateRange(2025, null);

        assertEquals(12, java.time.temporal.ChronoUnit.MONTHS.between(yearRange.start(), yearRange.end()));
    }
}

package com.yukiani.server.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 按站点时区换算指标历史的日期与时刻。
 */
public final class MetricDateUtil {

    /** 站点时区，确保日期边界不受服务器默认时区影响。 */
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private MetricDateUtil() {
    }

    /** 返回指定日期的零点。 */
    public static Instant startOf(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant();
    }

    /** 返回次日零点，作为查询区间的排他上界。 */
    public static Instant endOf(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZONE).toInstant();
    }

    /** 返回观测时刻在站点时区所属的日期。 */
    public static LocalDate dateOf(Instant instant) {
        return LocalDate.ofInstant(instant, ZONE);
    }
}

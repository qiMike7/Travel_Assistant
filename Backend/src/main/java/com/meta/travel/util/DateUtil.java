package com.meta.travel.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间格式化辅助
 */
public final class DateUtil {

    public static final DateTimeFormatter DEFAULT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {
    }

    public static String format(LocalDateTime time) {
        return time == null ? null : time.format(DEFAULT);
    }
}

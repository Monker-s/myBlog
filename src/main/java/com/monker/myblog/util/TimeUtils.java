package com.monker.myblog.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * 文件用途：提供时间相关工具方法。
 * 作用说明：统一封装项目中需要复用的时间处理逻辑，避免散落在业务代码里重复实现。
 */
public final class TimeUtils {

    /**
     * 函数用途：禁止工具类被实例化。
     */
    private TimeUtils() {
    }

    /**
     * 函数用途：返回当前 UTC 时间。
     *
     * @return 当前 UTC 时区的时间对象
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}

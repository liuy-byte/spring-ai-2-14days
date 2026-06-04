package com.jishuyang.springai.day12;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

/**
 * Day12：无参工具 —— 获取当前时间。
 */
public class DateTimeTools {

    @Tool(description = "获取用户当前时区的日期和时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now()
                .atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .toString();
    }
}

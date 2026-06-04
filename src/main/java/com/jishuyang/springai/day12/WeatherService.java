package com.jishuyang.springai.day12;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Day12：带参工具 —— 查询天气（示例返回固定值，实际可调真实天气 API）。
 */
public class WeatherService {

    @Tool(description = "查询指定城市的当前天气")
    public String getWeather(@ToolParam(description = "城市名称，如：北京") String city) {
        // 这里可替换为真实天气 API 调用
        return city + "今天晴，25℃";
    }
}

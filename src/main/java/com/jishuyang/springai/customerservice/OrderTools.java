package com.jishuyang.springai.customerservice;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 客服业务工具：查订单、查物流、建工单。
 * 这里返回模拟数据，真实项目替换为数据库 / 微服务调用。
 */
public class OrderTools {

    @Tool(description = "根据订单号查询订单状态")
    public String queryOrder(@ToolParam(description = "订单号") String orderId) {
        return "订单 " + orderId + " 状态：已发货";
    }

    @Tool(description = "根据订单号查询物流轨迹")
    public String queryLogistics(@ToolParam(description = "订单号") String orderId) {
        return "顺丰 SF" + orderId + "，当前在【杭州转运中心】";
    }

    @Tool(description = "为用户创建售后工单，返回工单号")
    public String createTicket(@ToolParam(description = "订单号") String orderId,
                               @ToolParam(description = "问题描述") String issue) {
        return "工单已创建，编号 TK20260618（订单 " + orderId + "：" + issue + "）";
    }
}

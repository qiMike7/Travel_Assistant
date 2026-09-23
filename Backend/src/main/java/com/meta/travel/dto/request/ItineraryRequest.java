package com.meta.travel.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 快速智慧旅行攻略规划请求（对应 pages/shopping/shopping.vue）
 * <p>用户填写目的地与行程偏好，后端代理大模型生成结构化的分日攻略。</p>
 */
@Data
public class ItineraryRequest {

    /** 目的地（城市 / 景区） */
    @NotBlank(message = "目的地不能为空")
    private String destination;

    /** 行程天数，默认 3 天 */
    @Min(value = 1, message = "行程至少 1 天")
    @Max(value = 15, message = "行程最多 15 天")
    private Integer days = 3;

    /** 出行风格 / 兴趣标签，例如：亲子、美食、人文历史、自然风光 */
    private String style;

    /** 每日预算（元），为空时取用户旅行偏好 */
    private Integer budget;

    /** 出行人数，默认 2 人 */
    @Min(value = 1, message = "出行人数至少 1 人")
    private Integer travelers = 2;

    /** 补充说明 / 特殊要求 */
    private String note;
}

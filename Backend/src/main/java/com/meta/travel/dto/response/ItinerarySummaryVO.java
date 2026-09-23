package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 攻略历史列表项（对应 plan-history.vue）
 * <p>只含摘要字段，完整行程通过 {@code GET /api/itinerary/{id}} 获取。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarySummaryVO {

    private Long id;
    private String destination;
    private Integer days;
    private String summary;

    /** 创建时间，格式 yyyy-MM-dd HH:mm:ss */
    private String createTime;
}

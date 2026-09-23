package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 已保存的旅行攻略（对应 pages/plan-history、pages/shopping）
 * <p>content 存完整的 {@link com.meta.travel.dto.response.ItineraryVO} JSON，
 * 冗余 destination/days/summary 便于列表展示与排序。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_travel_plan", indexes = {
        @Index(name = "idx_travel_plan_user", columnList = "user_id, id")
})
public class TravelPlan extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 目的地 */
    @Column(nullable = false, length = 128)
    private String destination;

    /** 行程天数 */
    @Column(nullable = false)
    private Integer days;

    /** 概览（列表摘要展示） */
    @Column(length = 512)
    private String summary;

    /** 人均预估总花费（元） */
    @Column(name = "total_budget")
    private Integer totalBudget;

    /** 完整攻略 JSON */
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}

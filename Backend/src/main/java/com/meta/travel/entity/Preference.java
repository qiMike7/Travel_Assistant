package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户旅行偏好（对应 pages/preference/preference.vue）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_preference")
public class Preference extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** 旅行方式：自由行 / 跟团游 / 自驾游 / 徒步探险 */
    @Column(name = "travel_mode", length = 32)
    private String travelMode = "自由行";

    /** 每日预算（元） */
    private Integer budget = 500;

    /** 住宿偏好：经济型酒店 / 精品民宿 / 高端酒店 / 露营帐篷 */
    @Column(length = 32)
    private String stay = "经济型酒店";

    /** AI 推荐强度 1-5 */
    @Column(name = "ai_level")
    private Integer aiLevel = 3;

    /** 是否接收旅行提醒与推荐 */
    @Column(nullable = false)
    private Boolean notifications = true;

    /** 深色模式开关（settings.vue） */
    @Column(name = "dark_mode", nullable = false)
    private Boolean darkMode = false;
}

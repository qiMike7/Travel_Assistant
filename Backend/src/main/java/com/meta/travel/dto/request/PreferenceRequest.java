package com.meta.travel.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 偏好设置保存请求（对应 preference.vue）
 */
@Data
public class PreferenceRequest {

    private String travelMode;

    @Min(value = 0, message = "预算不能为负")
    private Integer budget;

    private String stay;

    @Min(value = 1, message = "AI 推荐强度范围 1-5")
    @Max(value = 5, message = "AI 推荐强度范围 1-5")
    private Integer aiLevel;

    private Boolean notifications;

    private Boolean darkMode;
}

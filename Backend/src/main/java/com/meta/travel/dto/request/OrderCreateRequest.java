package com.meta.travel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求（对应 guide.vue 立即购买）
 */
@Data
public class OrderCreateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为 1")
    private Integer quantity = 1;
}

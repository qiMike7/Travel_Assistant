package com.meta.travel.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payment request from order-confirm.vue.
 * Sent fields: orderId (required), orderNo, amount.
 * Amount is display-only; the signed amount always uses the server-side order total.
 */
@Data
public class PaymentRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** order number, passed by client for tracing; server trusts its own record */
    private String orderNo;

    /** client-side total for logging; not used for signing */
    private BigDecimal amount;
}

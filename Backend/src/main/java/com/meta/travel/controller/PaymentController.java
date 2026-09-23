package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.PaymentRequest;
import com.meta.travel.dto.response.WxPayParamsVO;
import com.meta.travel.entity.Order;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment endpoint (order-confirm.vue -> invoke WeChat Pay).
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/wxpay")
    public Result<WxPayParamsVO> wxpay(@Valid @RequestBody PaymentRequest request) {
        WxPayParamsVO params = paymentService.createWxPayParams(UserContext.currentUserId(), request);
        return Result.success("下单成功", params);
    }

    /** 支付确认：前端在 wx.requestPayment 成功（或完成付款码面板）后调用，订单推进为已付款 */
    @PostMapping("/confirm")
    public Result<Order> confirm(@Valid @RequestBody PaymentRequest request) {
        Order order = paymentService.confirmPaid(UserContext.currentUserId(), request);
        return Result.success("支付确认成功", order);
    }
}

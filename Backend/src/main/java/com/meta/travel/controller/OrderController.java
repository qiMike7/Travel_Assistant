package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.OrderCreateRequest;
import com.meta.travel.entity.Order;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口（对应 order.vue）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<List<Order>> list() {
        return Result.success(orderService.listByUser(UserContext.currentUserId()));
    }

    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success("下单成功", orderService.create(UserContext.currentUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.success(orderService.getById(UserContext.currentUserId(), id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(UserContext.currentUserId(), id);
        return Result.success("订单已取消", null);
    }
}

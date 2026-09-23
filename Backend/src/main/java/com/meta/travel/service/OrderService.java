package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.request.OrderCreateRequest;
import com.meta.travel.entity.Order;
import com.meta.travel.entity.Product;
import com.meta.travel.repository.OrderRepository;
import com.meta.travel.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务（对应 order.vue / guide.vue 立即购买）
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order create(Long userId, OrderCreateRequest request) {
        // 行锁读取，防止并发下库存 read-modify-write 导致超卖
        Product product = productRepository.findForUpdate(request.getProductId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "商品不存在"));
        if (product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品已下架");
        }
        int qty = request.getQuantity();
        if (product.getStock() != null && product.getStock() < qty) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品库存不足");
        }

        BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(qty));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductImage(product.getImage());
        order.setUnitPrice(unitPrice);
        order.setQuantity(qty);
        order.setTotalAmount(total);
        // 下单仅创建待支付订单，支付确认后才推进为“已付款”（见 markPaid）
        order.setStatus("待付款");

        product.setStock(product.getStock() == null ? null : product.getStock() - qty);
        product.setSales((product.getSales() == null ? 0 : product.getSales()) + qty);
        productRepository.save(product);

        return orderRepository.save(order);
    }

    /**
     * 支付成功确认：把待付款订单推进为已付款。
     * <p>幂等：重复确认直接返回；已取消订单不可恢复。</p>
     * <p>演示阶段由前端在 wx.requestPayment 成功后调用 /api/payments/confirm 触发；
     * 生产环境应以微信支付异步通知（验签后）为准。</p>
     */
    @Transactional
    public Order markPaid(Long userId, Long orderId) {
        Order order = getById(userId, orderId);
        if ("已付款".equals(order.getStatus())) {
            return order;
        }
        if ("已取消".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单已取消，无法支付");
        }
        order.setStatus("已付款");
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> listByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Transactional(readOnly = true)
    public Order getById(Long userId, Long id) {
        return orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "订单不存在"));
    }

    @Transactional
    public Order cancel(Long userId, Long id) {
        Order order = getById(userId, id);
        if ("已取消".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单已取消，请勿重复操作");
        }
        // 取消订单时回退库存与销量
        productRepository.findForUpdate(order.getProductId()).ifPresent(product -> {
            if (product.getStock() != null) {
                product.setStock(product.getStock() + order.getQuantity());
            }
            if (product.getSales() != null) {
                product.setSales(Math.max(0, product.getSales() - order.getQuantity()));
            }
            productRepository.save(product);
        });
        order.setStatus("已取消");
        return orderRepository.save(order);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FMT) + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}

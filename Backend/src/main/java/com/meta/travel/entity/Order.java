package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单（对应 pages/order/order.vue）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_order", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id, create_time"),
        @Index(name = "idx_order_product", columnList = "product_id")
})
public class Order extends BaseEntity {

    /** 订单号 */
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    /** 下单时商品名称快照 */
    @Column(name = "product_name", length = 128)
    private String productName;

    @Column(name = "product_image", length = 255)
    private String productImage;

    /** 单价 */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity = 1;

    /** 总价 */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** 状态：待付款 / 已付款 / 已发货 / 已完成 / 已取消 */
    @Column(nullable = false, length = 16)
    private String status = "已付款";
}

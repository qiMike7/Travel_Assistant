package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商城商品（对应 pages/guide/guide.vue 精品商城）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_product")
public class Product extends BaseEntity {

    /** 商品名称 */
    @Column(nullable = false, length = 128)
    private String name;

    /** 描述 */
    @Column(length = 255)
    private String description;

    /** 现价 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 原价（划线价） */
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    /** 商品主图 */
    @Column(length = 255)
    private String image;

    /** 分类 */
    @Column(length = 64)
    private String category;

    /** 库存 */
    @Column(nullable = false)
    private Integer stock = 100;

    /** 销量 */
    @Column(nullable = false)
    private Integer sales = 0;

    /** 上架状态：1 上架，0 下架 */
    @Column(nullable = false)
    private Integer status = 1;
}

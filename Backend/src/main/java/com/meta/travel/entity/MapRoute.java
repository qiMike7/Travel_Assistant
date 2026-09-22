package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 旅行路线（对应 pages/map-test、pages/shopping 的 polyline 地图轨迹）
 * <p>points 以 JSON 数组存储：[{ "latitude": 39.9, "longitude": 116.3 }, ...]</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_map_route")
public class MapRoute extends BaseEntity {

    /** 路线名称 */
    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 255)
    private String description;

    /** 轨迹点 JSON */
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String points;

    /** 线宽 */
    @Column(nullable = false)
    private Integer width = 10;

    /** 线条颜色 */
    @Column(length = 16)
    private String color = "#D9EEFB";

    /** 是否箭头线 */
    @Column(name = "arrow_line", nullable = false)
    private Boolean arrowLine = true;
}

package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 地图路线 VO，输出结构直接对应 uni-app <map> 组件的 polyline 数据格式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapRouteVO {

    private Long id;
    private String name;
    private String description;

    /** 轨迹点 */
    private List<Point> points;

    private Integer width;
    private String color;
    private Boolean arrowLine;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private Double latitude;
        private Double longitude;
    }
}

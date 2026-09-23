package com.meta.travel.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 旅行攻略规划结果 VO（对应 pages/shopping/shopping.vue）
 * <p>既包含分日行程文本，也包含可直接喂给 uni-app &lt;map&gt; 组件的 markers / polyline 坐标，
 * 让"快速智慧旅行攻略规划"不再是只有一张空地图。</p>
 */
@Data
public class ItineraryVO {

    /** 攻略 ID（保存到历史后返回；未保存时为空） */
    private Long id;

    /** 目的地 */
    private String destination;

    /** 行程天数 */
    private Integer days;

    /** 整体行程概览 / 出行贴士 */
    private String summary;

    /** 预估人均总花费（元） */
    private Integer totalBudget;

    /** 分日安排 */
    private List<PlanDay> plan = new ArrayList<>();

    @Data
    public static class PlanDay {

        /** 第几天，从 1 开始 */
        private Integer day;

        /** 当日主题，例如："西湖漫游 · 灵隐祈福" */
        private String title;

        /** 当日行程节点 */
        private List<PlanSpot> spots = new ArrayList<>();
    }

    @Data
    public static class PlanSpot {

        /** 时间段，例如："09:00-11:30" */
        private String time;

        /** 景点 / 场所名称 */
        private String name;

        /** 类别：景点 / 美食 / 住宿 / 交通 / 购物 */
        private String category;

        /** 玩法介绍 */
        private String description;

        /** 实用贴士 */
        private String tip;

        /** 纬度（大模型给出的近似坐标，可能为空） */
        private Double latitude;

        /** 经度（大模型给出的近似坐标，可能为空） */
        private Double longitude;
    }
}

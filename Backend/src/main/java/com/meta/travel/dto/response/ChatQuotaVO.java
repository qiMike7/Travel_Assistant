package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日提问额度（对应 chat.vue 额度提示条）
 * <p>会员不限次：{@code vip=true}，前端据此隐藏提示；非会员展示 {@code limit - used} 剩余次数。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatQuotaVO {

    /** 是否为有效会员（会员不限次） */
    private boolean vip;

    /** 今日已提问次数 */
    private int used;

    /** 每日免费额度（非会员） */
    private int limit;
}

package com.meta.travel.dto.response;

import lombok.Data;

/**
 * 会话摘要 VO（列表用）
 */
@Data
public class ChatSessionVO {

    private Long id;
    private String title;
    private String model;
    private String updateTime;
}

package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息 VO（用于加载历史）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {

    private Long id;
    private String role;
    private String content;
}

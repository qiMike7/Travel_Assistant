package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话回复
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long sessionId;

    /** 助手回复内容 */
    private String content;
}

package com.meta.travel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求（对应 chat.vue / huiyuankefu.vue）
 * <p>
 * 前端仅需传入 sessionId（可为空，为空时后端新建会话）与本次用户提问 content，
 * 历史消息由后端从数据库读取并拼装，避免暴露大模型 API Key。
 * </p>
 */
@Data
public class ChatRequest {

    /** 会话 ID，可为空 */
    private Long sessionId;

    /** 用户本次提问内容 */
    @NotBlank(message = "提问内容不能为空")
    private String content;
}

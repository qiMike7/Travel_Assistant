package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.ChatRequest;
import com.meta.travel.dto.response.ChatMessageVO;
import com.meta.travel.dto.response.ChatQuotaVO;
import com.meta.travel.dto.response.ChatResponse;
import com.meta.travel.dto.response.ChatSessionVO;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 对话接口
 * <ul>
 *   <li>/api/chat        —— 智慧问答（chat.vue，默认 DeepSeek）</li>
 *   <li>/api/kefu/chat   —— 会员客服（huiyuankefu.vue，默认 DeepSeek，可单独配置其它厂商）</li>
 * </ul>
 * 大模型 API Key 保存在服务端，前端不再直连模型。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(UserContext.currentUserId(), request, "assistant");
        return Result.success(response);
    }

    @PostMapping("/kefu/chat")
    public Result<ChatResponse> kefu(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(UserContext.currentUserId(), request, "kefu");
        return Result.success(response);
    }

    /** 今日提问额度（非会员每日限 5 次）：{ vip, used, limit } */
    @GetMapping("/chat/quota")
    public Result<ChatQuotaVO> quota() {
        return Result.success(chatService.quota(UserContext.currentUserId()));
    }

    @GetMapping("/chat/sessions")
    public Result<List<ChatSessionVO>> sessions() {
        return Result.success(chatService.listSessions(UserContext.currentUserId()));
    }

    @GetMapping("/chat/sessions/{id}/messages")
    public Result<List<ChatMessageVO>> messages(@PathVariable Long id) {
        return Result.success(chatService.listMessages(UserContext.currentUserId(), id));
    }

    @DeleteMapping("/chat/sessions/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        chatService.deleteSession(UserContext.currentUserId(), id);
        return Result.success("会话已删除", null);
    }
}

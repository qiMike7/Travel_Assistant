package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.request.ChatRequest;
import com.meta.travel.dto.response.ChatMessageVO;
import com.meta.travel.dto.response.ChatQuotaVO;
import com.meta.travel.dto.response.ChatResponse;
import com.meta.travel.dto.response.ChatSessionVO;
import com.meta.travel.entity.ChatMessage;
import com.meta.travel.entity.ChatSession;
import com.meta.travel.config.LlmProperties;
import com.meta.travel.repository.ChatMessageRepository;
import com.meta.travel.repository.ChatSessionRepository;
import com.meta.travel.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对话服务（对应 chat.vue 智慧问答 / huiyuankefu.vue 会员客服）
 * <p>负责会话与历史消息持久化，并通过 {@link LlmClient} 代理调用大模型。</p>
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT =
            "你是「智趣AI旅行助手」小趣，可为用户提供行程规划、景点推荐、美食介绍、旅行攻略等服务。请用简洁友好的中文回答。";

    /** 非会员每日免费提问次数 */
    private static final int FREE_DAILY_LIMIT = 5;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final LlmClient llmClient;
    private final LlmProperties llmProperties;
    private final UserService userService;

    /**
     * 一次对话。
     * <p>故意不加 {@code @Transactional}：大模型调用可耗时数十秒，
     * 若包在事务内会长时间占用连接池连接；写操作交由 Repository 各自的短事务。</p>
     */
    public ChatResponse chat(Long userId, ChatRequest request, String type) {
        boolean vip = userService.isVipActive(userService.getById(userId));
        if ("kefu".equalsIgnoreCase(type)) {
            // 会员客服为 VIP 专属：非会员直接抛 4292，由前端引导开通并退出
            if (!vip) {
                throw new BusinessException(ResultCode.MEMBER_ONLY);
            }
        } else if (!vip && todayQuestionCount(userId) >= FREE_DAILY_LIMIT) {
            // 非会员的智慧问答校验今日免费额度，超出抛 4291 由前端引导开通
            throw new BusinessException(ResultCode.CHAT_QUOTA_EXCEEDED);
        }

        ChatSession session = resolveSession(userId, request.getSessionId(), type);

        // 保存用户提问
        saveMessage(session.getId(), "user", request.getContent());
        if (!StringUtils.hasText(session.getTitle())) {
            session.setTitle(truncate(request.getContent(), 30));
            sessionRepository.save(session);
        }

        // 组装发给模型的消息：系统提示 + 最近 N 条历史（避免上下文无限增长）
        List<Map<String, String>> payload = new ArrayList<>();
        payload.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        for (ChatMessage m : recentHistory(session.getId())) {
            payload.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }

        String reply = llmClient.chat(type, payload);
        saveMessage(session.getId(), "assistant", reply);

        return new ChatResponse(session.getId(), reply);
    }

    /**
     * 取会话最近的 N 条对话（不含 system），按时间正序返回。
     */
    private List<ChatMessage> recentHistory(Long sessionId) {
        int limit = Math.max(llmProperties.getHistoryLimit(), 2);
        List<ChatMessage> recent = new ArrayList<>(messageRepository
                .findBySessionIdAndRoleNotOrderByIdDesc(sessionId, "system", PageRequest.of(0, limit)));
        Collections.reverse(recent);
        return recent;
    }

    /**
     * 今日提问额度：会员不限次（vip=true）；非会员返回已用次数与每日上限。
     */
    @Transactional(readOnly = true)
    public ChatQuotaVO quota(Long userId) {
        boolean vip = userService.isVipActive(userService.getById(userId));
        int used = (int) todayQuestionCount(userId);
        return new ChatQuotaVO(vip, used, FREE_DAILY_LIMIT);
    }

    /** 今日（自然日）已提问次数。 */
    private long todayQuestionCount(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return messageRepository.countUserQuestionsSince(userId, startOfDay);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionVO> listSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdateTimeDesc(userId).stream().map(s -> {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setModel(s.getModel());
            vo.setUpdateTime(DateUtil.format(s.getUpdateTime()));
            return vo;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageVO> listMessages(Long userId, Long sessionId) {
        requireOwned(userId, sessionId);
        return messageRepository.findBySessionIdOrderByIdAsc(sessionId).stream()
                .filter(m -> !"system".equals(m.getRole()))
                .map(m -> new ChatMessageVO(m.getId(), m.getRole(), m.getContent()))
                .toList();
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = requireOwned(userId, sessionId);
        messageRepository.deleteBySessionId(session.getId());
        sessionRepository.delete(session);
    }

    private ChatSession resolveSession(Long userId, Long sessionId, String type) {
        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setModel(llmClient.modelFor(type));
            return sessionRepository.save(session);
        }
        return requireOwned(userId, sessionId);
    }

    private ChatSession requireOwned(Long userId, Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "会话不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return session;
    }

    private void saveMessage(Long sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        messageRepository.save(message);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}

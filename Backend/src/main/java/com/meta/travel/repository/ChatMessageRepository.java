package com.meta.travel.repository;

import com.meta.travel.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByIdAsc(Long sessionId);

    /** 取会话最近的 N 条消息（倒序分页，调用方需自行反转为时间正序） */
    List<ChatMessage> findBySessionIdAndRoleNotOrderByIdDesc(Long sessionId, String role, Pageable pageable);

    void deleteBySessionId(Long sessionId);

    /**
     * 统计某用户自指定时间以来的提问（user 角色）次数，用于每日免费额度校验。
     * <p>消息只挂 sessionId，需关联会话按 userId 汇总。</p>
     */
    @Query("select count(m) from ChatMessage m, ChatSession s "
            + "where m.sessionId = s.id and s.userId = :userId "
            + "and m.role = 'user' and m.createTime >= :start")
    long countUserQuestionsSince(@Param("userId") Long userId, @Param("start") LocalDateTime start);
}

package com.meta.travel.repository;

import com.meta.travel.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByIdAsc(Long sessionId);

    /** 取会话最近的 N 条消息（倒序分页，调用方需自行反转为时间正序） */
    List<ChatMessage> findBySessionIdAndRoleNotOrderByIdDesc(Long sessionId, String role, Pageable pageable);

    void deleteBySessionId(Long sessionId);
}

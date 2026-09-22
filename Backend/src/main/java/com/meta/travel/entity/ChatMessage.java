package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_chat_message", indexes = {
        @Index(name = "idx_chat_message_session", columnList = "session_id, id")
})
public class ChatMessage extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /** 角色：system / user / assistant */
    @Column(nullable = false, length = 16)
    private String role;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}

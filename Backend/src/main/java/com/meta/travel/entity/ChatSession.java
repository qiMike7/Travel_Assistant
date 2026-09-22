package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话会话（对应 pages/chat/chat.vue）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_chat_session", indexes = {
        @Index(name = "idx_chat_session_user", columnList = "user_id, update_time")
})
public class ChatSession extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 会话标题，取首条用户消息 */
    @Column(length = 128)
    private String title;

    /** 模型标识 */
    @Column(length = 64)
    private String model;
}

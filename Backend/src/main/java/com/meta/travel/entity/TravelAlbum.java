package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 旅行相册（对应 pages/lvxingxiangce/lvxingxiangce.vue）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_album", indexes = {
        @Index(name = "idx_album_user", columnList = "user_id, create_time")
})
public class TravelAlbum extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 相册标题 */
    @Column(length = 128)
    private String title;

    /** 封面（一般取第一张照片） */
    @Column(name = "cover_url", length = 255)
    private String coverUrl;
}

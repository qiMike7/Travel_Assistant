package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 相册照片
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_album_photo", indexes = {
        @Index(name = "idx_album_photo_album", columnList = "album_id, sort_no")
})
public class AlbumPhoto extends BaseEntity {

    @Column(name = "album_id", nullable = false)
    private Long albumId;

    /** 照片 URL */
    @Column(nullable = false, length = 255)
    private String url;

    /** 照片文字说明 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String caption;

    /** 排序 */
    @Column(name = "sort_no", nullable = false)
    private Integer sortNo = 0;
}

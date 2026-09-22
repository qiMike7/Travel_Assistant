package com.meta.travel.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 旅行相册 VO
 */
@Data
public class AlbumVO {

    private Long id;
    private String title;
    private String coverUrl;
    private String createTime;
    private List<Photo> photos;

    @Data
    public static class Photo {
        private Long id;
        private String url;
        private String caption;
        private Integer sortNo;
    }
}

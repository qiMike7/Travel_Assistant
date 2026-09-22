package com.meta.travel.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 保存旅行相册请求（对应 lvxingxiangce.vue）
 */
@Data
public class AlbumRequest {

    private String title;

    @Valid
    private List<Photo> photos;

    @Data
    public static class Photo {

        @NotBlank(message = "照片地址不能为空")
        private String url;

        /** 照片文字说明 */
        private String caption;
    }
}

package com.meta.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件上传配置
 */
@Data
@ConfigurationProperties(prefix = "travel.upload")
public class UploadProperties {

    /** 本地存储目录 */
    private String dir = "./uploads";

    /** 对外访问前缀 */
    private String urlPrefix = "/uploads";
}

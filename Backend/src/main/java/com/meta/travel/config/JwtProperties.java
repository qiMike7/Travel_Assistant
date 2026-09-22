package com.meta.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置
 */
@Data
@ConfigurationProperties(prefix = "travel.jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secret;

    /** 过期时间（毫秒） */
    private long expire = 604800000L;

    /** 令牌请求头 */
    private String header = "Authorization";

    /** 令牌前缀 */
    private String prefix = "Bearer ";
}

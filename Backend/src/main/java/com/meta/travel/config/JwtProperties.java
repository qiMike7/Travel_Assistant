package com.meta.travel.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * JWT 配置
 */
@Data
@ConfigurationProperties(prefix = "travel.jwt")
public class JwtProperties {

    /** HS256 签名密钥最小长度（256 bit = 32 字节） */
    private static final int MIN_SECRET_BYTES = 32;

    /** 签名密钥 */
    private String secret;

    /** 过期时间（毫秒） */
    private long expire = 604800000L;

    /** 令牌请求头 */
    private String header = "Authorization";

    /** 令牌前缀 */
    private String prefix = "Bearer ";

    /**
     * 启动校验：签名密钥必须由外部（环境变量 JWT_SECRET 或 config/application.properties）提供，
     * 不再内置默认值——避免"默认密钥"随代码进入公开仓库后被用于伪造令牌。
     * 缺失或长度不足时直接让应用启动失败，fail-fast 优于运行期报晦涩异常。
     */
    @PostConstruct
    void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("travel.jwt.secret 未配置：请通过环境变量 JWT_SECRET"
                    + "（或 config/application.properties 的 travel.jwt.secret）提供至少 32 字符的签名密钥");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("travel.jwt.secret 过短：HS256 签名密钥至少需 256 bit（32 字节）");
        }
    }
}

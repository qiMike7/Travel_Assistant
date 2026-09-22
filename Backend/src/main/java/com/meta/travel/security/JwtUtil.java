package com.meta.travel.security;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_USERNAME = "username";

    private final JwtProperties jwtProperties;

    /** 签名密钥不随请求变化，构建一次后复用（每个请求都要解析 token） */
    private volatile SecretKey cachedKey;

    private SecretKey key() {
        SecretKey key = cachedKey;
        if (key == null) {
            synchronized (this) {
                key = cachedKey;
                if (key == null) {
                    key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
                    cachedKey = key;
                }
            }
        }
        return key;
    }

    /**
     * 生成 token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpire());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /**
     * 解析 token，非法则抛出 401
     */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = claims.get(CLAIM_USER_ID, Number.class).longValue();
            String username = claims.get(CLAIM_USERNAME, String.class);
            return new LoginUser(userId, username);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("token 解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 从请求头中提取纯 token
     */
    public String resolveToken(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        String prefix = jwtProperties.getPrefix();
        if (prefix != null && !prefix.isBlank() && headerValue.startsWith(prefix)) {
            return headerValue.substring(prefix.length()).trim();
        }
        return headerValue.trim();
    }

    public long getExpire() {
        return jwtProperties.getExpire();
    }
}

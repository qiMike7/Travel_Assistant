package com.meta.travel.security;

/**
 * 登录用户信息（从 token 解析而来）
 */
public record LoginUser(Long userId, String username) {
}

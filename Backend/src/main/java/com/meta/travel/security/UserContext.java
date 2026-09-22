package com.meta.travel.security;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;

/**
 * 基于 ThreadLocal 的当前登录用户上下文
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录抛出 401
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long currentUserId() {
        return require().userId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    /** token 有效期（毫秒） */
    private Long expire;

    private UserVO user;
}

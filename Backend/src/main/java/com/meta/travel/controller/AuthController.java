package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.LoginRequest;
import com.meta.travel.dto.request.RegisterRequest;
import com.meta.travel.dto.response.LoginResponse;
import com.meta.travel.security.PublicApi;
import com.meta.travel.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PublicApi
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @PublicApi
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }
}

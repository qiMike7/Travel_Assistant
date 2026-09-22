package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.PasswordRequest;
import com.meta.travel.dto.request.ProfileRequest;
import com.meta.travel.dto.response.UserVO;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户 / 个人资料接口（对应 user.vue、profile.vue）
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<UserVO> profile() {
        return Result.success(userService.getProfile(UserContext.currentUserId()));
    }

    @PutMapping
    public Result<UserVO> updateProfile(@RequestBody ProfileRequest request) {
        return Result.success("资料已更新", userService.updateProfile(UserContext.currentUserId(), request));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordRequest request) {
        userService.updatePassword(UserContext.currentUserId(), request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 开通会员 / 分享领时长（user.vue 立即开通、share.vue 分享免费使用 12 小时）
     *
     * @param hours 增加的会员时长，默认 12 小时
     */
    @PostMapping("/vip")
    public Result<UserVO> grantVip(@RequestParam(defaultValue = "12") long hours) {
        return Result.success("开通成功", userService.grantVip(UserContext.currentUserId(), hours));
    }
}

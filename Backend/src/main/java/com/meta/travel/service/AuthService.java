package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.request.LoginRequest;
import com.meta.travel.dto.request.RegisterRequest;
import com.meta.travel.dto.response.LoginResponse;
import com.meta.travel.entity.Preference;
import com.meta.travel.entity.User;
import com.meta.travel.repository.PreferenceRepository;
import com.meta.travel.repository.UserRepository;
import com.meta.travel.security.JwtUtil;
import com.meta.travel.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 注册 / 登录服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PreferenceRepository preferenceRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名不能为空");
        }
        // 服务层存在性检查：注册前显式拦截重复用户名
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : username);
        user.setSignature("这个人很懒，什么都没有留下");
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // 并发注册兜底：username 唯一索引冲突时转为友好提示
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }

        // 初始化默认偏好
        Preference preference = new Preference();
        preference.setUserId(user.getId());
        preferenceRepository.save(preference);

        return buildLoginResponse(user);
    }

    /**
     * 用户名是否可用（未被占用）
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        return !userRepository.existsByUsername(username.trim());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, jwtUtil.getExpire(), userService.toVO(user));
    }
}

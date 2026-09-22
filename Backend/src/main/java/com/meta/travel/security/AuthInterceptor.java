package com.meta.travel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.travel.common.Result;
import com.meta.travel.common.ResultCode;
import com.meta.travel.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录鉴权拦截器
 * <p>
 * 校验 token 并写入 {@link UserContext}。命中 {@link PublicApi} 注解的接口放行。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 标注 @PublicApi 的接口无需登录
        if (handlerMethod.hasMethodAnnotation(PublicApi.class)
                || handlerMethod.getBeanType().isAnnotationPresent(PublicApi.class)) {
            return true;
        }

        String token = jwtUtil.resolveToken(request.getHeader(jwtProperties.getHeader()));
        if (token == null) {
            writeUnauthorized(response);
            return false;
        }
        try {
            UserContext.set(jwtUtil.parseToken(token));
            return true;
        } catch (Exception e) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

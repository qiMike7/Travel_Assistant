package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.PreferenceRequest;
import com.meta.travel.entity.Preference;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 旅行偏好接口（对应 preference.vue）
 */
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public Result<Preference> get() {
        return Result.success(preferenceService.get(UserContext.currentUserId()));
    }

    @PutMapping
    public Result<Preference> save(@Valid @RequestBody PreferenceRequest request) {
        return Result.success("设置已保存", preferenceService.save(UserContext.currentUserId(), request));
    }
}

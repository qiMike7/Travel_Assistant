package com.meta.travel.service;

import com.meta.travel.dto.request.PreferenceRequest;
import com.meta.travel.entity.Preference;
import com.meta.travel.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 旅行偏好服务（对应 preference.vue）
 */
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;

    @Transactional(readOnly = true)
    public Preference get(Long userId) {
        return preferenceRepository.findByUserId(userId).orElseGet(() -> {
            Preference p = new Preference();
            p.setUserId(userId);
            return preferenceRepository.save(p);
        });
    }

    @Transactional
    public Preference save(Long userId, PreferenceRequest request) {
        Preference p = preferenceRepository.findByUserId(userId).orElseGet(() -> {
            Preference n = new Preference();
            n.setUserId(userId);
            return n;
        });
        if (request.getTravelMode() != null) {
            p.setTravelMode(request.getTravelMode());
        }
        if (request.getBudget() != null) {
            p.setBudget(request.getBudget());
        }
        if (request.getStay() != null) {
            p.setStay(request.getStay());
        }
        if (request.getAiLevel() != null) {
            p.setAiLevel(request.getAiLevel());
        }
        if (request.getNotifications() != null) {
            p.setNotifications(request.getNotifications());
        }
        if (request.getDarkMode() != null) {
            p.setDarkMode(request.getDarkMode());
        }
        return preferenceRepository.save(p);
    }
}

package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.request.PasswordRequest;
import com.meta.travel.dto.request.ProfileRequest;
import com.meta.travel.dto.response.UserVO;
import com.meta.travel.entity.User;
import com.meta.travel.repository.UserRepository;
import com.meta.travel.util.DateUtil;
import com.meta.travel.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户 / 个人资料服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
    }

    @Transactional(readOnly = true)
    public UserVO getProfile(Long userId) {
        return toVO(getById(userId));
    }

    @Transactional
    public UserVO updateProfile(Long userId, ProfileRequest request) {
        User user = getById(userId);
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getRegion() != null) {
            user.setRegion(request.getRegion());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getWechat() != null) {
            user.setWechat(request.getWechat());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getQrcode() != null) {
            user.setQrcode(request.getQrcode());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        return toVO(userRepository.save(user));
    }

    @Transactional
    public void updatePassword(Long userId, PasswordRequest request) {
        User user = getById(userId);
        if (!PasswordUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "原密码不正确");
        }
        user.setPassword(PasswordUtil.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 开通/续期会员（user.vue 立即开通、share.vue 分享免费使用 12 小时）
     */
    @Transactional
    public UserVO grantVip(Long userId, long hours) {
        User user = getById(userId);
        LocalDateTime base = user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now())
                ? user.getVipExpireTime()
                : LocalDateTime.now();
        user.setVipExpireTime(base.plusHours(hours));
        user.setVip(true);
        return toVO(userRepository.save(user));
    }

    public UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        vo.setGender(user.getGender());
        vo.setRegion(user.getRegion());
        vo.setPhone(user.getPhone());
        vo.setWechat(user.getWechat());
        vo.setAvatar(user.getAvatar());
        vo.setQrcode(user.getQrcode());
        vo.setSignature(user.getSignature());
        // vip 字段只在入库时置 true；到期后应表现为非会员，故按到期时间推导
        vo.setVip(isVipActive(user));
        vo.setVipExpireTime(DateUtil.format(user.getVipExpireTime()));
        return vo;
    }

    /**
     * 会员是否在有效期内（到期后自动失效）。
     */
    public boolean isVipActive(User user) {
        return Boolean.TRUE.equals(user.getVip())
                && user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
    }
}

package com.meta.travel.dto.request;

import lombok.Data;

/**
 * 个人资料编辑请求（对应 profile.vue）
 */
@Data
public class ProfileRequest {

    private String nickname;
    /** 男 / 女 / 保密 */
    private String gender;
    private String region;
    private String phone;
    private String wechat;
    private String avatar;
    private String qrcode;
    private String signature;
}

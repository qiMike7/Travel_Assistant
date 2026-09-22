package com.meta.travel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息 VO（不含密码）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String gender;
    private String region;
    private String phone;
    private String wechat;
    private String avatar;
    private String qrcode;
    private String signature;
    private Boolean vip;
    private String vipExpireTime;
}

package com.meta.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户账号 + 个人资料
 * <p>对应前端 pages/user、pages/profile、pages/settings。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_user")
public class User extends BaseEntity {

    /** 登录账号 */
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** 密码（加密存储） */
    @Column(nullable = false, length = 128)
    private String password;

    /** 昵称 / 名字 */
    @Column(length = 64)
    private String nickname;

    /** 性别：男 / 女 / 保密 */
    @Column(length = 16)
    private String gender;

    /** 地区 */
    @Column(length = 64)
    private String region;

    /** 手机号 */
    @Column(length = 32)
    private String phone;

    /** 微信号 */
    @Column(length = 64)
    private String wechat;

    /** 头像 URL */
    @Column(length = 255)
    private String avatar;

    /** 个人二维码 URL */
    @Column(length = 255)
    private String qrcode;

    /** 个性签名 */
    @Column(length = 255)
    private String signature;

    /** 是否 VIP 会员 */
    @Column(name = "vip", nullable = false)
    private Boolean vip = false;

    /** 会员/会话到期时间 */
    @Column(name = "vip_expire_time")
    private LocalDateTime vipExpireTime;

    /** 账号状态：1 正常，0 禁用 */
    @Column(nullable = false)
    private Integer status = 1;
}

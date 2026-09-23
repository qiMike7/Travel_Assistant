package com.meta.travel.common;

/**
 * 统一响应状态码
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    /** 今日免费提问次数已用完（非会员） */
    CHAT_QUOTA_EXCEEDED(4291, "今日免费提问次数已用完"),
    /** 会员专属功能 */
    MEMBER_ONLY(4292, "该功能为会员专属"),
    /** 攻略保存数量已达上限 */
    PLAN_LIMIT_REACHED(4293, "攻略保存数量已达上限"),
    BUSINESS_ERROR(500, "业务处理失败"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

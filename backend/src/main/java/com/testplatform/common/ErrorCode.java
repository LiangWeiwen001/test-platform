package com.testplatform.common;

import lombok.Getter;

/**
 * 业务错误码枚举
 */
@Getter
public enum ErrorCode {

    /** 成功 */
    SUCCESS(0, "success"),

    /** 参数错误 */
    PARAM_ERROR(40000, "参数错误"),

    /** 未认证 */
    UNAUTHORIZED(40100, "未认证"),

    /** token 过期 */
    TOKEN_EXPIRED(40101, "token 过期"),

    /** 登录失败 */
    LOGIN_FAILED(40102, "登录失败"),

    /** 无权限 */
    FORBIDDEN(40300, "无权限"),

    /** 资源不存在 */
    NOT_FOUND(40400, "资源不存在"),

    /** 数据冲突 */
    CONFLICT(40900, "数据冲突"),

    /** 系统错误 */
    SYSTEM_ERROR(50000, "系统错误");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
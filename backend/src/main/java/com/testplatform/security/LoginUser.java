package com.testplatform.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户上下文（ThreadLocal 持有）
 * <p>
 * 由 JwtAuthFilter 在认证通过后写入，请求结束清理；Controller/Service 层通过静态方法获取。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String username;

    /** 角色编码集合 */
    private List<String> roles;

    /** 权限码集合 */
    private List<String> perms;

    /** 写入当前线程上下文 */
    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /** 获取当前登录用户（未登录返回 null） */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 清理当前线程上下文 */
    public static void clear() {
        HOLDER.remove();
    }
}
package com.testplatform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/刷新响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** access token */
    private String accessToken;

    /** refresh token */
    private String refreshToken;

    /** access token 有效期（秒） */
    private long expiresIn;

    /** 当前用户信息（含角色与权限码） */
    private UserInfoDTO user;
}
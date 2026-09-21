package com.testplatform.service.auth;

import com.testplatform.dto.auth.LoginRequest;
import com.testplatform.dto.auth.LoginResponse;
import com.testplatform.dto.auth.UserInfoDTO;

/**
 * 认证服务：登录 / 刷新 / 当前用户
 */
public interface AuthService {

    /**
     * 登录：校验账号密码，签发 access + refresh token
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新：校验 refresh token，重新签发 token 对
     */
    LoginResponse refresh(String refreshToken);

    /**
     * 当前用户信息（含角色与权限码）
     */
    UserInfoDTO me(Long userId);
}
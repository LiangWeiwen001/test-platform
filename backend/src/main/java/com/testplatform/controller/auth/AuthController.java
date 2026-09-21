package com.testplatform.controller.auth;

import com.testplatform.common.Result;
import com.testplatform.dto.auth.LoginRequest;
import com.testplatform.dto.auth.LoginResponse;
import com.testplatform.dto.auth.RefreshRequest;
import com.testplatform.dto.auth.UserInfoDTO;
import com.testplatform.security.LoginUser;
import com.testplatform.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录 / 登出 / 刷新 / 当前用户
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录：签发 access + refresh token
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /**
     * 登出：无状态 JWT，前端清除 token 即可，服务端仅返回成功
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    /**
     * 刷新：refresh token 换新 token 对
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.ok(authService.refresh(request.getRefreshToken()));
    }

    /**
     * 当前登录用户信息（含角色与权限码）
     */
    @GetMapping("/me")
    public Result<UserInfoDTO> me() {
        LoginUser loginUser = LoginUser.get();
        return Result.ok(authService.me(loginUser.getUserId()));
    }
}
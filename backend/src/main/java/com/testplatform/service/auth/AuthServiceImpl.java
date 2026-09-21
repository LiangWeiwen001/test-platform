package com.testplatform.service.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.testplatform.common.BusinessException;
import com.testplatform.common.ErrorCode;
import com.testplatform.dto.auth.LoginRequest;
import com.testplatform.dto.auth.LoginResponse;
import com.testplatform.dto.auth.UserInfoDTO;
import com.testplatform.entity.SysPermission;
import com.testplatform.entity.SysRole;
import com.testplatform.entity.SysRolePermission;
import com.testplatform.entity.SysUser;
import com.testplatform.entity.SysUserRole;
import com.testplatform.mapper.SysPermissionMapper;
import com.testplatform.mapper.SysRoleMapper;
import com.testplatform.mapper.SysRolePermissionMapper;
import com.testplatform.mapper.SysUserMapper;
import com.testplatform.mapper.SysUserRoleMapper;
import com.testplatform.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** admin 角色编码：直接授予全部权限 */
    private static final String ADMIN_ROLE_CODE = "admin";

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysRolePermissionMapper sysRolePermissionMapper;

    private final SysPermissionMapper sysPermissionMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Value("${jwt.access-expire-seconds}")
    private long accessExpireSeconds;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 按 username 查询（MyBatis-Plus 逻辑删除自动过滤 deleted=0）
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.getUsername()));
        // 统一提示，防账号枚举：用户不存在与密码错误返回相同信息
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "账号已被禁用");
        }

        List<SysRole> roles = getRoles(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();
        List<String> perms = getPerms(roles);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roleCodes, perms);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        return new LoginResponse(accessToken, refreshToken, accessExpireSeconds,
                buildUserInfo(user, roleCodes, perms));
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "refresh token 已过期");
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "refresh token 无效");
        }

        Long userId = jwtUtil.getUserId(claims);
        SysUser user = sysUserMapper.selectById(userId);
        // 用户被删除或禁用后，refresh token 一并失效
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "refresh token 无效");
        }

        List<SysRole> roles = getRoles(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();
        List<String> perms = getPerms(roles);

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roleCodes, perms);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        return new LoginResponse(newAccessToken, newRefreshToken, accessExpireSeconds,
                buildUserInfo(user, roleCodes, perms));
    }

    @Override
    public UserInfoDTO me(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        List<SysRole> roles = getRoles(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();
        List<String> perms = getPerms(roles);
        return buildUserInfo(user, roleCodes, perms);
    }

    /**
     * 查询用户启用的角色
     */
    private List<SysRole> getRoles(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds).eq(SysRole::getStatus, 1));
    }

    /**
     * 查询角色权限码集合；admin 角色直接授予全部权限
     */
    private List<String> getPerms(List<SysRole> roles) {
        if (roles.isEmpty()) {
            return List.of();
        }
        if (roles.stream().anyMatch(role -> ADMIN_ROLE_CODE.equals(role.getRoleCode()))) {
            List<SysPermission> all = sysPermissionMapper.selectList(
                    Wrappers.<SysPermission>lambdaQuery().eq(SysPermission::getStatus, 1));
            return all.stream().map(SysPermission::getPermCode).toList();
        }
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        List<SysRolePermission> rolePerms = sysRolePermissionMapper.selectList(
                Wrappers.<SysRolePermission>lambdaQuery().in(SysRolePermission::getRoleId, roleIds));
        if (rolePerms.isEmpty()) {
            return List.of();
        }
        List<Long> permIds = rolePerms.stream().map(SysRolePermission::getPermissionId).toList();
        List<SysPermission> perms = sysPermissionMapper.selectList(
                Wrappers.<SysPermission>lambdaQuery().in(SysPermission::getId, permIds).eq(SysPermission::getStatus, 1));
        return perms.stream().map(SysPermission::getPermCode).toList();
    }

    /**
     * 组装用户信息 DTO
     */
    private UserInfoDTO buildUserInfo(SysUser user, List<String> roleCodes, List<String> perms) {
        return new UserInfoDTO(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(),
                roleCodes, perms);
    }
}
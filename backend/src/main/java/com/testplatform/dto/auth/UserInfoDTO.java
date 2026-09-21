package com.testplatform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    /** 用户 ID */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 昵称/姓名 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 角色编码集合 */
    private List<String> roles;

    /** 权限码集合 */
    private List<String> perms;
}
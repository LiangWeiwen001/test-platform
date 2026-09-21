package com.testplatform.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新建用户请求
 */
@Data
public class UserCreateRequest {

    /** 登录账号 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过 50")
    private String username;

    /** 昵称/姓名 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    /** 初始密码（明文，服务端 BCrypt 加密存储） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度需在 6-50 之间")
    private String password;

    /** 邮箱 */
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    /** 手机号 */
    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    /** 状态：1启用 / 0禁用（缺省为启用） */
    private Integer status;

    /** 绑定角色 ID 列表 */
    private List<Long> roleIds;
}

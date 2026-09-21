package com.testplatform.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑用户请求（不含用户名与密码）
 */
@Data
public class UserUpdateRequest {

    /** 昵称/姓名 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    /** 邮箱 */
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    /** 手机号 */
    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    /** 状态：1启用 / 0禁用 */
    private Integer status;

    /** 绑定角色 ID 列表（全量覆盖） */
    private List<Long> roleIds;
}

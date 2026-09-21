package com.testplatform.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色编辑请求
 */
@Data
public class RoleUpdateRequest {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;

    private Integer status;

    /** 关联权限 ID 列表 */
    private List<Long> permIds;
}

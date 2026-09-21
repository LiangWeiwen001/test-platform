package com.testplatform.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色新建请求
 */
@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;

    /** 状态：1启用 / 0禁用，前端不传默认1 */
    private Integer status;

    /** 关联权限 ID 列表（可空） */
    private List<Long> permIds;
}

package com.testplatform.dto.system;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色视图对象
 */
@Data
public class RoleVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    /** 已关联的权限 ID 列表 */
    private List<Long> permIds;

    private LocalDateTime createdAt;
}

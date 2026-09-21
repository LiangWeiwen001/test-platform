package com.testplatform.service.system;

import com.testplatform.common.PageResult;
import com.testplatform.dto.system.PermissionNode;
import com.testplatform.dto.system.RoleCreateRequest;
import com.testplatform.dto.system.RoleQueryRequest;
import com.testplatform.dto.system.RoleUpdateRequest;
import com.testplatform.dto.system.RoleVO;

import java.util.List;

/**
 * 角色管理服务
 */
public interface RoleService {

    /** 分页查询角色 */
    PageResult<RoleVO> pageRoles(RoleQueryRequest request);

    /** 查询全部启用角色（供下拉使用） */
    List<RoleVO> listAllEnabledRoles();

    /** 新建角色 */
    RoleVO createRole(RoleCreateRequest request);

    /** 编辑角色 */
    void updateRole(Long id, RoleUpdateRequest request);

    /** 删除角色（逻辑删除 + 清理关联） */
    void deleteRole(Long id);

    /** 权限树 */
    List<PermissionNode> getPermissionTree();

    /** 分配角色权限（先删后插） */
    void assignPermissions(Long roleId, List<Long> permIds);
}

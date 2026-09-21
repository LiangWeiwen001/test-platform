package com.testplatform.controller.system;

import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.system.PermissionNode;
import com.testplatform.dto.system.RoleCreateRequest;
import com.testplatform.dto.system.RoleQueryRequest;
import com.testplatform.dto.system.RoleUpdateRequest;
import com.testplatform.dto.system.RoleVO;
import com.testplatform.security.RequirePermission;
import com.testplatform.service.system.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/roles")
    @RequirePermission("system:role:list")
    public Result<PageResult<RoleVO>> pageRoles(RoleQueryRequest request) {
        return Result.ok(roleService.pageRoles(request));
    }

    @GetMapping("/roles/all")
    @RequirePermission("system:role:list")
    public Result<List<RoleVO>> listAllRoles() {
        return Result.ok(roleService.listAllEnabledRoles());
    }

    @PostMapping("/roles")
    @RequirePermission("system:role:add")
    public Result<RoleVO> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return Result.ok(roleService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    @RequirePermission("system:role:edit")
    public Result<Void> updateRole(@PathVariable Long id,
                                   @Valid @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(id, request);
        return Result.ok();
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("system:role:delete")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }

    @GetMapping("/permissions/tree")
    @RequirePermission("system:role:list")
    public Result<List<PermissionNode>> getPermissionTree() {
        return Result.ok(roleService.getPermissionTree());
    }

    @PutMapping("/roles/{id}/permissions")
    @RequirePermission("system:role:assignPerm")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @RequestBody List<Long> permIds) {
        roleService.assignPermissions(id, permIds);
        return Result.ok();
    }
}

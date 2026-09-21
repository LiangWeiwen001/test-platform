package com.testplatform.controller.system;

import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.system.PasswordResetRequest;
import com.testplatform.dto.system.UserCreateRequest;
import com.testplatform.dto.system.UserQueryRequest;
import com.testplatform.dto.system.UserUpdateRequest;
import com.testplatform.dto.system.UserVO;
import com.testplatform.security.RequirePermission;
import com.testplatform.service.system.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口（REQ-003）
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户分页列表（支持关键字/状态筛选）
     */
    @GetMapping
    @RequirePermission("system:user:list")
    public Result<PageResult<UserVO>> page(UserQueryRequest query) {
        return Result.ok(userService.page(query));
    }

    /**
     * 新建用户（含角色绑定）
     */
    @PostMapping
    @RequirePermission("system:user:add")
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userService.create(request));
    }

    /**
     * 编辑用户（含角色调整）
     */
    @PutMapping("/{id}")
    @RequirePermission("system:user:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.ok();
    }

    /**
     * 删除用户（逻辑删除 + 清理角色绑定）
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    @RequirePermission("system:user:edit")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.ok();
    }

    /**
     * 重置密码（管理员指定新密码）
     */
    @PutMapping("/{id}/password")
    @RequirePermission("system:user:resetPwd")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(id, request.getPassword());
        return Result.ok();
    }
}

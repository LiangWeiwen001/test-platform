package com.testplatform.service.system;

import com.testplatform.common.PageResult;
import com.testplatform.dto.system.UserCreateRequest;
import com.testplatform.dto.system.UserQueryRequest;
import com.testplatform.dto.system.UserUpdateRequest;
import com.testplatform.dto.system.UserVO;

/**
 * 用户管理服务
 */
public interface UserService {

    /**
     * 用户分页列表（keyword 模糊匹配 username/nickname，status 精确筛选）
     */
    PageResult<UserVO> page(UserQueryRequest query);

    /**
     * 新建用户（用户名唯一校验 + BCrypt 密码 + 绑定角色）
     */
    UserVO create(UserCreateRequest request);

    /**
     * 编辑用户（同步覆盖角色绑定）
     */
    void update(Long id, UserUpdateRequest request);

    /**
     * 逻辑删除用户并清理角色绑定（admin 账号保护）
     */
    void delete(Long id);

    /**
     * 启用/禁用用户
     */
    void updateStatus(Long id, Integer status);

    /**
     * 重置密码（BCrypt）
     */
    void resetPassword(Long id, String password);
}

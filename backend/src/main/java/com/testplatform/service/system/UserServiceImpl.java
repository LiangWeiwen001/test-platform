package com.testplatform.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.BusinessException;
import com.testplatform.common.ErrorCode;
import com.testplatform.common.PageResult;
import com.testplatform.dto.system.UserCreateRequest;
import com.testplatform.dto.system.UserQueryRequest;
import com.testplatform.dto.system.UserUpdateRequest;
import com.testplatform.dto.system.UserVO;
import com.testplatform.entity.SysRole;
import com.testplatform.entity.SysUser;
import com.testplatform.entity.SysUserRole;
import com.testplatform.mapper.SysRoleMapper;
import com.testplatform.mapper.SysUserMapper;
import com.testplatform.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户管理服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 内置管理员账号：禁止删除 */
    private static final String ADMIN_USERNAME = "admin";

    /** 分页默认值与上限（见 接口设计 1.3） */
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 10L;
    private static final long MAX_SIZE = 100L;

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> page(UserQueryRequest query) {
        long page = query.getPage() == null || query.getPage() < 1 ? DEFAULT_PAGE : query.getPage();
        long size = query.getSize() == null || query.getSize() < 1 ? DEFAULT_SIZE : Math.min(query.getSize(), MAX_SIZE);
        String keyword = query.getKeyword();
        Integer status = query.getStatus();

        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getNickname, keyword))
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getId);

        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page, size, result.getTotal(), records);
    }

    @Override
    public UserVO create(UserCreateRequest request) {
        Long exists = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        sysUserMapper.insert(user);

        saveUserRoles(user.getId(), request.getRoleIds());
        return toVO(sysUserMapper.selectById(user.getId()));
    }

    @Override
    public void update(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        sysUserMapper.updateById(user);

        // 角色绑定全量覆盖（先删后插）
        saveUserRoles(id, request.getRoleIds());
    }

    @Override
    public void delete(Long id) {
        SysUser user = requireUser(id);
        if (ADMIN_USERNAME.equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "内置管理员账号不可删除");
        }
        // 逻辑删除（MyBatis-Plus 自动 UPDATE deleted=1）
        sysUserMapper.deleteById(id);
        // 关联表无逻辑删除，物理清理角色绑定
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "状态值只能为 0 或 1");
        }
        SysUser user = requireUser(id);
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String password) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(password));
        sysUserMapper.updateById(user);
    }

    /**
     * 查询用户，不存在抛 40400
     */
    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    /**
     * 角色绑定：先清空再按 roleIds 重建
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds.stream().distinct().toList()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    /**
     * 实体转 VO（含角色 ID 与角色名称）
     */
    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());

        List<Long> roleIds = sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()))
                .stream().map(SysUserRole::getRoleId).toList();
        vo.setRoleIds(roleIds);
        if (roleIds.isEmpty()) {
            vo.setRoleNames(List.of());
        } else {
            vo.setRoleNames(sysRoleMapper.selectList(
                            Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds))
                    .stream().map(SysRole::getRoleName).toList());
        }
        return vo;
    }
}

package com.testplatform.service.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.testplatform.common.BusinessException;
import com.testplatform.common.ErrorCode;
import com.testplatform.common.PageResult;
import com.testplatform.dto.system.PermissionNode;
import com.testplatform.dto.system.RoleCreateRequest;
import com.testplatform.dto.system.RoleQueryRequest;
import com.testplatform.dto.system.RoleUpdateRequest;
import com.testplatform.dto.system.RoleVO;
import com.testplatform.entity.SysPermission;
import com.testplatform.entity.SysRole;
import com.testplatform.entity.SysRolePermission;
import com.testplatform.entity.SysUserRole;
import com.testplatform.mapper.SysPermissionMapper;
import com.testplatform.mapper.SysRoleMapper;
import com.testplatform.mapper.SysRolePermissionMapper;
import com.testplatform.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public PageResult<RoleVO> pageRoles(RoleQueryRequest request) {
        var wrapper = Wrappers.<SysRole>lambdaQuery()
                .like(StringUtils.hasText(request.getKeyword()), SysRole::getRoleName, request.getKeyword())
                .or()
                .like(StringUtils.hasText(request.getKeyword()), SysRole::getRoleCode, request.getKeyword())
                .eq(request.getStatus() != null, SysRole::getStatus, request.getStatus())
                .orderByAsc(SysRole::getId);

        // 如果 keyword 非空需要 AND 包裹 OR
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper = Wrappers.<SysRole>lambdaQuery()
                    .and(w -> w
                            .like(SysRole::getRoleName, request.getKeyword())
                            .or()
                            .like(SysRole::getRoleCode, request.getKeyword()))
                    .eq(request.getStatus() != null, SysRole::getStatus, request.getStatus())
                    .orderByAsc(SysRole::getId);
        }

        long total = roleMapper.selectCount(wrapper);
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;
        int offset = (page - 1) * size;

        List<SysRole> roles = roleMapper.selectList(
                wrapper.last("LIMIT " + size + " OFFSET " + offset));

        List<RoleVO> voList = toVOList(roles);
        return new PageResult<>(page, size, total, voList);
    }

    @Override
    public List<RoleVO> listAllEnabledRoles() {
        List<SysRole> roles = roleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId));
        return toVOList(roles);
    }

    @Override
    @Transactional
    public RoleVO createRole(RoleCreateRequest request) {
        // roleCode 唯一性检查
        Long count = roleMapper.selectCount(
                Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, request.getRoleCode()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        roleMapper.insert(role);

        // 写入角色-权限关联
        if (request.getPermIds() != null && !request.getPermIds().isEmpty()) {
            insertRolePermissions(role.getId(), request.getPermIds());
        }

        return getRoleVO(role);
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleUpdateRequest request) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        // roleCode 唯一性检查（排除自身）
        Long count = roleMapper.selectCount(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getRoleCode, request.getRoleCode())
                        .ne(SysRole::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }

        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        roleMapper.updateById(role);

        // 重新写入角色-权限关联（先删后插）
        if (request.getPermIds() != null) {
            rolePermissionMapper.delete(
                    Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, id));
            if (!request.getPermIds().isEmpty()) {
                insertRolePermissions(id, request.getPermIds());
            }
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        // 逻辑删除角色
        roleMapper.deleteById(id);
        // 清理角色-权限关联
        rolePermissionMapper.delete(
                Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, id));
        // 清理用户-角色关联
        userRoleMapper.delete(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, id));
    }

    @Override
    public List<PermissionNode> getPermissionTree() {
        List<SysPermission> all = permissionMapper.selectList(
                Wrappers.<SysPermission>lambdaQuery()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort));

        List<PermissionNode> nodes = all.stream().map(p -> {
            PermissionNode node = new PermissionNode();
            node.setId(p.getId());
            node.setParentId(p.getParentId());
            node.setPermName(p.getPermName());
            node.setPermCode(p.getPermCode());
            // M→MENU, B→BUTTON
            node.setType("M".equals(p.getPermType()) ? "MENU" : "BUTTON");
            node.setPath(p.getPath());
            node.setIcon(p.getIcon());
            node.setSort(p.getSort());
            node.setChildren(new ArrayList<>());
            return node;
        }).toList();

        return buildTree(nodes);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        // 先删后插
        rolePermissionMapper.delete(
                Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, roleId));
        if (permIds != null && !permIds.isEmpty()) {
            insertRolePermissions(roleId, permIds);
        }
    }

    // ---- 私有方法 ----

    /** 批量插入角色-权限关联 */
    private void insertRolePermissions(Long roleId, List<Long> permIds) {
        List<SysRolePermission> batch = permIds.stream().map(permId -> {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            return rp;
        }).toList();
        for (SysRolePermission rp : batch) {
            rolePermissionMapper.insert(rp);
        }
    }

    /** 查询角色的权限 ID 列表 */
    private List<Long> getRolePermIds(Long roleId) {
        List<SysRolePermission> list = rolePermissionMapper.selectList(
                Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, roleId));
        return list.stream().map(SysRolePermission::getPermissionId).toList();
    }

    /** 角色实体 → VO */
    private RoleVO getRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setPermIds(getRolePermIds(role.getId()));
        return vo;
    }

    /** 批量角色实体 → VO 列表 */
    private List<RoleVO> toVOList(List<SysRole> roles) {
        if (roles.isEmpty()) {
            return List.of();
        }
        // 批量查所有角色的权限 ID
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        Map<Long, List<Long>> permMap = rolePermissionMapper.selectList(
                Wrappers.<SysRolePermission>lambdaQuery().in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .collect(Collectors.groupingBy(SysRolePermission::getRoleId,
                        Collectors.mapping(SysRolePermission::getPermissionId, Collectors.toList())));

        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            vo.setDescription(role.getDescription());
            vo.setStatus(role.getStatus());
            vo.setCreatedAt(role.getCreatedAt());
            vo.setPermIds(permMap.getOrDefault(role.getId(), Collections.emptyList()));
            return vo;
        }).toList();
    }

    /** 构建权限树 */
    private List<PermissionNode> buildTree(List<PermissionNode> nodes) {
        Map<Long, List<PermissionNode>> childrenMap = nodes.stream()
                .collect(Collectors.groupingBy(PermissionNode::getParentId));
        nodes.forEach(n -> n.setChildren(childrenMap.getOrDefault(n.getId(), new ArrayList<>())));
        return nodes.stream()
                .filter(n -> n.getParentId() == 0L)
                .toList();
    }
}

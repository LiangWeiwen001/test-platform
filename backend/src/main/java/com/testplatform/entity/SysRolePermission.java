package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联表（无逻辑删除，随主记录级联清理）
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    /** 主键（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色 ID（外键意图 → sys_role.id） */
    private Long roleId;

    /** 权限 ID（外键意图 → sys_permission.id） */
    private Long permissionId;

    /** 创建时间（依赖 DB 默认值） */
    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
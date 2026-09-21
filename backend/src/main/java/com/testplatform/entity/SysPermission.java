package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限表（菜单 + 按钮）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /** 父级 ID（0=顶级菜单；外键意图 → sys_permission.id） */
    private Long parentId;

    /** 权限/菜单名称 */
    private String permName;

    /** 权限码（如 system:user:list） */
    private String permCode;

    /** 类型：M菜单 / B按钮 */
    private String permType;

    /** 前端路由路径（菜单用） */
    private String path;

    /** 菜单图标 */
    private String icon;

    /** 排序号（同级升序） */
    private Integer sort;

    /** 状态：1启用 / 0禁用 */
    private Integer status;
}
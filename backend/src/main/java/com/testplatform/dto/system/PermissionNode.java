package com.testplatform.dto.system;

import lombok.Data;

import java.util.List;

/**
 * 权限树节点
 */
@Data
public class PermissionNode {

    private Long id;

    private Long parentId;

    private String permName;

    private String permCode;

    /** MENU 或 BUTTON（对应 DB 中 M/B） */
    private String type;

    private String path;

    private String icon;

    private Integer sort;

    private List<PermissionNode> children;
}

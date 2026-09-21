package com.testplatform.dto.system;

import lombok.Data;

/**
 * 角色分页查询请求
 */
@Data
public class RoleQueryRequest {

    private Integer page = 1;

    private Integer size = 10;

    /** 按角色名/角色编码模糊搜索 */
    private String keyword;

    /** 状态过滤：1启用 / 0禁用 */
    private Integer status;
}

package com.testplatform.dto.system;

import lombok.Data;

/**
 * 用户分页查询请求
 */
@Data
public class UserQueryRequest {

    /** 页码（从 1 开始） */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /** 关键字：模糊匹配 username / nickname */
    private String keyword;

    /** 状态筛选：1启用 / 0禁用（null 不过滤） */
    private Integer status;
}

package com.testplatform.dto.env;

import lombok.Data;

/**
 * 环境分页查询请求（REQ-005）
 */
@Data
public class EnvironmentQueryRequest {

    /** 页码（从 1 开始） */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /** 关键字：模糊匹配 env_name / env_code / description */
    private String keyword;
}
package com.testplatform.dto.testcase;

import lombok.Data;

/**
 * 用例分页查询请求（REQ-006）
 */
@Data
public class CaseQueryRequest {

    /** 页码（默认 1） */
    private Long page;

    /** 每页条数（默认 10，最大 100） */
    private Long size;

    /** 关键字（LIKE case_name/description） */
    private String keyword;

    /** 用例类型筛选 */
    private String caseType;

    /** 优先级筛选 */
    private String caseLevel;

    /** 标签 ID 筛选 */
    private Long tagId;

    /** 状态筛选：1启用 / 0停用 */
    private Integer status;
}
package com.testplatform.dto.search;

import lombok.Data;

/**
 * 全局搜索结果项-用例（REQ-008）
 */
@Data
public class CaseHitVO {

    private Long id;

    /** 用例名称 */
    private String name;

    /** 用例类型 */
    private String type;

    /** 优先级 */
    private String level;

    /** 命中的字段名 */
    private String matchField;
}
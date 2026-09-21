package com.testplatform.dto.search;

import lombok.Data;

/**
 * 全局搜索结果项-环境（REQ-008）
 */
@Data
public class EnvHitVO {

    private Long id;

    /** 环境名称 */
    private String name;

    /** 环境编码 */
    private String code;

    /** 命中的字段名 */
    private String matchField;
}
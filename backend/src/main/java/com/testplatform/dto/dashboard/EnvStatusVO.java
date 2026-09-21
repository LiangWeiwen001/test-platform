package com.testplatform.dto.dashboard;

import lombok.Data;

/**
 * 环境状态项（REQ-007）
 */
@Data
public class EnvStatusVO {

    /** 环境 ID */
    private Long id;

    /** 环境编码 */
    private String envCode;

    /** 环境名称 */
    private String envName;

    /** 环境类型 */
    private String envType;

    /** 环境地址 */
    private String baseUrl;

    /** 状态：1启用 / 0停用 */
    private Integer status;

    /** 最近健康状态：UP / DOWN / 未检测 */
    private String healthStatus;

    /** 最近健康延迟（ms） */
    private Long latencyMs;
}
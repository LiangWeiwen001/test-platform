package com.testplatform.dto.env;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境列表/详情视图对象（REQ-005）
 */
@Data
public class EnvironmentVO {

    /** 环境 ID */
    private Long id;

    /** 环境标识（dev/test/prod） */
    private String envCode;

    /** 环境名称 */
    private String envName;

    /** 被测系统基础地址 */
    private String baseUrl;

    /** 环境描述 */
    private String description;

    /** 排序号 */
    private Integer sort;

    /** 状态：1启用 / 0停用 */
    private Integer status;

    /** 创建人 ID */
    private Long createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}
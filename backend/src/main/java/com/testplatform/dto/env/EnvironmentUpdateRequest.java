package com.testplatform.dto.env;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑环境请求（REQ-005）
 */
@Data
public class EnvironmentUpdateRequest {

    /** 环境标识（dev/test/prod） */
    @NotBlank(message = "环境编码不能为空")
    @Size(max = 50, message = "环境编码长度不能超过 50")
    private String envCode;

    /** 环境名称 */
    @NotBlank(message = "环境名称不能为空")
    @Size(max = 50, message = "环境名称长度不能超过 50")
    private String envName;

    /** 被测系统基础地址（健康自检探测目标） */
    @Size(max = 255, message = "地址长度不能超过 255")
    private String baseUrl;

    /** 环境描述 */
    @Size(max = 255, message = "描述长度不能超过 255")
    private String description;

    /** 状态：1启用 / 0停用 */
    private Integer status;
}
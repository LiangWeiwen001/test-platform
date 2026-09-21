package com.testplatform.dto.testcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建用例请求（REQ-006）
 */
@Data
public class CaseCreateRequest {

    /** 用例名称 */
    @NotBlank(message = "用例名称不能为空")
    @Size(max = 200, message = "用例名称最长 200 字符")
    private String caseName;

    /** 用例类型：API接口/PERF性能/DB数据库/COMMON通用 */
    @NotBlank(message = "用例类型不能为空")
    private String caseType;

    /** 优先级：P0/P1/P2 */
    @NotBlank(message = "用例级别不能为空")
    private String caseLevel;

    /** 用例描述 */
    @Size(max = 1000, message = "描述最长 1000 字符")
    private String description;

    /** 请求配置（JSON 存储） */
    private String requestConfig;

    /** 预期结果（JSON 存储） */
    private String expectedResult;

    /** 标签 ID 列表（逗号分隔） */
    private String tagIds;

    /** 状态：1启用 / 0停用 */
    private Integer status;
}
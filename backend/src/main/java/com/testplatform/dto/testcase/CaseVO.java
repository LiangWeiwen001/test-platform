package com.testplatform.dto.testcase;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用例视图对象（含标签名列表）
 */
@Data
public class CaseVO {

    private Long id;

    /** 用例名称 */
    private String caseName;

    /** 用例类型 */
    private String caseType;

    /** 优先级 */
    private String caseLevel;

    /** 用例描述 */
    private String description;

    /** 请求配置（JSON 字符串） */
    private String requestConfig;

    /** 预期结果（JSON 字符串） */
    private String expectedResult;

    /** 标签 ID 列表（逗号分隔） */
    private String tagIds;

    /** 标签名列表（由 tagIds 解析） */
    private List<String> tagNames;

    /** 用例版本 */
    private String version;

    /** 状态：1启用 / 0停用 */
    private Integer status;

    /** 创建人 */
    private Long createdBy;

    /** 最后修改人 */
    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
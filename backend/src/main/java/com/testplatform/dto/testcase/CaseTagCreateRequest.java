package com.testplatform.dto.testcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建标签请求（REQ-006）
 */
@Data
public class CaseTagCreateRequest {

    /** 标签名称 */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称最长 50 字符")
    private String tagName;

    /** 标签颜色（默认 #409EFF） */
    @Size(max = 20, message = "颜色值最长 20 字符")
    private String tagColor;
}
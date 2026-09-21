package com.testplatform.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志动态（REQ-007）
 */
@Data
public class ActivityVO {

    /** 用户名 */
    private String username;

    /** 模块 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 结果：success / fail */
    private String result;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
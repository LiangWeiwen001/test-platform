package com.testplatform.dto.env;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境健康检查记录视图对象（REQ-005）
 */
@Data
public class HealthRecordVO {

    /** 记录 ID */
    private Long id;

    /** 环境 ID */
    private Long envId;

    /** 检查类型（HTTP/MySQL/Redis） */
    private String checkType;

    /** 检测目标（URL/连接串） */
    private String target;

    /** 结果：UP / DOWN */
    private String status;

    /** 响应耗时（毫秒） */
    private Integer latencyMs;

    /** 失败原因 */
    private String errorMsg;

    /** 触发人 ID */
    private Long checkedBy;

    /** 检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime checkedAt;
}
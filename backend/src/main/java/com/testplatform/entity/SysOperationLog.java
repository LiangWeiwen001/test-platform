package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志表（只增不改，无 updated_at / deleted）
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    /** 主键（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 ID（外键意图 → sys_user.id） */
    private Long userId;

    /** 操作人账号（冗余，防用户删除后丢失） */
    private String username;

    /** 所属模块（auth/system/env/case/dashboard/search） */
    private String module;

    /** 操作描述（如"新增用户"） */
    private String operation;

    /** HTTP 方法（GET/POST/PUT/DELETE） */
    private String method;

    /** 请求路径 */
    private String url;

    /** 请求参数（JSON 字符串，敏感字段脱敏） */
    private String params;

    /** 结果：1成功 / 0失败 */
    private Integer result;

    /** 失败原因 */
    private String errorMsg;

    /** 操作 IP */
    private String ip;

    /** 耗时（毫秒） */
    private Integer costMs;

    /** 操作时间（依赖 DB 默认值） */
    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
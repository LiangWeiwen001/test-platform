package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境健康检查记录表（只增不改，无 updated_at / deleted）
 */
@Data
@TableName("td_environment_health")
public class TdEnvironmentHealth {

    /** 主键（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 环境 ID（外键意图 → td_environment.id） */
    private Long envId;

    /** 检查类型（HTTP/MySQL/Redis） */
    private String checkType;

    /** 检测目标（URL/连接串，脱敏存储） */
    private String target;

    /** 结果：UP / DOWN */
    private String status;

    /** 响应耗时（毫秒） */
    private Integer latencyMs;

    /** 失败原因 */
    private String errorMsg;

    /** 触发人（外键意图 → sys_user.id；定时任务为 0） */
    private Long checkedBy;

    /** 检查时间（依赖 DB 默认值） */
    @TableField(value = "checked_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime checkedAt;
}
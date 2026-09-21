package com.testplatform.common;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体公共字段基类
 * <p>
 * createdAt / updatedAt 依赖数据库默认值（CURRENT_TIMESTAMP / ON UPDATE CURRENT_TIMESTAMP），
 * 插入与更新时均不携带这两个字段，由 MySQL 自动维护。
 */
@Data
public abstract class BaseEntity {

    /** 主键（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间（依赖 DB 默认值，插入时不写入） */
    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    /** 更新时间（依赖 DB ON UPDATE CURRENT_TIMESTAMP，插入/更新均不写入） */
    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;

    /** 逻辑删除：0 正常 / 1 已删除 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
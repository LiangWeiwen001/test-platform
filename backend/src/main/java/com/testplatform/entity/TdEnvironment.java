package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试环境表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("td_environment")
public class TdEnvironment extends BaseEntity {

    /** 环境标识（dev/test/prod） */
    private String envCode;

    /** 环境名称 */
    private String envName;

    /** 被测系统基础地址（健康自检探测目标） */
    private String baseUrl;

    /** 环境描述 */
    private String description;

    /** 排序号 */
    private Integer sort;

    /** 状态：1启用 / 0停用 */
    private Integer status;

    /** 创建人（外键意图 → sys_user.id） */
    private Long createdBy;
}
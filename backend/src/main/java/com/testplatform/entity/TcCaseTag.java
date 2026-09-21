package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用例标签表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tc_case_tag")
public class TcCaseTag extends BaseEntity {

    /** 标签名称 */
    private String tagName;

    /** 标签颜色（Element Plus 色值） */
    private String tagColor;
}
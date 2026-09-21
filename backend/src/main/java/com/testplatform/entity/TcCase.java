package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试用例表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tc_case")
public class TcCase extends BaseEntity {

    /** 用例名称 */
    private String caseName;

    /** 用例类型：API接口/PERF性能/DB数据库/COMMON通用 */
    private String caseType;

    /** 优先级：P0/P1/P2 */
    private String caseLevel;

    /** 用例描述 */
    private String description;

    /** 请求配置（接口用例：method/url/headers/body，JSON 存储） */
    private String requestConfig;

    /** 预期结果（断言配置，JSON 存储） */
    private String expectedResult;

    /** 标签 ID 列表（逗号分隔，如 "1,2,3"） */
    private String tagIds;

    /** 用例版本（编辑时递增） */
    private String version;

    /** 状态：1启用 / 0停用 */
    private Integer status;

    /** 创建人（外键意图 → sys_user.id） */
    private Long createdBy;

    /** 最后修改人（外键意图 → sys_user.id） */
    private Long updatedBy;
}
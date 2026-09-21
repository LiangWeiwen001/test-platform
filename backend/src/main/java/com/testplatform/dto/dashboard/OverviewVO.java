package com.testplatform.dto.dashboard;

import lombok.Data;

import java.util.List;

/**
 * 工作台概览统计（REQ-007）
 */
@Data
public class OverviewVO {

    /** 用户总数 */
    private Long userCount;

    /** 环境总数 */
    private Long envCount;

    /** 用例总数 */
    private Long caseCount;

    /** 健康环境数（最近一次检查 UP） */
    private Long healthUpCount;
}
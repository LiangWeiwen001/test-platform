package com.testplatform.dto.dashboard;

import lombok.Data;

import java.util.List;

/**
 * 近 7 日趋势（REQ-007）
 */
@Data
public class TrendVO {

    /** 日期序列（yyyy-MM-dd，近 7 天） */
    private List<String> dates;

    /** 每日新增用例数 */
    private List<Long> caseCounts;

    /** 每日健康检查次数 */
    private List<Long> healthChecks;
}
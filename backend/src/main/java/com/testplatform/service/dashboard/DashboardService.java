package com.testplatform.service.dashboard;

import com.testplatform.dto.dashboard.ActivityVO;
import com.testplatform.dto.dashboard.EnvStatusVO;
import com.testplatform.dto.dashboard.OverviewVO;
import com.testplatform.dto.dashboard.TrendVO;

import java.util.List;

/**
 * 工作台服务（REQ-007）
 */
public interface DashboardService {

    /** 概览统计 */
    OverviewVO overview();

    /** 近 7 日趋势 */
    TrendVO trend();

    /** 环境状态列表 */
    List<EnvStatusVO> envStatus();

    /** 最近操作动态（默认 10 条） */
    List<ActivityVO> activities();
}
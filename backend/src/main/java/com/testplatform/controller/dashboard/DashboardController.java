package com.testplatform.controller.dashboard;

import com.testplatform.common.Result;
import com.testplatform.dto.dashboard.ActivityVO;
import com.testplatform.dto.dashboard.EnvStatusVO;
import com.testplatform.dto.dashboard.OverviewVO;
import com.testplatform.dto.dashboard.TrendVO;
import com.testplatform.security.RequirePermission;
import com.testplatform.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台（REQ-007）
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 概览统计 */
    @GetMapping("/overview")
    @RequirePermission("dashboard:view")
    public Result<OverviewVO> overview() {
        return Result.ok(dashboardService.overview());
    }

    /** 近 7 日趋势 */
    @GetMapping("/trend")
    @RequirePermission("dashboard:view")
    public Result<TrendVO> trend() {
        return Result.ok(dashboardService.trend());
    }

    /** 环境状态列表 */
    @GetMapping("/env-status")
    @RequirePermission("dashboard:view")
    public Result<List<EnvStatusVO>> envStatus() {
        return Result.ok(dashboardService.envStatus());
    }

    /** 最近操作动态 */
    @GetMapping("/activities")
    @RequirePermission("dashboard:view")
    public Result<List<ActivityVO>> activities() {
        return Result.ok(dashboardService.activities());
    }
}
package com.testplatform.service.dashboard.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.dto.dashboard.ActivityVO;
import com.testplatform.dto.dashboard.EnvStatusVO;
import com.testplatform.dto.dashboard.OverviewVO;
import com.testplatform.dto.dashboard.TrendVO;
import com.testplatform.entity.SysOperationLog;
import com.testplatform.entity.SysUser;
import com.testplatform.entity.TcCase;
import com.testplatform.entity.TdEnvironment;
import com.testplatform.entity.TdEnvironmentHealth;
import com.testplatform.mapper.SysOperationLogMapper;
import com.testplatform.mapper.SysUserMapper;
import com.testplatform.mapper.TcCaseMapper;
import com.testplatform.mapper.TdEnvironmentHealthMapper;
import com.testplatform.mapper.TdEnvironmentMapper;
import com.testplatform.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台服务实现（REQ-007）
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SysUserMapper sysUserMapper;
    private final TdEnvironmentMapper environmentMapper;
    private final TcCaseMapper caseMapper;
    private final TdEnvironmentHealthMapper healthMapper;
    private final SysOperationLogMapper operationLogMapper;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public OverviewVO overview() {
        OverviewVO vo = new OverviewVO();
        vo.setUserCount(sysUserMapper.selectCount(null));
        vo.setEnvCount(environmentMapper.selectCount(null));
        vo.setCaseCount(caseMapper.selectCount(null));
        // 健康环境数：有最近一次检查记录且状态为 UP 的环境数
        List<TdEnvironmentHealth> latest = latestHealthList();
        long up = latest.stream().filter(h -> "UP".equals(h.getStatus())).count();
        vo.setHealthUpCount(up);
        return vo;
    }

    @Override
    public TrendVO trend() {
        TrendVO vo = new TrendVO();
        List<String> dates = new ArrayList<>();
        List<Long> caseCounts = new ArrayList<>();
        List<Long> healthChecks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(6).atStartOfDay();
        // 取 7 日内创建的用户/用例/健康记录（createdAt 字段）
        Map<LocalDate, Long> caseMap = countByDay(caseMapper.selectList(
                new LambdaQueryWrapper<TcCase>().ge(TcCase::getCreatedAt, start)), "createdAt");
        Map<LocalDate, Long> healthMap = countByDay(healthMapper.selectList(
                new LambdaQueryWrapper<TdEnvironmentHealth>().ge(TdEnvironmentHealth::getCheckedAt, start)), "checkedAt");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dates.add(d.format(DAY));
            caseCounts.add(caseMap.getOrDefault(d, 0L));
            healthChecks.add(healthMap.getOrDefault(d, 0L));
        }
        vo.setDates(dates);
        vo.setCaseCounts(caseCounts);
        vo.setHealthChecks(healthChecks);
        return vo;
    }

    @Override
    public List<EnvStatusVO> envStatus() {
        List<TdEnvironment> envs = environmentMapper.selectList(null);
        List<TdEnvironmentHealth> latest = latestHealthList();
        Map<Long, TdEnvironmentHealth> latestByEnv = new HashMap<>();
        for (TdEnvironmentHealth h : latest) {
            latestByEnv.putIfAbsent(h.getEnvId(), h);
        }
        List<EnvStatusVO> result = new ArrayList<>();
        for (TdEnvironment env : envs) {
            EnvStatusVO vo = new EnvStatusVO();
            vo.setId(env.getId());
            vo.setEnvCode(env.getEnvCode());
            vo.setEnvName(env.getEnvName());
            // 表无 env_type 列，按 env_code 前缀推断类型
            String code = env.getEnvCode() == null ? "" : env.getEnvCode().toLowerCase();
            if (code.startsWith("dev")) {
                vo.setEnvType("DEV");
            } else if (code.startsWith("test") || code.startsWith("qa")) {
                vo.setEnvType("TEST");
            } else if (code.startsWith("prod")) {
                vo.setEnvType("PROD");
            } else {
                vo.setEnvType("OTHER");
            }
            vo.setBaseUrl(env.getBaseUrl());
            vo.setStatus(env.getStatus());
            TdEnvironmentHealth h = latestByEnv.get(env.getId());
            if (h == null) {
                vo.setHealthStatus("未检测");
                vo.setLatencyMs(null);
            } else {
                vo.setHealthStatus(h.getStatus());
                vo.setLatencyMs(h.getLatencyMs() == null ? null : h.getLatencyMs().longValue());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ActivityVO> activities() {
        List<SysOperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<SysOperationLog>()
                        .orderByDesc(SysOperationLog::getCreatedAt)
                        .last("LIMIT 10"));
        List<ActivityVO> result = new ArrayList<>();
        for (SysOperationLog log : logs) {
            ActivityVO vo = new ActivityVO();
            vo.setUsername(log.getUsername());
            vo.setModule(log.getModule());
            vo.setOperation(log.getOperation());
            vo.setResult(log.getResult() != null && log.getResult() == 1 ? "success" : "fail");
            vo.setCreatedAt(log.getCreatedAt());
            result.add(vo);
        }
        return result;
    }

    /** 每环境最近一次健康检查（按 envId 分组取 checkedAt 最新） */
    private List<TdEnvironmentHealth> latestHealthList() {
        List<TdEnvironmentHealth> all = healthMapper.selectList(
                new LambdaQueryWrapper<TdEnvironmentHealth>().orderByDesc(TdEnvironmentHealth::getCheckedAt));
        Map<Long, TdEnvironmentHealth> latest = new HashMap<>();
        for (TdEnvironmentHealth h : all) {
            latest.putIfAbsent(h.getEnvId(), h);
        }
        return new ArrayList<>(latest.values());
    }

    /** 按日期统计实体创建数（field 区分 createdAt / checkedAt） */
    private Map<LocalDate, Long> countByDay(List<?> entities, String field) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (Object e : entities) {
            LocalDateTime t = null;
            if ("createdAt".equals(field) && e instanceof TcCase c) {
                t = c.getCreatedAt();
            } else if ("checkedAt".equals(field) && e instanceof TdEnvironmentHealth h) {
                t = h.getCheckedAt();
            }
            if (t != null) {
                map.merge(t.toLocalDate(), 1L, Long::sum);
            }
        }
        return map;
    }
}
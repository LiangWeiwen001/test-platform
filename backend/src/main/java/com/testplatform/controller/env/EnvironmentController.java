package com.testplatform.controller.env;

import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.env.EnvironmentCreateRequest;
import com.testplatform.dto.env.EnvironmentQueryRequest;
import com.testplatform.dto.env.EnvironmentUpdateRequest;
import com.testplatform.dto.env.EnvironmentVO;
import com.testplatform.dto.env.HealthRecordVO;
import com.testplatform.security.RequirePermission;
import com.testplatform.service.env.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 环境管理接口（REQ-005）
 */
@RestController
@RequestMapping("/api/v1/envs")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    /**
     * 环境分页列表（支持关键字筛选）
     */
    @GetMapping
    @RequirePermission("env:env:list")
    public Result<PageResult<EnvironmentVO>> page(EnvironmentQueryRequest query) {
        return Result.ok(environmentService.page(query));
    }

    /**
     * 全部启用环境（下拉用）
     */
    @GetMapping("/all")
    @RequirePermission("env:env:list")
    public Result<List<EnvironmentVO>> listAll() {
        return Result.ok(environmentService.listAllEnabled());
    }

    /**
     * 新建环境（env_code 唯一冲突 40900）
     */
    @PostMapping
    @RequirePermission("env:env:add")
    public Result<EnvironmentVO> create(@Valid @RequestBody EnvironmentCreateRequest request) {
        return Result.ok(environmentService.create(request));
    }

    /**
     * 编辑环境
     */
    @PutMapping("/{id}")
    @RequirePermission("env:env:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EnvironmentUpdateRequest request) {
        environmentService.update(id, request);
        return Result.ok();
    }

    /**
     * 删除环境（逻辑删除 + 保留健康记录）
     */
    @DeleteMapping("/{id}")
    @RequirePermission("env:env:delete")
    public Result<Void> delete(@PathVariable Long id) {
        environmentService.delete(id);
        return Result.ok();
    }

    /**
     * 启用/禁用环境
     */
    @PutMapping("/{id}/status")
    @RequirePermission("env:env:edit")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        environmentService.updateStatus(id, status);
        return Result.ok();
    }

    /**
     * 手动触发健康自检（HTTP 探测 base_url）
     */
    @PostMapping("/{id}/health-check")
    @RequirePermission("env:env:health")
    public Result<HealthRecordVO> healthCheck(@PathVariable Long id) {
        return Result.ok(environmentService.healthCheck(id));
    }

    /**
     * 健康检查记录（最近 10 条）
     */
    @GetMapping("/{id}/health-records")
    @RequirePermission("env:env:health")
    public Result<List<HealthRecordVO>> healthRecords(@PathVariable Long id) {
        return Result.ok(environmentService.healthRecords(id));
    }
}
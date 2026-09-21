package com.testplatform.controller.testcase;

import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.testcase.*;
import com.testplatform.security.RequirePermission;
import com.testplatform.service.testcase.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用例管理接口（REQ-006）
 */
@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    /** 分页查询用例 */
    @GetMapping
    @RequirePermission("case:case:list")
    public Result<PageResult<CaseVO>> page(CaseQueryRequest request) {
        return Result.ok(caseService.page(request));
    }

    /** 用例详情 */
    @GetMapping("/{id}")
    @RequirePermission("case:case:list")
    public Result<CaseVO> detail(@PathVariable Long id) {
        return Result.ok(caseService.detail(id));
    }

    /** 新建用例 */
    @PostMapping
    @RequirePermission("case:case:add")
    public Result<Long> create(@Valid @RequestBody CaseCreateRequest request) {
        return Result.ok(caseService.create(request));
    }

    /** 编辑用例 */
    @PutMapping("/{id}")
    @RequirePermission("case:case:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CaseUpdateRequest request) {
        caseService.update(id, request);
        return Result.ok();
    }

    /** 删除用例 */
    @DeleteMapping("/{id}")
    @RequirePermission("case:case:delete")
    public Result<Void> delete(@PathVariable Long id) {
        caseService.delete(id);
        return Result.ok();
    }

    /** 启用/禁用用例 */
    @PutMapping("/{id}/status")
    @RequirePermission("case:case:edit")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        caseService.updateStatus(id, status);
        return Result.ok();
    }

    /** 标签列表（含关联用例数） */
    @GetMapping("/tags")
    @RequirePermission("case:tag:list")
    public Result<List<CaseTagVO>> listTags() {
        return Result.ok(caseService.listTags());
    }

    /** 新建标签 */
    @PostMapping("/tags")
    @RequirePermission("case:tag:add")
    public Result<Long> createTag(@Valid @RequestBody CaseTagCreateRequest request) {
        return Result.ok(caseService.createTag(request));
    }

    /** 删除标签 */
    @DeleteMapping("/tags/{id}")
    @RequirePermission("case:tag:delete")
    public Result<Void> deleteTag(@PathVariable Long id) {
        caseService.deleteTag(id);
        return Result.ok();
    }
}
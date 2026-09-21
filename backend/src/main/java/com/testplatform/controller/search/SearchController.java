package com.testplatform.controller.search;

import com.testplatform.common.Result;
import com.testplatform.dto.search.SearchResultVO;
import com.testplatform.security.RequirePermission;
import com.testplatform.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局搜索接口（REQ-008）
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 跨用例/环境/用户全局搜索
     */
    @GetMapping
    @RequirePermission("search:global")
    public Result<SearchResultVO> search(@RequestParam(required = false) String keyword) {
        return Result.ok(searchService.search(keyword));
    }
}
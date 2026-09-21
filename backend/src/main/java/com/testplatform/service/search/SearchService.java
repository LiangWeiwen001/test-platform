package com.testplatform.service.search;

import com.testplatform.dto.search.SearchResultVO;

/**
 * 全局搜索服务（REQ-008）
 */
public interface SearchService {

    /**
     * 跨用例/环境/用户全局搜索
     *
     * @param keyword 关键字
     * @return 分组搜索结果（每类最多 10 条）
     */
    SearchResultVO search(String keyword);
}
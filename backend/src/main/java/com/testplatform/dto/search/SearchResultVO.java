package com.testplatform.dto.search;

import lombok.Data;

import java.util.List;

/**
 * 全局搜索结果（REQ-008，跨用例/环境/用户）
 */
@Data
public class SearchResultVO {

    /** 搜索关键字 */
    private String keyword;

    /** 命中的用例 */
    private List<CaseHitVO> cases;

    /** 命中的环境 */
    private List<EnvHitVO> envs;

    /** 命中的用户 */
    private List<UserHitVO> users;
}
package com.testplatform.dto.search;

import lombok.Data;

/**
 * 全局搜索结果项-用户（REQ-008）
 */
@Data
public class UserHitVO {

    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 命中的字段名 */
    private String matchField;
}
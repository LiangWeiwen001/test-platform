package com.testplatform.service.testcase;

import com.testplatform.common.PageResult;
import com.testplatform.dto.testcase.*;

import java.util.List;

/**
 * 用例管理服务（REQ-006）
 */
public interface CaseService {

    /** 分页查询用例 */
    PageResult<CaseVO> page(CaseQueryRequest request);

    /** 用例详情 */
    CaseVO detail(Long id);

    /** 新建用例 */
    Long create(CaseCreateRequest request);

    /** 编辑用例（版本递增） */
    void update(Long id, CaseUpdateRequest request);

    /** 删除用例（逻辑删除） */
    void delete(Long id);

    /** 启用/禁用用例 */
    void updateStatus(Long id, Integer status);

    /** 标签列表（含关联用例数） */
    List<CaseTagVO> listTags();

    /** 新建标签 */
    Long createTag(CaseTagCreateRequest request);

    /** 删除标签（同时清理用例中的该 tagId） */
    void deleteTag(Long id);
}
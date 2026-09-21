package com.testplatform.service.env;

import com.testplatform.common.PageResult;
import com.testplatform.dto.env.EnvironmentCreateRequest;
import com.testplatform.dto.env.EnvironmentQueryRequest;
import com.testplatform.dto.env.EnvironmentUpdateRequest;
import com.testplatform.dto.env.EnvironmentVO;
import com.testplatform.dto.env.HealthRecordVO;

import java.util.List;

/**
 * 环境管理服务（REQ-005）
 */
public interface EnvironmentService {

    /**
     * 环境分页列表（关键字模糊匹配 env_name / env_code / description）
     */
    PageResult<EnvironmentVO> page(EnvironmentQueryRequest query);

    /**
     * 全部启用环境（下拉用）
     */
    List<EnvironmentVO> listAllEnabled();

    /**
     * 新建环境（env_code 唯一冲突 40900）
     */
    EnvironmentVO create(EnvironmentCreateRequest request);

    /**
     * 编辑环境
     */
    void update(Long id, EnvironmentUpdateRequest request);

    /**
     * 删除环境（逻辑删除，保留健康记录）
     */
    void delete(Long id);

    /**
     * 启用/禁用环境
     */
    void updateStatus(Long id, Integer status);

    /**
     * 健康自检：HTTP GET base_url（超时 3s），2xx/3xx 记 UP，失败记 DOWN，写入健康记录表
     */
    HealthRecordVO healthCheck(Long id);

    /**
     * 最近 10 条健康检查记录
     */
    List<HealthRecordVO> healthRecords(Long id);
}
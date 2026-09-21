/**
 * 用例管理接口（见 DESIGN_接口设计.md 第 2.6 节）
 */
import request from './request'

/**
 * 用例分页列表（支持关键字/类型/级别/标签/状态筛选）
 * GET /api/v1/cases
 * @param {{page?: number, size?: number, keyword?: string, caseType?: string, caseLevel?: string, tagId?: number, status?: number}} params
 */
export function listCases(params) {
  return request({
    url: '/cases',
    method: 'get',
    params,
  })
}

/**
 * 用例详情
 * GET /api/v1/cases/:id
 */
export function getCase(id) {
  return request({
    url: `/cases/${id}`,
    method: 'get',
  })
}

/**
 * 新建用例
 * POST /api/v1/cases
 */
export function createCase(data) {
  return request({
    url: '/cases',
    method: 'post',
    data,
  })
}

/**
 * 编辑用例（编辑后版本自动递增）
 * PUT /api/v1/cases/:id
 */
export function updateCase(id, data) {
  return request({
    url: `/cases/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除用例（逻辑删除）
 * DELETE /api/v1/cases/:id
 */
export function deleteCase(id) {
  return request({
    url: `/cases/${id}`,
    method: 'delete',
  })
}

/**
 * 启用/禁用用例
 * PUT /api/v1/cases/:id/status?status=0|1
 */
export function updateCaseStatus(id, status) {
  return request({
    url: `/cases/${id}/status`,
    method: 'put',
    params: { status },
  })
}

/**
 * 标签列表（含关联用例数）
 * GET /api/v1/cases/tags
 */
export function listCaseTags() {
  return request({
    url: '/cases/tags',
    method: 'get',
  })
}

/**
 * 新建标签
 * POST /api/v1/cases/tags
 */
export function createCaseTag(data) {
  return request({
    url: '/cases/tags',
    method: 'post',
    data,
  })
}

/**
 * 删除标签（同时解除用例关联）
 * DELETE /api/v1/cases/tags/:id
 */
export function deleteCaseTag(id) {
  return request({
    url: `/cases/tags/${id}`,
    method: 'delete',
  })
}
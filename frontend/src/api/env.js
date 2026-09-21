/**
 * 环境管理接口（见 DESIGN_接口设计.md 第 2.5 节）
 */
import request from './request'

/**
 * 环境分页列表（支持关键字筛选）
 * GET /api/v1/envs?page=1&size=10&keyword=
 * @param {{page?: number, size?: number, keyword?: string}} params
 */
export function listEnvs(params) {
  return request({
    url: '/envs',
    method: 'get',
    params,
  })
}

/**
 * 全部启用环境（下拉用）
 * GET /api/v1/envs/all
 */
export function listAllEnvs() {
  return request({
    url: '/envs/all',
    method: 'get',
  })
}

/**
 * 新建环境
 * POST /api/v1/envs
 * @param {{envCode: string, envName: string, baseUrl?: string, description?: string, status?: number}} data
 */
export function createEnv(data) {
  return request({
    url: '/envs',
    method: 'post',
    data,
  })
}

/**
 * 编辑环境
 * PUT /api/v1/envs/:id
 */
export function updateEnv(id, data) {
  return request({
    url: `/envs/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除环境（逻辑删除 + 保留健康记录）
 * DELETE /api/v1/envs/:id
 */
export function deleteEnv(id) {
  return request({
    url: `/envs/${id}`,
    method: 'delete',
  })
}

/**
 * 启用/禁用环境
 * PUT /api/v1/envs/:id/status?status=0|1
 */
export function updateEnvStatus(id, status) {
  return request({
    url: `/envs/${id}/status`,
    method: 'put',
    params: { status },
  })
}

/**
 * 手动触发健康自检（HTTP 探测 base_url）
 * POST /api/v1/envs/:id/health-check
 */
export function healthCheck(id) {
  return request({
    url: `/envs/${id}/health-check`,
    method: 'post',
  })
}

/**
 * 健康检查记录（最近 10 条）
 * GET /api/v1/envs/:id/health-records
 */
export function getHealthRecords(id) {
  return request({
    url: `/envs/${id}/health-records`,
    method: 'get',
  })
}
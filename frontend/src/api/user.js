/**
 * 用户管理接口（见 DESIGN_接口设计.md 第 2.2 节）
 */
import request from './request'

/**
 * 用户分页列表（支持关键字/状态筛选）
 * GET /api/v1/system/users?page=1&size=10&keyword=&status=
 * @param {{page?: number, size?: number, keyword?: string, status?: number}} params
 */
export function listUsers(params) {
  return request({
    url: '/system/users',
    method: 'get',
    params,
  })
}

/**
 * 新建用户（含角色绑定）
 * POST /api/v1/system/users
 * @param {{username: string, nickname: string, password: string, email?: string, phone?: string, status?: number, roleIds?: number[]}} data
 */
export function createUser(data) {
  return request({
    url: '/system/users',
    method: 'post',
    data,
  })
}

/**
 * 编辑用户（含角色调整）
 * PUT /api/v1/system/users/:id
 */
export function updateUser(id, data) {
  return request({
    url: `/system/users/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除用户（逻辑删除 + 清理角色绑定）
 * DELETE /api/v1/system/users/:id
 */
export function deleteUser(id) {
  return request({
    url: `/system/users/${id}`,
    method: 'delete',
  })
}

/**
 * 启用/禁用用户
 * PUT /api/v1/system/users/:id/status?status=0|1
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/system/users/${id}/status`,
    method: 'put',
    params: { status },
  })
}

/**
 * 重置密码
 * PUT /api/v1/system/users/:id/password
 * @param {number} id 用户 ID
 * @param {string} password 新密码（明文）
 */
export function resetPassword(id, password) {
  return request({
    url: `/system/users/${id}/password`,
    method: 'put',
    data: { password },
  })
}

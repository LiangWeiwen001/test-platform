/**
 * 角色管理接口
 */
import request from './request'

/**
 * 分页查询角色
 * GET /api/v1/system/roles?page=1&size=10&keyword=&status=
 */
export function listRoles(params) {
  return request({ url: '/system/roles', method: 'get', params })
}

/**
 * 所有启用角色（下拉用）
 * GET /api/v1/system/roles/all
 */
export function getAllRoles() {
  return request({ url: '/system/roles/all', method: 'get' })
}

/**
 * 新建角色
 * POST /api/v1/system/roles
 */
export function createRole(data) {
  return request({ url: '/system/roles', method: 'post', data })
}

/**
 * 编辑角色
 * PUT /api/v1/system/roles/:id
 */
export function updateRole(id, data) {
  return request({ url: `/system/roles/${id}`, method: 'put', data })
}

/**
 * 删除角色
 * DELETE /api/v1/system/roles/:id
 */
export function deleteRole(id) {
  return request({ url: `/system/roles/${id}`, method: 'delete' })
}

/**
 * 权限树
 * GET /api/v1/system/permissions/tree
 */
export function getPermissionTree() {
  return request({ url: '/system/permissions/tree', method: 'get' })
}

/**
 * 分配角色权限
 * PUT /api/v1/system/roles/:id/permissions
 */
export function assignPermissions(id, permIds) {
  return request({ url: `/system/roles/${id}/permissions`, method: 'put', data: permIds })
}

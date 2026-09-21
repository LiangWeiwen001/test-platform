/**
 * 认证相关接口（见 DESIGN_接口设计.md 第 2.1 节）
 */
import request from './request'

/**
 * 登录（签发 access + refresh token）
 * POST /api/v1/auth/login
 * @param {{username: string, password: string}} data
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

/**
 * 登出（作废当前 token）
 * POST /api/v1/auth/logout
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}

/**
 * 刷新 access token
 * POST /api/v1/auth/refresh
 * @param {{refreshToken: string}} data
 */
export function refreshToken(data) {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data,
  })
}

/**
 * 获取当前登录用户信息（含角色与权限码集合）
 * GET /api/v1/auth/me
 */
export function getCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'get',
  })
}

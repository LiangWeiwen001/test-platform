/**
 * 工作台接口（见 DESIGN_接口设计.md 第 2.8 节）
 */
import request from './request'

/**
 * 概览统计（用户/环境/用例/健康环境数）
 * GET /api/v1/dashboard/overview
 */
export function getOverview() {
  return request({
    url: '/dashboard/overview',
    method: 'get',
  })
}

/**
 * 近 7 日趋势（新增用例/健康检查）
 * GET /api/v1/dashboard/trend
 */
export function getTrend() {
  return request({
    url: '/dashboard/trend',
    method: 'get',
  })
}

/**
 * 环境状态列表（含最近健康状态）
 * GET /api/v1/dashboard/env-status
 */
export function getEnvStatus() {
  return request({
    url: '/dashboard/env-status',
    method: 'get',
  })
}

/**
 * 操作动态（最近 10 条）
 * GET /api/v1/dashboard/activities
 */
export function getActivities() {
  return request({
    url: '/dashboard/activities',
    method: 'get',
  })
}
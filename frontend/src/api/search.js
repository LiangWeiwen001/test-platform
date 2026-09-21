// 全局搜索 API（REQ-008，search:global）
import request from './request'

/**
 * 跨用例/环境/用户搜索
 * @param {string} keyword 关键字
 * @returns {Promise} {keyword, cases:[], envs:[], users:[]}
 */
export function search(keyword) {
  return request.get('/search', { params: { keyword } })
}
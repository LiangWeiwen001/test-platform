/**
 * 路由守卫（骨架）
 *
 * 当前实现：仅做登录态校验
 *   - 无 token 访问需登录页面 → 跳转 /login（带 redirect 参数）
 *   - 白名单页面（/login、403、404）直接放行
 *
 * 待 TASK-004 补充（见 DESIGN_系统架构.md 4.3）：
 *   1. 有 token 但未加载用户信息 → 调 GET /api/v1/auth/me 拉取用户与权限码
 *   2. 按权限码过滤并动态注册路由
 *   3. 校验目标路由权限，无权限跳 403
 */
import { getToken } from '@/utils/auth'

/** 免登录白名单 */
const WHITE_LIST = ['/login', '/403', '/404']

/** 全局标题后缀 */
const APP_TITLE = '测试平台'

/**
 * 注册路由守卫
 * @param {import('vue-router').Router} router
 */
export function setupRouterGuard(router) {
  router.beforeEach((to, from, next) => {
    // 页面标题
    document.title = to.meta && to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

    // 白名单或显式免登录页面直接放行
    if (WHITE_LIST.includes(to.path) || to.meta.requiresAuth === false) {
      return next()
    }

    // 无 token → 跳登录页
    if (!getToken()) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }

    next()
  })
}

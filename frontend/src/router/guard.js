/**
 * 路由守卫
 *
 * 逻辑（见 DESIGN_系统架构.md 4.3）：
 *   1. 白名单（/login、/403、/404）直接放行；已登录访问 /login 则跳工作台
 *   2. 无 accessToken 访问受保护页面 → 跳 /login（带 redirect）
 *   3. 有 token 但未加载用户信息 → 调 GET /api/v1/auth/me 拉取 user（含 roles/perms）
 *   4. 按目标路由 meta.perm 校验权限码，无权限跳 403
 *   5. fetchMe 失败（token 失效）→ 清 token 跳 /login
 */
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'

/** 免登录白名单 */
const WHITE_LIST = ['/login', '/403', '/404']

/** 全局标题后缀 */
const APP_TITLE = '测试平台'

/**
 * 注册路由守卫
 * @param {import('vue-router').Router} router
 */
export function setupRouterGuard(router) {
  router.beforeEach(async (to, from, next) => {
    // 页面标题
    document.title = to.meta && to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

    const token = getToken()

    // 白名单或显式免登录页面
    if (WHITE_LIST.includes(to.path) || to.meta.requiresAuth === false) {
      // 已登录再访问登录页 → 跳工作台
      if (to.path === '/login' && token) {
        return next('/dashboard')
      }
      return next()
    }

    // 无 token → 跳登录页
    if (!token) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }

    const userStore = useUserStore()

    // 有 token 但用户信息未加载（如刷新页面）→ 拉取用户与权限
    if (!userStore.hasUser) {
      try {
        await userStore.fetchMe()
      } catch (e) {
        // token 失效：清空登录态回登录页（拦截器已提示）
        userStore.reset()
        return next({ path: '/login', query: { redirect: to.fullPath } })
      }
    }

    // 权限校验：无对应权限码跳 403
    if (to.meta && to.meta.perm && !userStore.perms.includes(to.meta.perm)) {
      return next('/403')
    }

    next()
  })
}

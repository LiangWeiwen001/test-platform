/**
 * 路由表
 * - 静态路由：登录 / 主布局（含菜单占位子路由）/ 错误页
 * - meta.perm：访问该路由所需权限码，由 guard.js 校验（无权限跳 403）
 */
import { createRouter, createWebHistory } from 'vue-router'

/** 静态路由表 */
export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    // 主布局：菜单子路由（页面内容后续任务实现）
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', perm: 'dashboard:view' },
      },
      {
        // 工具菜单（REQ-004）：/tool 重定向到首个工具，子路由 /tool/:key 由注册表驱动
        path: 'tool',
        name: 'Tool',
        redirect: '/tool/json',
        meta: { title: '工具菜单', perm: 'tool:view' },
        children: [
          {
            path: ':key',
            name: 'ToolPage',
            component: () => import('@/views/tool/ToolPage.vue'),
            meta: { title: '工具', perm: 'tool:view' },
          },
        ],
      },
      {
        path: 'env',
        name: 'Environment',
        component: () => import('@/views/env/index.vue'),
        meta: { title: '环境管理', perm: 'env:env:list' },
      },
      {
        path: 'case',
        name: 'Case',
        component: () => import('@/views/case/index.vue'),
        meta: { title: '用例管理', perm: 'case:case:list' },
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', perm: 'system:user:list' },
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色权限', perm: 'system:role:list' },
      },
      {
        path: 'system/log',
        name: 'OperationLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志', perm: 'system:log:list' },
      },
    ],
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', requiresAuth: false },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false },
  },
  {
    // 兜底：未匹配路由 → 404
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router

/**
 * 路由表
 * - 静态路由：登录 / 主布局 / 错误页
 * - 动态路由：按权限码注册由 TASK-004 实现（见 DESIGN_系统架构.md 4.3）
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
    // 主布局：children 由 TASK-004 按权限动态注册
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/login',
    meta: { requiresAuth: true },
    children: [],
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

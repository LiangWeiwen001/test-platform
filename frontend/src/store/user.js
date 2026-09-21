/**
 * 用户态 store
 * 职责：token / 用户信息 / 权限码集合 / 角色（动态路由由 TASK-004 接入）
 */
import { defineStore } from 'pinia'
import { getToken, setToken as saveToken, clearToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 初始化时从 localStorage 恢复 token，刷新页面不丢登录态
    token: getToken() || '',
    userInfo: null, // { id, username, nickname, avatar ... }
    roles: [], // 角色编码集合，如 ['admin']
    permissions: [], // 权限码集合，如 ['system:user:add']
  }),

  getters: {
    /** 是否已登录 */
    isLogin: (state) => !!state.token,
  },

  actions: {
    /** 保存 token（内存 + localStorage） */
    setToken(token) {
      this.token = token || ''
      saveToken(token)
    },

    /** 保存用户信息与权限 */
    setUserInfo(info) {
      this.userInfo = info || null
      this.roles = (info && info.roles) || []
      this.permissions = (info && info.permissions) || []
    },

    /** 清空登录态 */
    reset() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.permissions = []
      clearToken()
    },

    /** 退出登录 */
    logout() {
      this.reset()
    },
  },
})

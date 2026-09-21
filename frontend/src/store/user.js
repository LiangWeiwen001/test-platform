/**
 * 用户态 store
 * 职责：token / 用户信息 / 权限码集合（登录、拉取当前用户、登出）
 *
 * 数据结构对齐后端 UserInfoDTO：
 *   user = { id, username, nickname, avatar, roles: string[], perms: string[] }
 */
import { defineStore } from 'pinia'
import {
  getToken,
  setToken as saveToken,
  setRefreshToken as saveRefreshToken,
  clearToken,
} from '@/utils/auth'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 初始化时从 localStorage 恢复 token，刷新页面不丢登录态
    token: getToken() || '',
    user: null, // { id, username, nickname, avatar, roles, perms }
    perms: [], // 权限码集合，取自 user.perms
  }),

  getters: {
    /** 是否已登录（有 token） */
    isLogin: (state) => !!state.token,
    /** 角色编码集合 */
    roles: (state) => (state.user && state.user.roles) || [],
    /** 是否已加载用户信息 */
    hasUser: (state) => !!state.user,
    /** 昵称（顶栏展示） */
    nickname: (state) => (state.user && (state.user.nickname || state.user.username)) || '',
  },

  actions: {
    /** 写入用户信息（含 roles / perms） */
    setUser(user) {
      this.user = user || null
      this.perms = (user && user.perms) || []
    },

    /** 登录：保存 token + 用户信息 */
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.accessToken
      saveToken(data.accessToken)
      if (data.refreshToken) saveRefreshToken(data.refreshToken)
      this.setUser(data.user)
      return data
    },

    /** 拉取当前用户信息（刷新页面后恢复用户态与菜单） */
    async fetchMe() {
      const user = await getCurrentUser()
      this.setUser(user)
      return user
    },

    /** 清空登录态（内存 + localStorage） */
    reset() {
      this.token = ''
      this.user = null
      this.perms = []
      clearToken()
    },

    /** 退出登录：通知后端（忽略失败）+ 清态 + 跳登录页 */
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 无状态 JWT，后端失败不影响本地登出
      }
      this.reset()
      router.push('/login')
    },
  },
})

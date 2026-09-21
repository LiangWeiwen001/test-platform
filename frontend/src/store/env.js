/**
 * 当前环境 store
 * 职责：全局环境切换（REQ-005 ENV-04），联动各模块（环境管理 / 用例 / 工作台）
 *
 * 骨架说明：环境列表与默认环境由 TASK 后续接入 /api/v1/env 接口后填充。
 */
import { defineStore } from 'pinia'

const CURRENT_ENV_KEY = 'currentEnvId'

export const useEnvStore = defineStore('env', {
  state: () => ({
    // 当前选中的环境 ID（从 localStorage 恢复）
    currentEnvId: localStorage.getItem(CURRENT_ENV_KEY) || '',
    // 环境列表缓存
    envList: [],
  }),

  getters: {
    /** 当前环境对象 */
    currentEnv: (state) =>
      state.envList.find((item) => String(item.id) === String(state.currentEnvId)) || null,
  },

  actions: {
    /** 切换当前环境 */
    setCurrentEnv(envId) {
      this.currentEnvId = envId || ''
      if (envId) {
        localStorage.setItem(CURRENT_ENV_KEY, envId)
      } else {
        localStorage.removeItem(CURRENT_ENV_KEY)
      }
    },

    /** 缓存环境列表 */
    setEnvList(list) {
      this.envList = list || []
    },
  },
})

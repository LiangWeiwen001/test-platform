/**
 * axios 实例封装
 * - baseURL：/api/v1（开发期经 Vite 代理到后端 8080）
 * - 请求拦截器：注入 Authorization: Bearer <accessToken>
 * - 响应拦截器：统一处理 {code, message, data}；401 跳转登录页
 *
 * 骨架说明：refresh token 自动续期逻辑由 TASK-004 补充。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from '@/utils/auth'

const service = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// 请求拦截器：注入 token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 后端约定的业务成功码（0 或 200 均视为成功，兼容 M1 约定）
const SUCCESS_CODES = [0, 200]

// 响应拦截器：统一处理响应体
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准响应（如二进制流）直接返回
    if (res == null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (SUCCESS_CODES.includes(res.code)) {
      return res.data
    }
    // 401：清空登录态并跳转登录页
    if (res.code === 401) {
      clearToken()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
      return Promise.reject(new Error(res.message || '未登录'))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // HTTP 层 401
    if (error.response && error.response.status === 401) {
      clearToken()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  },
)

export default service

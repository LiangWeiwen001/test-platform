/**
 * axios 实例封装
 * - baseURL：/api/v1（开发期经 Vite 代理到后端 8080）
 * - 请求拦截器：注入 Authorization: Bearer <accessToken>
 * - 响应拦截器：统一处理 {code, message, data}；40100/40101 自动刷新 token 并重放原请求
 *   刷新失败才清空登录态跳转登录页
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  getToken,
  clearToken,
  getRefreshToken,
  setToken,
  setRefreshToken,
} from '@/utils/auth'

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

/** 业务成功码（见 接口设计 1.2：0=成功） */
const SUCCESS_CODE = 0
/** 需要刷新 token 的错误码：40100 未认证 / 40101 token 过期 */
const REFRESH_CODES = [40100, 40101]

/** 单飞刷新，避免并发请求重复刷新 */
let refreshing = null

/** 清空登录态并跳转登录页 */
function redirectToLogin() {
  clearToken()
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

/**
 * 用 refresh token 换取新 access token（独立的 axios 调用，不走本实例拦截器）
 * @returns {Promise<void>}
 */
function refreshAccessToken() {
  const rt = getRefreshToken()
  if (!rt) return Promise.reject(new Error('无 refresh token'))
  return axios
    .post('/api/v1/auth/refresh', { refreshToken: rt })
    .then((resp) => {
      const body = resp.data
      if (!body || body.code !== SUCCESS_CODE || !body.data) {
        throw new Error((body && body.message) || '刷新 token 失败')
      }
      setToken(body.data.accessToken)
      if (body.data.refreshToken) setRefreshToken(body.data.refreshToken)
    })
}

/**
 * 刷新后重放原请求
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @param {string} [message] 401 时的后端提示
 */
async function retryAfterRefresh(config, message) {
  if (config._retry) {
    redirectToLogin()
    ElMessage.error('登录已过期，请重新登录')
    return Promise.reject(new Error(message || '登录已过期'))
  }
  config._retry = true
  try {
    if (!refreshing) {
      refreshing = refreshAccessToken().finally(() => {
        refreshing = null
      })
    }
    await refreshing
    // 重放：请求拦截器会读取刷新后的新 token 重新注入
    return service(config)
  } catch (e) {
    redirectToLogin()
    ElMessage.error('登录已过期，请重新登录')
    return Promise.reject(e)
  }
}

// 响应拦截器：统一处理响应体
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准响应（如二进制流）直接返回
    if (res == null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code === SUCCESS_CODE) {
      return res.data
    }
    // token 无效 / 过期 → 刷新并重放
    if (REFRESH_CODES.includes(res.code)) {
      return retryAfterRefresh(response.config, res.message)
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const resp = error.response
    const code = resp && resp.data && resp.data.code
    // HTTP 401：仅 40100/40101 可刷新；40102（登录失败）等直接提示
    if (resp && resp.status === 401 && REFRESH_CODES.includes(code)) {
      return retryAfterRefresh(error.config, resp.data.message)
    }
    ElMessage.error((resp && resp.data && resp.data.message) || error.message || '网络异常')
    return Promise.reject(error)
  },
)

export default service

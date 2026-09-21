/**
 * token 存取工具
 * 存储位置：localStorage
 * key 约定：accessToken / refreshToken（与后端 JWT 方案一致）
 */
const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'

/** 读取 access token */
export function getToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

/** 写入 access token */
export function setToken(token) {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
  }
}

/** 移除 access token */
export function removeToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

/** 读取 refresh token */
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/** 写入 refresh token */
export function setRefreshToken(token) {
  if (token) {
    localStorage.setItem(REFRESH_TOKEN_KEY, token)
  }
}

/** 移除 refresh token */
export function removeRefreshToken() {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

/** 清空登录态（access + refresh） */
export function clearToken() {
  removeToken()
  removeRefreshToken()
}

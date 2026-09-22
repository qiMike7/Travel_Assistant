/**
 * 登录态与令牌本地存储工具
 * 与后端 JWT 鉴权配合使用：登录后保存 token，请求头携带 Authorization。
 */
const TOKEN_KEY = 'travel_token'
const USER_KEY = 'travel_user'

export function setToken(token) {
	uni.setStorageSync(TOKEN_KEY, token)
}

export function getToken() {
	return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setUser(user) {
	uni.setStorageSync(USER_KEY, user || {})
}

export function getUser() {
	return uni.getStorageSync(USER_KEY) || null
}

export function isLogin() {
	return !!getToken()
}

export function clearAuth() {
	uni.removeStorageSync(TOKEN_KEY)
	uni.removeStorageSync(USER_KEY)
}

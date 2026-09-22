/**
 * 后端接口集中定义
 * 与 Spring Boot 后端的 REST 接口一一对应。
 */
import { http, uploadFile, LLM_TIMEOUT } from '../utils/request.js'

/* ---------------- 认证 ---------------- */
export const authApi = {
	register: (data) => http.post('/api/auth/register', data),
	login: (data) => http.post('/api/auth/login', data)
}

/* ---------------- 用户 / 个人资料 ---------------- */
export const userApi = {
	profile: () => http.get('/api/users/me'),
	updateProfile: (data) => http.put('/api/users/me', data),
	updatePassword: (data) => http.put('/api/users/me/password', data),
	grantVip: (hours = 12) => http.post('/api/users/me/vip?hours=' + hours)
}

/* ---------------- 旅行偏好 ---------------- */
export const preferenceApi = {
	get: () => http.get('/api/preferences'),
	save: (data) => http.put('/api/preferences', data)
}

/* ---------------- 商城商品 ---------------- */
export const productApi = {
	list: (category) => http.get('/api/products', category ? { category } : {}),
	detail: (id) => http.get('/api/products/' + id)
}

/* ---------------- 订单 ---------------- */
export const orderApi = {
	list: () => http.get('/api/orders'),
	create: (data) => http.post('/api/orders', data),
	detail: (id) => http.get('/api/orders/' + id),
	cancel: (id) => http.del('/api/orders/' + id)
}

/* ---------------- 支付 ---------------- */
export const payApi = {
	// 发起微信支付：后端下单并返回调起收银台所需参数
	// 返回体：{ timeStamp, nonceStr, package, signType, paySign, ... }
	wechat: (data) => http.post('/api/payments/wxpay', data)
}

/* ---------------- 旅行相册 ---------------- */
export const albumApi = {
	list: () => http.get('/api/albums'),
	detail: (id) => http.get('/api/albums/' + id),
	create: (data) => http.post('/api/albums', data),
	update: (id, data) => http.put('/api/albums/' + id, data),
	remove: (id) => http.del('/api/albums/' + id)
}

/* ---------------- 地图路线 ---------------- */
export const routeApi = {
	list: () => http.get('/api/routes'),
	detail: (id) => http.get('/api/routes/' + id)
}

/* ---------------- AI 对话（后端代理大模型） ---------------- */
export const chatApi = {
	// 智慧问答：chat.vue
	send: (data) => http.post('/api/chat', data, { showLoading: false, timeout: LLM_TIMEOUT }),
	// 会员客服：huiyuankefu.vue
	kefu: (data) => http.post('/api/kefu/chat', data, { timeout: LLM_TIMEOUT }),
	sessions: () => http.get('/api/chat/sessions'),
	messages: (sessionId) => http.get('/api/chat/sessions/' + sessionId + '/messages'),
	removeSession: (sessionId) => http.del('/api/chat/sessions/' + sessionId)
}

/* ---------------- 文件上传 ---------------- */
export const fileApi = {
	upload: (filePath) => uploadFile(filePath)
}

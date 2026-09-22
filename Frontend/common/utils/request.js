/**
 * 统一网络请求封装
 * - 自动拼接后端 BASE_URL
 * - 自动携带 JWT（Authorization: Bearer <token>）
 * - 统一解析后端返回体 { code, message, data }
 * - 401 自动跳转登录
 */
import { getToken, clearAuth } from './auth.js'

// 后端服务地址：
// - Android 模拟器访问宿主机用 10.0.2.2
// - iOS 模拟器 / H5 用 localhost
// - 真机请改成电脑局域网 IP，例如 http://192.168.1.10:8080
export const BASE_URL = 'http://localhost:8080'

// 普通接口超时（毫秒）
const REQUEST_TIMEOUT = 30000
// AI 对话接口超时（毫秒）：后端代理大模型，正常可能近一分钟
export const LLM_TIMEOUT = 120000

/**
 * 拼接后端完整地址（小程序不能直接请求相对路径）
 */
export function buildUrl(url = '') {
	if (/^https?:\/\//i.test(url)) {
		return url
	}
	return BASE_URL + url
}

/**
 * 后端返回的相对资源路径（如 /uploads/xxx.png）转为绝对 URL，供 image 组件直接使用
 */
export function toAbsoluteUrl(url) {
	if (!url || typeof url !== 'string') {
		return url
	}
	return buildUrl(url)
}

function toLogin() {
	clearAuth()
	const pages = getCurrentPages()
	const current = pages.length ? pages[pages.length - 1].route : ''
	if (current && current.indexOf('pages/login/login') >= 0) {
		return
	}
	uni.navigateTo({ url: '/pages/login/login' })
}

export function request(options = {}) {
	const { url, method = 'GET', data = {}, header = {}, showLoading = false, timeout = REQUEST_TIMEOUT } = options

	if (showLoading) {
		uni.showLoading({ title: '加载中...', mask: true })
	}

	return new Promise((resolve, reject) => {
		const token = getToken()
		uni.request({
			url: buildUrl(url),
			method: method.toUpperCase(),
			data,
			timeout,
			header: {
				'Content-Type': 'application/json',
				...(token ? { Authorization: 'Bearer ' + token } : {}),
				...header
			},
			success: (res) => {
				const body = res.data
				// 后端统一返回 { code, message, data }
				if (res.statusCode === 200 && body && body.code === 200) {
					resolve(body.data)
					return
				}
				if (body && body.code === 401) {
					toLogin()
					reject(body)
					return
				}
				const msg = (body && body.message) || ('请求失败(' + res.statusCode + ')')
				uni.showToast({ title: msg, icon: 'none' })
				reject(body || { message: msg })
			},
			fail: (err) => {
				uni.showToast({ title: '网络连接失败，请检查后端服务是否启动', icon: 'none' })
				reject(err)
			},
			complete: () => {
				if (showLoading) {
					uni.hideLoading()
				}
			}
		})
	})
}

export const http = {
	get: (url, data, opt = {}) => request({ url, method: 'GET', data, ...opt }),
	post: (url, data, opt = {}) => request({ url, method: 'POST', data, ...opt }),
	put: (url, data, opt = {}) => request({ url, method: 'PUT', data, ...opt }),
	del: (url, data, opt = {}) => request({ url, method: 'DELETE', data, ...opt })
}

/**
 * 图片上传：调用后端 /api/files/upload
 * @param {string} filePath uni.chooseImage 得到的本地临时路径
 * @returns {Promise<string>} 后端存储的相对路径（如 /uploads/2026-09-22/x.jpg），
 *   保持相对入库存放，展示时用 toAbsoluteUrl() 拼当前 BASE_URL
 */
export function uploadFile(filePath) {
	const token = getToken()
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: BASE_URL + '/api/files/upload',
			timeout: 120000,
			filePath,
			name: 'file',
			header: token ? { Authorization: 'Bearer ' + token } : {},
			success: (res) => {
				try {
					const body = JSON.parse(res.data)
					if (body.code === 200) {
						resolve(body.data.url)
					} else if (body.code === 401) {
						toLogin()
						reject(body)
					} else {
						uni.showToast({ title: body.message || '上传失败', icon: 'none' })
						reject(body)
					}
				} catch (e) {
					reject(e)
				}
			},
			fail: (err) => {
				uni.showToast({ title: '上传失败', icon: 'none' })
				reject(err)
			}
		})
	})
}

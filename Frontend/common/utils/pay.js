/**
 * 支付工具：统一封装调起微信支付收银台
 * - 微信小程序：调用 uni.requestPayment，弹出真实微信支付界面
 *   （timeStamp / nonceStr / package / signType / paySign 由后端 /api/payments/wxpay 下发）
 * - 非微信小程序环境（H5 / App 调试）：用系统弹窗模拟，保证下单流程可跑通
 * @param {Object} params 后端返回的支付参数，额外可带 amount（用于展示）
 * @returns {Promise<Object>} 支付成功 resolve，取消/失败 reject
 */
export function invokeWxPayment(params = {}) {
	return new Promise((resolve, reject) => {
		// #ifdef MP-WEIXIN
		uni.requestPayment({
			provider: 'wxpay',
			timeStamp: params.timeStamp,
			nonceStr: params.nonceStr,
			package: params.package,
			signType: params.signType || 'RSA',
			paySign: params.paySign,
			success: (res) => resolve(res),
			fail: (err) => reject(err)
		})
		// #endif

		// #ifndef MP-WEIXIN
		uni.showModal({
			title: '微信支付',
			content: params.amount != null ? `确认支付 ¥${params.amount}` : '确认支付',
			confirmText: '确认支付',
			success: (res) => {
				if (res.confirm) {
					resolve({ mock: true })
				} else {
					reject({ errMsg: 'payment:cancel' })
				}
			},
			fail: (err) => reject(err)
		})
		// #endif
	})
}

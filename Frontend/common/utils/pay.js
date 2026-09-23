/**
 * 支付工具：统一封装调起微信支付
 * - 后端 /api/payments/wxpay 在商户号未配置时会返回 mock 参数（prepay_id=mock_xxx / MOCK_SIGN_xxx），
 *   真机也调不起真实收银台。isMockPayParams() 用于识别这种“无法真实支付”的情况。
 * - invokeWxPayment 仅在微信小程序 + 真实商户参数下调用 uni.requestPayment。
 *   其余环境请由页面降级展示“付款码”，保证下单流程可跑通。
 */

/**
 * 判断后端下发的支付参数是否为 mock（即当前环境无法调起真实微信支付）
 * @param {Object} params 后端 /api/payments/wxpay 返回体
 * @returns {boolean} true 表示为模拟参数，需要降级展示付款码
 */
export function isMockPayParams(params = {}) {
	const pkg = params.package || params.packageValue || ''
	const sign = params.paySign || ''
	return params.mock === true
		|| pkg.indexOf('prepay_id=mock_') === 0
		|| sign.indexOf('MOCK_SIGN_') === 0
		|| !pkg
}

/**
 * 调起微信支付收银台（仅微信小程序 + 真实商户参数场景有效）
 * @param {Object} params 后端返回的支付参数
 * @returns {Promise<Object>} 支付成功 resolve，取消/失败 reject
 */
export function invokeWxPayment(params = {}) {
	return new Promise((resolve, reject) => {
		// #ifdef MP-WEIXIN
		uni.requestPayment({
			provider: 'wxpay',
			timeStamp: params.timeStamp,
			nonceStr: params.nonceStr,
			package: params.package || params.packageValue,
			signType: params.signType || 'RSA',
			paySign: params.paySign,
			success: (res) => resolve(res),
			fail: (err) => reject(err)
		})
		// #endif

		// #ifndef MP-WEIXIN
		// 非微信小程序环境无法调起微信支付，交由页面降级展示付款码
		reject({ errMsg: 'payment:unsupported', unsupported: true })
		// #endif
	})
}

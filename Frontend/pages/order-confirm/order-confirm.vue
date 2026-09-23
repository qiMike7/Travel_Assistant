<template>
	<view class="confirm-page">
		<CustomNavBar :showBack="true" title="确认订单" @back="handleBack" />

		<!-- 收货地址（目的地址暂可跳过） -->
		<view class="address-card" @tap="chooseAddress">
			<view class="address-icon">📍</view>
			<view class="address-info">
				<block v-if="address">
					<text class="address-line">{{ address.userName }} {{ address.telNumber }}</text>
					<text class="address-detail">{{ address.detailInfo }}</text>
				</block>
				<text v-else class="address-empty">请填写收货地址（暂可跳过）</text>
			</view>
			<text class="address-arrow">›</text>
		</view>

		<!-- 商品清单 -->
		<view class="goods-card">
			<view class="goods-item">
				<image class="goods-image" :src="productImage" mode="aspectFill"></image>
				<view class="goods-detail">
					<text class="goods-name">{{ product.name }}</text>
					<text class="goods-price">¥{{ product.price }}</text>
				</view>
				<view class="stepper">
					<text class="step-btn" :class="{ disabled: quantity <= 1 }" @tap="changeQty(-1)">－</text>
					<text class="step-num">{{ quantity }}</text>
					<text class="step-btn" @tap="changeQty(1)">＋</text>
				</view>
			</view>
			<view class="goods-summary">
				<text class="summary-label">商品合计</text>
				<text class="summary-value">¥{{ totalAmount }}</text>
			</view>
		</view>

		<!-- 底部支付栏 -->
		<view class="pay-bar">
			<view class="pay-total">
				<text class="pay-total-label">实付金额</text>
				<text class="pay-total-value">¥{{ totalAmount }}</text>
			</view>
			<button class="pay-btn" :loading="paying" @tap="payNow">微信支付</button>
		</view>

		<!-- 付款码面板（无法调起真实微信支付时的降级展示） -->
		<view v-if="payCode.visible" class="paycode-mask" @tap.stop="closePayCode">
			<view class="paycode-panel" @tap.stop>
				<text class="paycode-title">微信扫一扫付款</text>
				<text class="paycode-amount">¥{{ payCode.amount }}</text>
				<canvas canvas-id="payQr" id="payQr" class="paycode-qr"></canvas>
				<text class="paycode-order">订单号：{{ payCode.orderNo }}</text>
				<text class="paycode-tip">当前为模拟器/未配置商户号环境，展示的是演示付款码</text>
				<button class="paycode-done" @tap="finishMockPay">我已完成支付</button>
				<text class="paycode-cancel" @tap="closePayCode">取消</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive, computed, nextTick, getCurrentInstance } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { orderApi, payApi } from '@/common/api/index.js'
import { toAbsoluteUrl } from '@/common/utils/request.js'
import { invokeWxPayment, isMockPayParams } from '@/common/utils/pay.js'
import { isLogin } from '@/common/utils/auth.js'

const instance = getCurrentInstance()

const product = ref({ id: null, name: '', price: 0 })
const quantity = ref(1)
const address = ref(null)
const paying = ref(false)

// 付款码面板（无法调起真实微信支付时的降级展示）
const payCode = reactive({ visible: false, amount: '', orderNo: '', orderId: null })

// 商品图片：后端种子数据的图片是本地静态资源路径（如 /static/shopping/bag.jpg）
// 或绝对地址，需直接使用；仅后端相对上传路径（如 /uploads/xx）才拼接 BASE_URL
const productImage = computed(() => {
	const img = product.value.image
	if (!img) return '/static/shopping/shouna.jpg'
	if (img.indexOf('/static') === 0 || /^https?:\/\//i.test(img)) return img
	return toAbsoluteUrl(img) || '/static/shopping/shouna.jpg'
})
const totalAmount = computed(() => {
	const p = Number(product.value.price) || 0
	return (p * quantity.value).toFixed(2)
})

onLoad((options = {}) => {
	product.value = {
		id: options.id,
		name: decodeURIComponent(options.name || ''),
		price: Number(options.price) || 0,
		image: decodeURIComponent(options.image || '')
	}
})

const handleBack = () => {
	uni.navigateBack({ delta: 1 })
}

// 选择收货地址：调用微信地址能力，取消则跳过（目的地址先不强制）
const chooseAddress = () => {
	uni.chooseAddress({
		success: (res) => { address.value = res },
		fail: () => { /* 用户取消或不支持，忽略 */ }
	})
}

const changeQty = (delta) => {
	const next = quantity.value + delta
	if (next < 1) return
	quantity.value = next
}

// 下单 -> 调起支付（微信小程序 + 真实商户）或降级展示付款码
const payNow = async () => {
	if (!isLogin()) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	if (paying.value) return
	paying.value = true
	let order = null
	try {
		// 1. 创建待支付订单（后端初始状态为“待付款”，支付确认后才变“已付款”）
		order = await orderApi.create({
			productId: product.value.id,
			quantity: quantity.value
		})
		const oid = order && (order.id || order.orderId)
		// 2. 向后端换取微信支付参数（目的地址/商户号由后端处理）
		const payParams = await payApi.wechat({
			orderId: oid,
			orderNo: order && order.orderNo,
			amount: totalAmount.value
		})
		// 3. 微信小程序且为真实商户参数：调起微信收银台
		// #ifdef MP-WEIXIN
		if (!isMockPayParams(payParams)) {
			try {
				await invokeWxPayment({ ...(payParams || {}), amount: totalAmount.value })
				// 支付成功：调后端确认，订单推进为“已付款”
				await payApi.confirm({ orderId: oid })
				paying.value = false
				afterPaySuccess()
				return
			} catch (e) {
				// 用户在收银台取消：取消该笔未付款订单，不在历史里留下“已付款”
				if (e && e.errMsg && e.errMsg.indexOf('cancel') >= 0) {
					paying.value = false
					abortUnpaidOrder(oid)
					return
				}
				// 已支付但确认接口失败（有 code 的是接口错误）：引导去订单记录查看
				if (e && e.code) {
					paying.value = false
					uni.showToast({ title: '支付结果确认中，请在订单记录查看', icon: 'none', duration: 3000 })
					return
				}
				// 调起失败：降级展示付款码
			}
		}
		// #endif
		// 4. 无法真实支付（模拟器 / 未配置商户号 / 非微信环境）：展示付款码
		showPayCodePanel(order)
	} catch (e) {
		// 接口错误：request 封装已统一提示
	} finally {
		paying.value = false
	}
}

// 放弃支付：取消该笔未付款订单（后端会回退库存与销量），避免历史订单里留下“已付款”假记录
const abortUnpaidOrder = (orderId) => {
	if (orderId == null) return
	orderApi.cancel(orderId)
		.then(() => uni.showToast({ title: '已取消支付', icon: 'none' }))
		.catch(() => { /* 取消失败时订单仍为“待付款”，不会误标已付款 */ })
}

// 展示付款码面板（当前环境无法调起微信支付时的降级方案）
const showPayCodePanel = (order) => {
	payCode.amount = totalAmount.value
	payCode.orderNo = (order && order.orderNo) || ('' + Date.now())
	payCode.orderId = (order && (order.id || order.orderId)) || null
	payCode.visible = true
	nextTick(() => drawPayQr(payCode.orderNo))
}

// 本地自绘付款二维码（演示用图案，非真实可扫描）：根据订单号确定性地生成点阵
const drawPayQr = (seedStr) => {
	try {
		const SIZE = 220
		const N = 25
		const CELL = Math.floor(SIZE / (N + 2))
		const OFF = Math.floor((SIZE - CELL * N) / 2)
		const ctx = uni.createCanvasContext('payQr', instance.proxy)
		ctx.setFillStyle('#ffffff')
		ctx.fillRect(0, 0, SIZE, SIZE)
		// 字符串哈希，保证同一订单号图案一致
		let h = 2166136261
		for (let i = 0; i < seedStr.length; i++) {
			h ^= seedStr.charCodeAt(i)
			h = Math.imul(h, 16777619)
		}
		let seed = h >>> 0
		const rand = () => {
			seed = (Math.imul(seed, 1103515245) + 12345) & 0x7fffffff
			return seed / 0x7fffffff
		}
		const inFinder = (x, y) =>
			(x < 8 && y < 8) || (x >= N - 8 && y < 8) || (x < 8 && y >= N - 8)
		ctx.setFillStyle('#111111')
		for (let y = 0; y < N; y++) {
			for (let x = 0; x < N; x++) {
				if (inFinder(x, y)) continue
				if (rand() > 0.5) {
					ctx.fillRect(OFF + x * CELL, OFF + y * CELL, CELL, CELL)
				}
			}
		}
		// 三个定位角
		const finder = (fx, fy) => {
			const px = OFF + fx * CELL
			const py = OFF + fy * CELL
			ctx.setFillStyle('#111111')
			ctx.fillRect(px, py, CELL * 7, CELL * 7)
			ctx.setFillStyle('#ffffff')
			ctx.fillRect(px + CELL, py + CELL, CELL * 5, CELL * 5)
			ctx.setFillStyle('#111111')
			ctx.fillRect(px + CELL * 2, py + CELL * 2, CELL * 3, CELL * 3)
		}
		finder(1, 1)
		finder(N - 8, 1)
		finder(1, N - 8)
		ctx.draw()
	} catch (e) {
		// 绘图失败不阻断付款码展示
	}
}

// 用户点击“我已完成支付”：调后端把订单确认为已付款后再进入订单
const finishMockPay = async () => {
	const oid = payCode.orderId
	payCode.visible = false
	if (oid == null) {
		afterPaySuccess()
		return
	}
	try {
		await payApi.confirm({ orderId: oid })
		afterPaySuccess()
	} catch (e) {
		// 确认失败：request 封装已统一提示，不展示“支付成功”弹窗
	}
}

// 关闭付款码面板 = 未完成支付：取消订单，不在历史记录里留下“已付款”
const closePayCode = () => {
	const oid = payCode.orderId
	payCode.visible = false
	payCode.orderId = null
	abortUnpaidOrder(oid)
}

const afterPaySuccess = () => {
	uni.showModal({
		title: '支付成功',
		content: `已支付 ¥${totalAmount.value}，可在“订单记录”中查看`,
		confirmText: '查看订单',
		cancelText: '返回',
		success: (res) => {
			if (res.confirm) {
				uni.redirectTo({ url: '/pages/order/order' })
			} else {
				uni.navigateBack({ delta: 1 })
			}
		}
	})
}
</script>

<style lang="scss" scoped>
.confirm-page {
	min-height: 100vh;
	background: #f5f6fa;
	padding-bottom: 140rpx;
}

.address-card {
	display: flex;
	align-items: center;
	background: #fff;
	margin: 24rpx;
	padding: 30rpx 24rpx;
	border-radius: 20rpx;
}

.address-icon {
	font-size: 40rpx;
	margin-right: 20rpx;
}

.address-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.address-line {
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.address-detail {
	font-size: 26rpx;
	color: #666;
	margin-top: 8rpx;
}

.address-empty {
	font-size: 28rpx;
	color: #999;
}

.address-arrow {
	font-size: 40rpx;
	color: #ccc;
}

.goods-card {
	background: #fff;
	margin: 0 24rpx;
	border-radius: 20rpx;
	padding: 24rpx;
}

.goods-item {
	display: flex;
	align-items: center;
}

.goods-image {
	width: 140rpx;
	height: 140rpx;
	border-radius: 16rpx;
	background: #f0f0f0;
}

.goods-detail {
	flex: 1;
	margin-left: 20rpx;
	display: flex;
	flex-direction: column;
}

.goods-name {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 12rpx;
}

.goods-price {
	font-size: 30rpx;
	color: #ff4444;
	font-weight: bold;
}

.stepper {
	display: flex;
	align-items: center;
}

.step-btn {
	width: 52rpx;
	height: 52rpx;
	line-height: 48rpx;
	text-align: center;
	font-size: 30rpx;
	color: #333;
	border: 1rpx solid #ddd;
	border-radius: 10rpx;

	&.disabled {
		color: #ccc;
		border-color: #eee;
	}
}

.step-num {
	min-width: 60rpx;
	text-align: center;
	font-size: 28rpx;
	color: #333;
}

.goods-summary {
	display: flex;
	justify-content: flex-end;
	align-items: center;
	margin-top: 24rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
}

.summary-label {
	font-size: 26rpx;
	color: #666;
	margin-right: 12rpx;
}

.summary-value {
	font-size: 30rpx;
	color: #ff4444;
	font-weight: bold;
}

.pay-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	height: 110rpx;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
	box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.pay-total {
	display: flex;
	align-items: baseline;
}

.pay-total-label {
	font-size: 26rpx;
	color: #666;
	margin-right: 10rpx;
}

.pay-total-value {
	font-size: 36rpx;
	color: #ff4444;
	font-weight: bold;
}

.pay-btn {
	background: linear-gradient(135deg, #4F52FF 0%, #FF9CCF 100%);
	color: #fff;
	font-size: 30rpx;
	border-radius: 44rpx;
	padding: 0 50rpx;
	height: 76rpx;
	line-height: 76rpx;
	margin: 0;
}

.paycode-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.55);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}

.paycode-panel {
	width: 560rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 48rpx 40rpx 36rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.paycode-title {
	font-size: 32rpx;
	color: #333;
	font-weight: bold;
}

.paycode-amount {
	font-size: 48rpx;
	color: #ff4444;
	font-weight: bold;
	margin: 16rpx 0 24rpx;
}

.paycode-qr {
	width: 220px;
	height: 220px;
	background: #fff;
	border: 1rpx solid #eee;
	border-radius: 12rpx;
}

.paycode-order {
	font-size: 24rpx;
	color: #666;
	margin-top: 20rpx;
}

.paycode-tip {
	font-size: 22rpx;
	color: #999;
	margin-top: 10rpx;
	text-align: center;
	line-height: 1.5;
}

.paycode-done {
	margin-top: 30rpx;
	width: 100%;
	background: #07c160;
	color: #fff;
	font-size: 30rpx;
	border-radius: 44rpx;
	height: 80rpx;
	line-height: 80rpx;
}

.paycode-cancel {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #999;
}
</style>

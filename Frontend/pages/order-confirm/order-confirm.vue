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
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { orderApi, payApi } from '@/common/api/index.js'
import { toAbsoluteUrl } from '@/common/utils/request.js'
import { invokeWxPayment } from '@/common/utils/pay.js'
import { isLogin } from '@/common/utils/auth.js'

const product = ref({ id: null, name: '', price: 0 })
const quantity = ref(1)
const address = ref(null)
const paying = ref(false)

const productImage = computed(() =>
	toAbsoluteUrl(product.value.image) || '/static/shopping/shouna.jpg'
)
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

// 下单 -> 调起微信支付 -> 成功回调
const payNow = async () => {
	if (!isLogin()) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	if (paying.value) return
	paying.value = true
	try {
		// 1. 创建待支付订单
		const order = await orderApi.create({
			productId: product.value.id,
			quantity: quantity.value
		})
		// 2. 向后端换取微信支付参数（目的地址/商户号由后端处理）
		const payParams = await payApi.wechat({
			orderId: order && (order.id || order.orderId),
			orderNo: order && order.orderNo,
			amount: totalAmount.value
		})
		// 3. 调起微信支付收银台
		await invokeWxPayment({ ...(payParams || {}), amount: totalAmount.value })
		// 4. 支付成功
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
	} catch (e) {
		// 支付取消或接口错误：request 封装已统一提示
	} finally {
		paying.value = false
	}
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
</style>

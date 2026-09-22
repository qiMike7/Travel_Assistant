<template>
	<view class="container">
		<CustomNavBar :showBack="true" title="精品商城" @back="handleBack" />

		<!-- 商品列表 -->
		<scroll-view scroll-y="true" class="product-list">
			<view class="product-grid">
				<view
					class="product-card"
					v-for="(product, index) in products"
					:key="index"
				>
					<image :src="product.image" mode="aspectFill" class="product-image"></image>
					<view class="product-info">
						<text class="product-name">{{ product.name }}</text>
						<text class="product-desc">{{ product.description }}</text>
						<view class="price-row">
							<text class="price">¥{{ product.price }}</text>
							<text class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</text>
						</view>
						<view class="buy-btn" @click.stop="buyProduct(product)">立即购买</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { productApi } from '@/common/api/index.js'

// 商品列表数据（来自后端 /api/products）
const products = ref([])

const loadProducts = async () => {
	try {
		const res = await productApi.list()
		products.value = res || []
	} catch (e) {
		// 错误提示已在请求封装中统一处理
	}
}

// 返回上一页
const handleBack = () => {
	uni.navigateBack({
		delta: 1
	})
}

// 立即购买：进入订单确认页（选数量/地址 -> 调起微信支付）
const buyProduct = (product) => {
	const query = [
		`id=${product.id}`,
		`name=${encodeURIComponent(product.name || '')}`,
		`price=${product.price != null ? product.price : ''}`,
		`image=${encodeURIComponent(product.image || '')}`
	].join('&')
	uni.navigateTo({ url: `/pages/order-confirm/order-confirm?${query}` })
}

onLoad(() => {
	loadProducts()
})
</script>

<style>
/* 页面背景样式 */
page {
	background: linear-gradient(180deg, rgba(79, 82, 255, 0.18) 0.07%, rgba(79, 82, 255, 0) 75.38%);
	min-height: 100vh;
	position: relative;
}

page::before {
	content: '';
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background:
		radial-gradient(ellipse 262px 268px at 31% 41%, rgba(255, 156, 207, 0.5) 0%, transparent 70%),
		radial-gradient(ellipse 262px 268px at 69% 31%, rgba(0, 251, 255, 0.5) 0%, transparent 70%);
	filter: blur(100px);
	z-index: -1;
	pointer-events: none;
}

.container {
	min-height: 100vh;
	background: transparent;
}

.product-list {
	height: calc(100vh - 100rpx);
}

.product-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 40rpx;
	padding: 30rpx;
}

.product-card {
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(10px);
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 32rpx rgba(79, 82, 255, 0.15);
	transition: all 0.3s ease;
	border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.product-card:active {
	transform: scale(0.98);
	box-shadow: 0 4rpx 16rpx rgba(79, 82, 255, 0.2);
}

.product-image {
	width: 100%;
	height: 220rpx;
	object-fit: cover;
}

.product-info {
	padding: 16rpx;
}

.product-name {
	font-size: 26rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 8rpx;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.product-desc {
	font-size: 22rpx;
	color: #666;
	display: block;
	margin-bottom: 12rpx;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.price-row {
	display: flex;
	align-items: center;
	margin-bottom: 12rpx;
}

.price {
	color: #ff4757;
	font-size: 32rpx;
	font-weight: bold;
}

.original-price {
	color: #999;
	font-size: 22rpx;
	text-decoration: line-through;
	margin-left: 12rpx;
}

.buy-btn {
	background: linear-gradient(135deg, #4F52FF 0%, #FF9CCF 100%);
	color: white;
	text-align: center;
	padding: 12rpx 0;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: bold;
	box-shadow: 0 4rpx 15rpx rgba(79, 82, 255, 0.3);
	transition: all 0.3s ease;
}

.buy-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(79, 82, 255, 0.2);
}

</style>

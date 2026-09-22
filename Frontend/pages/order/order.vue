<template>
	<view>
		<!-- 带返回按钮的导航栏 -->
		<CustomNavBar :showBack="true" title="我的订单" @back="handleBack" />
		
		<!-- 订单列表 -->
		<view v-if="orders.length === 0" class="empty-tip">
			<text>暂无订单，去精品商城看看吧～</text>
		</view>
		<view class="order-card" v-for="(order, index) in orders" :key="index">
			<view class="order-header">
				<text class="order-number">订单号：{{ order.orderNo }}</text>
				<text class="order-status">{{ order.status }}</text>
			</view>
			<view class="order-content">
				<text class="product-label">商品：</text>
				<text class="product-name">{{ order.productName }} x{{ order.quantity }}</text>
			</view>
			<view class="order-footer">
				<text class="price-label">总价：</text>
				<text class="price-value">¥ {{ order.totalAmount }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { orderApi } from '@/common/api/index.js'

const orders = ref([])

const loadOrders = async () => {
	try {
		const res = await orderApi.list()
		orders.value = res || []
	} catch (e) {
		// 错误提示已在请求封装中统一处理（未登录会跳转登录页）
	}
}

const handleBack = () => {
	uni.navigateBack({
		delta: 1
	});
}

onLoad(() => {
	loadOrders()
})
</script>

<style lang="scss" scoped>
.order-card {
	margin: 30rpx;
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	border: 1px solid rgba(0, 0, 0, 1);
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 15rpx;
	border-bottom: 1rpx solid #F0F0F0;
}

.order-number {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.order-status {
	font-size: 26rpx;
	color: #666;
	background-color: #F5F5F5;
	padding: 8rpx 16rpx;
	border-radius: 12rpx;
}

.order-content {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.product-label {
	font-size: 28rpx;
	color: #333;
	margin-right: 10rpx;
}

.product-name {
	font-size: 28rpx;
	color: #333;
	flex: 1;
}

.order-footer {
	display: flex;
	align-items: center;
}

.price-label {
	font-size: 28rpx;
	color: #333;
	margin-right: 10rpx;
}

.price-value {
	font-size: 32rpx;
	color: #FF4444;
	font-weight: bold;
}

.empty-tip {
	text-align: center;
	color: #999;
	font-size: 28rpx;
	margin-top: 120rpx;
}
</style>

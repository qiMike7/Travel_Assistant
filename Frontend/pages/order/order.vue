<template>
	<view>
		<!-- 带返回按钮的导航栏 -->
		<CustomNavBar :showBack="true" title="我的订单" @back="handleBack" />
		
		<!-- 订单卡片 -->
		<view class="order-card">
			<view class="order-header">
				<text class="order-number">订单号：1238526852352652</text>
				<text class="order-status">已付款</text>
			</view>
			<view class="order-content">
				<text class="product-label">商品：</text>
				<text class="product-name">商品名称-24小时使用</text>
			</view>
			<view class="order-footer">
				<text class="price-label">总价：</text>
				<text class="price-value">¥ 6</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import CustomNavBar from '@/components/CustomNavBar.vue'

const handleBack = () => {
	uni.navigateBack({
		delta: 1
	});
}

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
</style>

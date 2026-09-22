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
import { ref, reactive } from 'vue'
import CustomNavBar from '@/components/CustomNavBar.vue'

// 商品列表数据
const products = reactive([
	{
		id: 1,
		name: "户外登山背包",
		description: "轻便防水，适合登山徒步",
		price: 299,
		originalPrice: 399,
		image: "/static/shopping/bag.jpg"
	},
	{
		id: 2,
		name: "旅行收纳套装",
		description: "多层收纳，整理神器",
		price: 89,
		originalPrice: 129,
		image: "/static/shopping/shouna.jpg"
	},
	{
		id: 3,
		name: "文创笔记本",
		description: "精美设计，记录美好时光",
		price: 19,
		originalPrice: 29,
		image: "/static/shopping/notebook.jpg"
	},
	{
		id: 4,
		name: "古镇手工茶具",
		description: "传统工艺，品味文化",
		price: 599,
		originalPrice: 799,
		image: "/static/shopping/teacup.jpg"
	},
	{
		id: 5,
		name: "便携折叠水壶",
		description: "折叠设计，携带方便",
		price: 159,
		originalPrice: 229,
		image: "/static/shopping/bottle.jpg"
	},
	{
		id: 6,
		name: "旅游充电宝",
		description: "大容量，快充支持",
		price: 89,
		originalPrice: 139,
		image: "/static/shopping/chongdianbao.jpg"
	}
])

// 当前选中的商品
const currentProduct = ref(null)

// 返回上一页
const handleBack = () => {
	uni.navigateBack({
		delta: 1
	})
}

// 购买商品
const buyProduct = (product) => {
	currentProduct.value = product
	// 使用uni-app的showModal提示购买成功
	uni.showModal({
		title: '购买成功！',
		content: `恭喜您成功购买 ${product.name}，价格：¥${product.price}`,
		showCancel: false,
		confirmText: '确定',
		success: (res) => {
			if (res.confirm) {
				currentProduct.value = null
			}
		}
	})
}
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

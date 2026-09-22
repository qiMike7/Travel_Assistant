<template>
	<view class="content">
		<!-- 带返回按钮的导航栏 -->
		<CustomNavBar 
			:title="'用户中心'"
			:showBack="true"
			@back="handleBack"
		/>
		
		<view class="main-content">
			<view class="demo-section">
				<text class="demo-title">导航栏组件使用示例</text>
				
				<view class="demo-item">
					<text class="demo-label">1. 仅显示标题（居中）：</text>
					<text class="demo-code">:title="'页面标题'" :showBack="false"</text>
				</view>
				
				<view class="demo-item">
					<text class="demo-label">2. 显示返回按钮 + 标题：</text>
					<text class="demo-code">:title="'页面标题'" :showBack="true"</text>
				</view>
				
				<view class="demo-item">
					<text class="demo-label">3. 自定义左侧文本：</text>
					<text class="demo-code">:leftText="'取消'" :showBack="false"</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import CustomNavBar from '@/components/CustomNavBar.vue'

export default {
	components: {
		CustomNavBar
	},
	data() {
		return {
			
		}
	},
	methods: {
		handleBack() {
			console.log('自定义返回处理');
			// 可以在这里添加自定义返回逻辑
		}
	}
}
</script>

<style lang="scss" scoped>
.content {
	min-height: 100vh;
	background: #f5f5f5;
}

.main-content {
	padding: 120rpx 50rpx 0;
}

.demo-section {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 40rpx;
}

.demo-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 40rpx;
	display: block;
}

.demo-item {
	margin-bottom: 30rpx;
	padding: 20rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
}

.demo-label {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	display: block;
	margin-bottom: 10rpx;
}

.demo-code {
	font-size: 24rpx;
	color: #666;
	font-family: 'Courier New', monospace;
	background: #e9ecef;
	padding: 10rpx;
	border-radius: 6rpx;
	display: block;
}
</style>

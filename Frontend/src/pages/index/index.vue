<template>
	<view class="content">
		<!-- 自定义导航栏 -->
		<CustomNavBar 
			:showBack="false"
			@leftClick="handleNavLeftClick"
		/>
		
		<!-- 主要内容区域 -->
		<view class="main-content">
			<!-- 主标题 -->
			<view class="title-section">
				<text class="main-title">智驭四海 趣循八方</text>
				<text class="subtitle">算法为辔，驰骋经纬之间，大漠孤烟之远 深巷杏花之幽，洞明方位，尽得真趣</text>
			</view>

			<!-- 功能卡片区域 -->
			<view class="cards-section">
				<!-- 左侧卡片 -->
				<view class="card left-card">
					<view class="card-img">
						<image class="pic-image" src="/static/assets/nav1.jpeg" mode="aspectFit"></image>
						<view class="icon-bg">
							<image class="icon-image" src="/static/icons/arrow.png" mode="aspectFit"></image>
						</view>
					</view>
					<text class="card-title">智慧大模型问答聊天</text>
				</view>

				<!-- 右侧卡片 -->
				<view class="card right-card">
					<view class="card-img">
						<image class="pic-image" src="/static/assets/nav2.jpeg" mode="aspectFit"></image>
						<view class="icon-bg">
							<image class="icon-image" src="/static/icons/arrow.png" mode="aspectFit"></image>
						</view>
					</view>
					<text class="card-title">快速智慧旅行攻略规划</text>
				</view>
			</view>

			<!-- 大卡片区域 -->
			<view class="large-card">
				<view class="large-card-image">
					<image class="icon-image" src="/static/assets/nav3.jpeg" mode="aspectFit"></image>
				</view>
				<!-- 底部按钮 -->
				<view class="bottom-button">
					<text class="button-text">智能导览 打卡探趣</text>
				</view>
			</view>

			<!-- Meta Tourism 文本 -->
			<view class="meta-text">
				<text class="meta-label">由 Meta- Tourism 自研文旅垂类语言模型支持</text>
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
			title: '智驱四海 越循八方'
		}
	},
	onLoad() {

	},
	methods: {
		handleNavLeftClick() {
			console.log('导航栏左侧点击');
			// 可以在这里处理左侧点击事件
		}
	}
}
</script>

<style lang="scss" scoped>
.content {
	min-height: 100vh;
	position: relative;
}

.main-content {
	padding: 0 50rpx;
	position: relative;
	z-index: 5;
	// 移除固定的顶部间距，因为导航栏现在使用相对定位
}

.title-section {
	text-align: center;
	margin-top: 80rpx;
	margin-bottom: 120rpx;
}

.main-title {
	font-size: 64rpx;
	font-weight: bold;
	color: #000;
	font-family: 'CustomFont', 'JapaneseFont', sans-serif;
	line-height: 1.2;
	display: block;
	margin-bottom: 40rpx;
}

.subtitle {
	font-size: 36rpx;
	color: #666;
	line-height: 1.6;
	font-family: 'JapaneseFont', 'CustomFont', sans-serif;
	display: block;
}

.cards-section {
	display: flex;
	gap: 26rpx;
	margin-bottom: 40rpx;
}

.card {
	flex: 1;
	background: #FFFFFF;
	border-radius: 30rpx;
	padding: 20rpx 20rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	height: 260rpx;
	width: 280rpx;
}

.card-img {
	position: relative;
	width: 260rpx;
	height: 220rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.pic-image {
	width: 200rpx;
	height: 200rpx;
	border-radius: 30rpx;
}

.icon-bg {
	position: absolute;
	top: 10rpx;
	right: 0;
	width: 50rpx;
	height: 50rpx;
	background: #D4EEFF;
	border-radius: 15rpx;
	display: flex;
	align-items: center;
	justify-content: center;

	.icon-image {
		width: 35rpx;
		height: 35rpx;
		transform: translateY(-5rpx);
	}
}

.card-title {
	font-size: 28rpx;
	color: #333;
	line-height: 1.4;
	font-family: 'CustomFont', sans-serif;
}

.large-card {
	background: #FFFFFF;
	border-radius: 30rpx;
	padding: 30rpx 40rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
	height: 420rpx;
}

.large-card-image {
	width: 100%;
	height: 300rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 20rpx;

	image {
		width: 100%;
		height: 320rpx;
		border-radius: 50rpx; // 增大圆角半径使其更圆润
	}
}


.bottom-button {
	background: #003DF5;
	border-radius: 27rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	text-align: center;
}

.button-text {
	color: #FFFFFF;
	font-size: 32rpx;
	font-weight: 600;
	font-family: 'CustomFont', sans-serif;
}

.meta-text {
	text-align: center;
	margin-bottom: 40rpx;
}

.meta-label {
	font-size: 24rpx;
	color: #666;
	font-family: 'CustomFont', sans-serif;
}

// 响应式适配
@media (max-width: 375px) {
	.main-content {
		padding: 100rpx 30rpx 0;
	}
	
	.title-section {
		margin-top: 50rpx;
		margin-bottom: 80rpx;
	}
	
	.main-title {
		font-size: 48rpx;
	}
	
	.subtitle {
		font-size: 28rpx;
		padding: 0 20rpx;
	}
}

@media (min-width: 768px) {
	.main-content {
		padding: 140rpx 80rpx 0;
		max-width: 1400rpx;
		margin: 0 auto;
	}
	
	.title-section {
		margin-top: 100rpx;
		margin-bottom: 150rpx;
	}
	
	.main-title {
		font-size: 80rpx;
	}
	
	.subtitle {
		font-size: 42rpx;
		max-width: 800rpx;
		margin: 0 auto;
	}
}
</style>

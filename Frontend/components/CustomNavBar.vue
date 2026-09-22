<template>
	<view class="custom-navbar" :style="{ height: totalHeight + 'px' }">
		<!-- 状态栏占位 -->
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		
		<!-- 导航栏内容 -->
		<view class="navbar-content" :style="{ height: navBarHeight + 'px' }">
			<!-- 左侧区域 -->
			<view class="navbar-left" @click="handleLeftClick" :style="{ width: leftWidth + 'px' }">
				<view v-if="showBack" class="back-button">
					<image class="back-icon" src="/static/icons/back-arrow.svg" mode="aspectFit"></image>
				</view>
				<text v-if="leftText" class="left-text">{{ leftText }}</text>
			</view>
			
			<!-- 中间区域 -->
			<view class="navbar-center">
				<text v-if="title" class="navbar-title">{{ title }}</text>
			</view>
			
			<!-- 右侧区域（占位） -->
			<view class="navbar-right" :style="{ width: rightWidth + 'px' }">
				<slot name="right"></slot>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CustomNavBar',
	props: {
		// 标题文本
		title: {
			type: String,
			default: ''
		},
		// 左侧文本
		leftText: {
			type: String,
			default: ''
		},
		// 是否显示返回按钮
		showBack: {
			type: Boolean,
			default: true
		},
		// 背景色（支持透明）
		backgroundColor: {
			type: String,
			default: 'transparent'
		},
		// 文字颜色
		textColor: {
			type: String,
			default: '#000000'
		},
		// 默认导航栏高度（rpx）
		defaultNavBarHeight: {
			type: Number,
			default: 88
		}
	},
	data() {
		return {
			statusBarHeight: 0,    // 状态栏高度
			navBarHeight: 44,      // 导航栏高度
			totalHeight: 0,        // 总高度
			menuButtonRect: {},    // 胶囊按钮信息
			leftWidth: 60,         // 左侧区域宽度
			rightWidth: 60,        // 右侧区域宽度
			isFirstPage: false     // 是否为首页
		}
	},
	mounted() {
		this.getRectInfo();
		this.checkFirstPage();
	},
	methods: {
		// 获取系统信息和胶囊信息
		getRectInfo() {
			// 获取状态栏高度 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
			const sysInfo = uni.getSystemInfoSync();
			this.statusBarHeight = sysInfo.statusBarHeight || 0;
			
			// 默认导航栏高度（转换rpx到px）
			this.navBarHeight = uni.upx2px(this.defaultNavBarHeight);
			
			// #ifndef APP || H5
			// 判断获取微信小程序胶囊API是否可用 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
			if (uni.canIUse('getMenuButtonBoundingClientRect')) {
				// 获取微信小程序胶囊布局位置信息 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
				this.menuButtonRect = uni.getMenuButtonBoundingClientRect();
				
				// (胶囊上部高度-状态栏高度)*2 + 胶囊高度 = 导航栏高度（不包含状态栏） <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
				// 以此保证胶囊位于中间位置，多机型适配
				this.navBarHeight = (this.menuButtonRect.top - this.statusBarHeight) * 2 + this.menuButtonRect.height;
				
				// 设置左右区域宽度与胶囊宽度一致 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
				this.leftWidth = this.menuButtonRect.width;
				this.rightWidth = this.menuButtonRect.width;
			}
			// #endif
			
			// 状态栏高度 + 导航栏高度 = 自定义导航栏高度总和 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
			this.totalHeight = this.statusBarHeight + this.navBarHeight;
		},
		
		// 检查当前页是否首页 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
		checkFirstPage() {
			const pages = getCurrentPages();
			this.isFirstPage = pages.length === 1;
		},
		
		handleLeftClick() {
			if (this.showBack) {
				// 发送返回事件
				this.$emit('back');
				// 默认行为：返回上一页
				if (!this.isFirstPage) {
					uni.navigateBack({
						delta: 1
					});
				} else {
					// 如果是首页，可以跳转到主页或其他逻辑
					this.$emit('home');
				}
			} else {
				// 发送左侧点击事件
				this.$emit('leftClick');
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.custom-navbar {
	position: relative; /* 改为相对定位，避免与状态栏冲突 */
	top: 0;
	left: 0;
	right: 0;
	z-index: 9999;
	background: transparent;
	width: 100%;
}

.status-bar {
	width: 100%;
	background: transparent;
}

.navbar-content {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 32rpx;
	position: relative;
	width: 100%;
}

.navbar-left {
	display: flex;
	align-items: center;
	justify-content: flex-start;
	height: 100%;
	flex-shrink: 0; /* 防止收缩 */
}

.back-button {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	// background: rgba(255, 255, 255, 0.8);
	// backdrop-filter: blur(10rpx);
	// box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.back-icon {
	width: 32rpx;
	height: 32rpx;
}

.left-text {
	font-size: 32rpx;
	color: #000;
	margin-left: 16rpx;
	font-weight: 500;
	line-height: 1;
}

.navbar-center {
	position: absolute;
	left: 50%;
	top: 50%;
	transform: translate(-50%, -50%);
	display: flex;
	align-items: center;
	justify-content: center;
	height: 100%;
	max-width: 60%; /* 限制最大宽度，避免与左右区域重叠 */
}

.navbar-title {
	font-size: 32rpx;
	// font-weight: 600;
	color: #000;
	text-align: center;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	line-height: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	transform: translateX(-15px);
}

.navbar-right {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	height: 100%;
	flex-shrink: 0; /* 防止收缩 */
}

// 响应式适配
@media (max-width: 375px) {
	.navbar-content {
		padding: 0 24rpx;
	}
	
	.navbar-title {
		font-size: 32rpx;
	}
	
	.left-text {
		font-size: 28rpx;
	}
	
	.back-button {
		width: 56rpx;
		height: 56rpx;
	}
	
	.back-icon {
		width: 28rpx;
		height: 28rpx;
	}
}

@media (min-width: 768px) {
	.navbar-content {
		padding: 0 48rpx;
	}
	
	.navbar-title {
		font-size: 40rpx;
	}
	
	.left-text {
		font-size: 36rpx;
	}
	
	.back-button {
		width: 70rpx;
		height: 70rpx;
	}
	
	.back-icon {
		width: 36rpx;
		height: 36rpx;
	}
}
</style>
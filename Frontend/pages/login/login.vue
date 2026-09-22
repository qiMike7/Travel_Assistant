<template>
	<view class="login-page">
		<CustomNavBar :showBack="true" title="账号登录" @back="handleBack" />

		<view class="brand">
			<text class="brand-title">智趣AI旅行助手</text>
			<text class="brand-sub">登录后即可同步你的偏好、订单与旅行相册</text>
		</view>

		<view class="form">
			<view class="tab-row">
				<text class="tab" :class="{ active: mode === 'login' }" @tap="mode = 'login'">登录</text>
				<text class="tab" :class="{ active: mode === 'register' }" @tap="mode = 'register'">注册</text>
			</view>

			<view class="field">
				<text class="label">账号</text>
				<input class="input" v-model="form.username" placeholder="请输入账号" />
			</view>
			<view class="field">
				<text class="label">密码</text>
				<input class="input" v-model="form.password" password placeholder="请输入密码（至少6位）" />
			</view>
			<view class="field" v-if="mode === 'register'">
				<text class="label">昵称</text>
				<input class="input" v-model="form.nickname" placeholder="选填，默认使用账号" />
			</view>

			<button class="submit" :loading="loading" @tap="submit">{{ mode === 'login' ? '登 录' : '注 册' }}</button>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { authApi } from '@/common/api/index.js'
import { setToken, setUser } from '@/common/utils/auth.js'

const mode = ref('login')
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '' })

const handleBack = () => {
	uni.navigateBack({ delta: 1 })
}

const submit = async () => {
	if (!form.username.trim() || !form.password.trim()) {
		uni.showToast({ title: '请填写账号和密码', icon: 'none' })
		return
	}
	loading.value = true
	try {
		const payload = mode.value === 'login'
			? { username: form.username.trim(), password: form.password }
			: { username: form.username.trim(), password: form.password, nickname: form.nickname.trim() }
		const res = await (mode.value === 'login' ? authApi.login(payload) : authApi.register(payload))
		setToken(res.token)
		setUser(res.user)
		uni.showToast({ title: mode.value === 'login' ? '登录成功' : '注册成功', icon: 'success' })
		setTimeout(() => {
			// 登录成功后返回来源页；无历史则回首页
			const pages = getCurrentPages()
			if (pages.length > 1) {
				uni.navigateBack({ delta: 1 })
			} else {
				uni.switchTab({ url: '/pages/index/index' })
			}
		}, 800)
	} catch (e) {
		// 错误提示已在请求封装中统一处理
	} finally {
		loading.value = false
	}
}
</script>

<style lang="scss" scoped>
.login-page {
	min-height: 100vh;
	background: linear-gradient(180deg, rgba(79, 82, 255, 0.12) 0%, rgba(79, 82, 255, 0) 60%), #f8f9fa;
	padding: 0 60rpx;
}

.brand {
	margin-top: 80rpx;
	margin-bottom: 60rpx;
	display: flex;
	flex-direction: column;
}

.brand-title {
	font-size: 52rpx;
	font-weight: bold;
	color: #003DF5;
}

.brand-sub {
	font-size: 26rpx;
	color: #666;
	margin-top: 16rpx;
}

.form {
	background: #fff;
	border-radius: 24rpx;
	padding: 40rpx 36rpx;
	box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.06);
}

.tab-row {
	display: flex;
	margin-bottom: 30rpx;
}

.tab {
	flex: 1;
	text-align: center;
	font-size: 32rpx;
	color: #999;
	padding-bottom: 16rpx;
	border-bottom: 4rpx solid transparent;
}

.tab.active {
	color: #003DF5;
	font-weight: bold;
	border-bottom-color: #003DF5;
}

.field {
	margin-bottom: 26rpx;
}

.label {
	font-size: 26rpx;
	color: #666;
	display: block;
	margin-bottom: 10rpx;
}

.input {
	border: 1rpx solid #e5e5e5;
	border-radius: 14rpx;
	padding: 20rpx 24rpx;
	font-size: 30rpx;
}

.submit {
	margin-top: 20rpx;
	background: #003DF5;
	color: #fff;
	border-radius: 44rpx;
	font-size: 32rpx;
}
</style>

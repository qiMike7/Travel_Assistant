<template>
	<view class="content">
		<!-- 带返回按钮的导航栏 -->
		<CustomNavBar :showBack="false" />

		<view class="section top">
			<view class="section-content">
				<view class="section-left" @click="goProfile">
					<image :src="avatarUrl" class="avatar-image"></image>
					<view class="user-info">
						<text class="avatar-name">{{ displayName }}</text>
						<text class="avatar-desc">{{ displayDesc }}</text>
					</view>
				</view>
				<view class="section-right">
					<view class="icon-block">
						<image src="/static/assets/user/icons/identify.png" class="icon-image"></image>
						<image src="/static/assets/user/icons/setting.png" class="icon-image" @click="goSettings()"></image>
					</view>
				</view>
			</view>
		</view>

		<view class="section">
			<view class="section-content go-vip">
				<view class="go-vip-content">
					<text class="go-vip-title">开通服务专享产品权益</text>
				</view>
				<view class="go-vip-button" @click="handleOpenVip">
					<text class="go-vip-button-text">立即开通</text>
				</view>
			</view>
		</view>

		<view class="section ">
			<view class="section-content activity">
				<view class="activity-top">
					<image src="/static/assets/user/icons/会员中心.png" class="activity-image"></image>
					<image src="/static/assets/user/icons/icon_箭头.png" class="activity-nav" ></image>
				</view>
				<view class="activity-bottom" @click="goShare()">
					<text>限时活动</text>
					<text>分享给好友即可免费使用12小时</text>
				</view>
			</view>
		</view>

		<view class="section vip">
			<view class="section-content vip">
				<view class="vip-title">会员中心</view>
				<view class="vip-content">
					<view>
						<image src="/static/assets/user/icons/share.png" class="vip-image" @click="goShare()"></image>
						分享APP
					</view>
					<view>
						<image src="/static/assets/user/icons/vip.png" class="vip-image" @click="goHuiyuan()"></image>
						会员客服
					</view>
					<view @click="goOrder">
						<image src="/static/assets/user/icons/shopping.png" class="vip-image"></image>
						订单记录
					</view>
					<view>
						<image src="/static/assets/user/icons/tell.png" class="vip-image" @click="goAbout()"></image>
						关于我们
					</view>
				</view>
				<view class="vip-footer">
					<image src="/static/assets/user/icons/联系我们.png" class="vip-image"></image>
					当前会话到期时间：{{ vipExpireText }}
				</view>
			</view>
		</view>

		<view class="section text">
			<view class="section-content subtitle">
				算法为辔，驰骋经纬之间，大漠孤烟之远
				深巷杏花之幽，洞明方位，尽得真趣
			</view>
		</view>

		<!-- Meta Tourism 文本 -->
		<view class="meta-text">
			<text class="meta-label">由 Meta- Tourism 自研文旅垂类语言模型支持</text>
		</view>
	</view>

</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { userApi, payApi } from '@/common/api/index.js'
import { isLogin, getUser, setUser } from '@/common/utils/auth.js'
import { toAbsoluteUrl } from '@/common/utils/request.js'
import { invokeWxPayment } from '@/common/utils/pay.js'

const user = ref(getUser() || {})

const avatarUrl = computed(() => toAbsoluteUrl(user.value.avatar) || '/static/assets/user/pic/avatar.png')
const displayName = computed(() => user.value.nickname || (isLogin() ? '旅行者' : '点击登录'))
const displayDesc = computed(() => user.value.signature || '这个人很懒，什么都没有留下')
const vipExpireText = computed(() => user.value.vipExpireTime || '未开通')

// 每次显示页面时刷新登录用户信息
const loadUser = async () => {
  if (!isLogin()) {
    user.value = {}
    return
  }
  try {
    const u = await userApi.profile()
    user.value = u || {}
    setUser(u)
  } catch (e) {
    // 未登录或异常，保持本地缓存
  }
}

onShow(() => {
  loadUser()
})

// 头像区：未登录去登录，已登录去资料页
const goProfile = () => {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/profile/profile' })
}

// 会员套餐：周卡 ¥10（7天）、月卡 ¥30（30天）
const VIP_PLANS = [
  { label: '周卡 ¥10（7 天）', price: 10, hours: 24 * 7 },
  { label: '月卡 ¥30（30 天）', price: 30, hours: 24 * 30 }
]

// 开通 VIP：选择套餐 -> 微信支付 -> 支付成功后后端赠送对应时长
const handleOpenVip = () => {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.showActionSheet({
    itemList: VIP_PLANS.map(p => p.label),
    success: async ({ tapIndex }) => {
      const plan = VIP_PLANS[tapIndex]
      if (!plan) return
      try {
        // 1. 向后端换取微信支付参数（bizType=vip）
        const payParams = await payApi.wechat({
          bizType: 'vip',
          plan: tapIndex === 0 ? 'week' : 'month',
          amount: plan.price
        })
        // 2. 调起微信支付收银台
        await invokeWxPayment({ ...(payParams || {}), amount: plan.price })
        // 3. 支付成功：按套餐时长开通
        const u = await userApi.grantVip(plan.hours)
        if (u) {
          user.value = u
          setUser(u)
        }
        uni.showToast({ title: '开通成功', icon: 'success' })
      } catch (e) {
        // 支付取消或接口异常，错误提示已统一处理
      }
    }
  })
}

const goSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/settings'
  });
}

const goOrder = () => {
  uni.navigateTo({
    url: '/pages/order/order'
  });
}

const goAbout = () => {
  uni.navigateTo({
    url: '/pages/aboutus/aboutus'
  });
}

const goShare = () => {
  uni.navigateTo({
    url: '/pages/share/share'
  });
}

const goHuiyuan = () => {
  uni.navigateTo({
    url: '/pages/huiyuankefu/huiyuankefu'
  });
}

</script>

<style lang="scss" scoped>
.content {
	min-height: 100vh;
}

.section {
	padding: 0rpx 30rpx;
	height: 140rpx;
	width: 100%;
	box-sizing: border-box;
	margin-bottom: 42rpx;

	&.top {
		height: 200rpx;
		padding: 120rpx 20rpx 100rpx 30rpx;
		background: transparent;
		margin-bottom: 20rpx;
	}

	&.vip {
		height: 320rpx;
		margin-bottom: 30rpx;

	}

	&.text {
		height: 230rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

}

.section-content {
	height: 100%;
	width: 100%;
	background-color: #fff;
	display: flex;
	align-items: center;
	border-radius: 20rpx;
	box-sizing: border-box;

	.top & {
		background: transparent;
		padding: 0;
	}

	&.go-vip {
		background: url('/static/assets/user/pic/开通-bg.png');
		background-size: 100% 100%;
		justify-content: space-between;
		padding: 0 30rpx;
	}

	&.activity {
		border: 1px solid rgba(0, 0, 0, 1);
		border-radius: 30rpx;
		flex-direction: column;
	}

	&.vip {
		flex-direction: column;
		padding: 0;
		align-items: center;
		border: 1px solid rgba(0, 0, 0, 1);
		justify-content: center;
	}
}

// 会员中心样式
.vip-title {
	font-size: 32rpx;
	color: #333;
	margin-bottom: 30rpx;
	width: 90%;
}

.vip-content {
	display: flex;
	justify-content: space-between;
	width: 90%;
	margin-bottom: 40rpx;

	>view {
		display: flex;
		flex-direction: column;
		align-items: center;
		font-size: 20rpx;
		color: #666;
	}
}

.vip-image {
	width: 50rpx;
	height: 50rpx;
	margin-bottom: 6rpx;
}

.vip-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-top: 2px solid rgba(0, 0, 0, 1);
	font-size: 26rpx;
	height: 80rpx;
	color: rgba(18, 24, 54, 1);
	width: 90%;

	.vip-image {
		width: 50rpx;
		height: 50rpx;
		margin-right: 6rpx;
		margin-bottom: 0;
	}
}

.go-vip-content {
	flex: 1;
}

.go-vip-title {
	font-size: 28rpx;
	color: rgba(227, 195, 119, 1);
	line-height: 1.2;
}

.go-vip-button {
	background: rgba(227, 195, 119, 1);
	box-shadow: 1.33px 1.33px 5.33px rgba(0, 11, 222, 0.24);
	border-radius: 25rpx;
	padding: 20rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.go-vip-button-text {
	font-size: 26rpx;
	color: #ffffff;
	text-align: center;
}

// 顶部用户信息区域样式
.section-left {
	display: flex;
	align-items: center;
	flex: 1;
	font-family: 'CustomFont', sans-serif;
}

.avatar-image {
	width: 130rpx;
	height: 130rpx;
	border-radius: 50%;
	margin-right: 20rpx;
	border: 3rpx solid rgba(255, 255, 255, 0.8);
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.user-info {
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.avatar-name {
	font-size: 24px;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
	line-height: 1.2;
}

.avatar-desc {
	font-size: 24rpx;
	color: #999;
	line-height: 1.2;
	font-family: 'JapaneseFont', 'CustomFont', sans-serif;
}

.section-right {
	display: flex;
	align-items: center;
	margin-right: 3%;
}

.icon-block {
	display: flex;
	align-items: center;
	background: rgba(255, 255, 255, 0.9);
	border-radius: 50rpx;
	padding: 15rpx 25rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.icon-image {
	width: 40rpx;
	height: 40rpx;
	margin-left: 15rpx;

	&:first-child {
		margin-left: 0;
	}
}

.activity-top {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 90%;
	height: 50rpx;
	padding-top: 22rpx;

	image {
		width: 45rpx;
		height: 45rpx;

		&.activity-nav {
			width: 50rpx;
			height: 50rpx;
			transform: translate(15rpx, 7rpx);
		}
	}
}

.activity-bottom {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 90%;
	height: 50rpx;

	text {
		font-size: 26rpx;
		font-family: 'CustomFont', 'JapaneseFont', sans-serif;
		font-weight: 400;
		color: rgba(18, 24, 54, 1);

		&:first-child {
			font-size: 30rpx;
		}
	}
}

.subtitle {
	width: 100%;
	font-size: 38rpx;
	color: #666;
	text-align: center;
	line-height: 1.6;
	font-family: 'JapaneseFont', 'CustomFont', sans-serif;
	display: flex;
	justify-content: center;
	align-items: center;
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
</style>

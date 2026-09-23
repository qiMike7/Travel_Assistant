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
					<view @click="goPlanHistory()">
						<image src="/static/assets/user/icons/plan-history.svg" class="vip-image"></image>
						规划历史
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

		<!-- 虚拟付款码面板（无法调起真实微信支付时的降级展示） -->
		<view v-if="payCode.visible" class="paycode-mask" @tap.stop="closePayCode">
			<view class="paycode-panel" @tap.stop>
				<text class="paycode-title">微信扫一扫付款</text>
				<text class="paycode-amount">¥{{ payCode.amount }}</text>
				<canvas canvas-id="vipPayQr" id="vipPayQr" class="paycode-qr"></canvas>
				<text class="paycode-order">订单号：{{ payCode.orderNo }}</text>
				<text class="paycode-tip">当前为模拟器/未配置商户号环境，展示的是演示付款码</text>
				<button class="paycode-done" @tap="finishMockPay">我已完成支付</button>
				<text class="paycode-cancel" @tap="closePayCode">取消</text>
			</view>
		</view>
	</view>

</template>

<script setup>
import { ref, computed, reactive, nextTick, getCurrentInstance } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { userApi } from '@/common/api/index.js'
import { isLogin, getUser, setUser } from '@/common/utils/auth.js'
import { toAbsoluteUrl } from '@/common/utils/request.js'

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

// 虚拟付款码面板（与购物下单一致：无法调起真实支付时展示演示付款码）
const instance = getCurrentInstance()
const payCode = reactive({ visible: false, amount: '', orderNo: '' })
let pendingPlan = null

// 开通 VIP：选择套餐 -> 展示虚拟付款码 -> “我已完成支付”后按套餐时长开通
const handleOpenVip = () => {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.showActionSheet({
    itemList: VIP_PLANS.map(p => p.label),
    success: ({ tapIndex }) => {
      const plan = VIP_PLANS[tapIndex]
      if (!plan) return
      pendingPlan = plan
      openPayCode(plan)
    }
  })
}

// 展示付款码面板（当前环境无法调起微信支付时的降级方案）
const openPayCode = (plan) => {
  payCode.amount = Number(plan.price).toFixed(2)
  payCode.orderNo = 'VIP' + Date.now()
  payCode.visible = true
  nextTick(() => drawPayQr(payCode.orderNo))
}

// 本地自绘付款二维码（演示用图案，非真实可扫描）：根据订单号确定性地生成点阵
const drawPayQr = (seedStr) => {
  try {
    const SIZE = 220
    const N = 25
    const CELL = Math.floor(SIZE / (N + 2))
    const OFF = Math.floor((SIZE - CELL * N) / 2)
    const ctx = uni.createCanvasContext('vipPayQr', instance.proxy)
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(0, 0, SIZE, SIZE)
    // 字符串哈希，保证同一订单号图案一致
    let h = 2166136261
    for (let i = 0; i < seedStr.length; i++) {
      h ^= seedStr.charCodeAt(i)
      h = Math.imul(h, 16777619)
    }
    let seed = h >>> 0
    const rand = () => {
      seed = (Math.imul(seed, 1103515245) + 12345) & 0x7fffffff
      return seed / 0x7fffffff
    }
    const inFinder = (x, y) =>
      (x < 8 && y < 8) || (x >= N - 8 && y < 8) || (x < 8 && y >= N - 8)
    ctx.setFillStyle('#111111')
    for (let y = 0; y < N; y++) {
      for (let x = 0; x < N; x++) {
        if (inFinder(x, y)) continue
        if (rand() > 0.5) {
          ctx.fillRect(OFF + x * CELL, OFF + y * CELL, CELL, CELL)
        }
      }
    }
    // 三个定位角
    const finder = (fx, fy) => {
      const px = OFF + fx * CELL
      const py = OFF + fy * CELL
      ctx.setFillStyle('#111111')
      ctx.fillRect(px, py, CELL * 7, CELL * 7)
      ctx.setFillStyle('#ffffff')
      ctx.fillRect(px + CELL, py + CELL, CELL * 5, CELL * 5)
      ctx.setFillStyle('#111111')
      ctx.fillRect(px + CELL * 2, py + CELL * 2, CELL * 3, CELL * 3)
    }
    finder(1, 1)
    finder(N - 8, 1)
    finder(1, N - 8)
    ctx.draw()
  } catch (e) {
    // 绘图失败不阻断付款码展示
  }
}

// 关闭付款码
const closePayCode = () => {
  payCode.visible = false
}

// “我已完成支付”：按套餐时长开通 VIP
const finishMockPay = async () => {
  payCode.visible = false
  if (!pendingPlan) return
  try {
    const u = await userApi.grantVip(pendingPlan.hours)
    if (u) {
      user.value = u
      setUser(u)
    }
    uni.showToast({ title: '开通成功', icon: 'success' })
  } catch (e) {
    // 接口异常，错误提示已统一处理
  } finally {
    pendingPlan = null
  }
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

// 规划历史：首页智能规划的保存记录（非会员 1 份 / 会员 5 份，删除可释放名额）
const goPlanHistory = () => {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({
    url: '/pages/plan-history/plan-history'
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

/* 虚拟付款码面板 */
.paycode-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.55);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}

.paycode-panel {
	width: 560rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 48rpx 40rpx 36rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.paycode-title {
	font-size: 32rpx;
	color: #333;
	font-weight: bold;
}

.paycode-amount {
	font-size: 48rpx;
	color: #ff4444;
	font-weight: bold;
	margin: 16rpx 0 24rpx;
}

.paycode-qr {
	width: 220px;
	height: 220px;
	background: #fff;
	border: 1rpx solid #eee;
	border-radius: 12rpx;
}

.paycode-order {
	font-size: 24rpx;
	color: #666;
	margin-top: 20rpx;
}

.paycode-tip {
	font-size: 22rpx;
	color: #999;
	margin-top: 10rpx;
	text-align: center;
	line-height: 1.5;
}

.paycode-done {
	margin-top: 30rpx;
	width: 100%;
	background: #07c160;
	color: #fff;
	font-size: 30rpx;
	border-radius: 44rpx;
	height: 80rpx;
	line-height: 80rpx;
}

.paycode-cancel {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #999;
}
</style>

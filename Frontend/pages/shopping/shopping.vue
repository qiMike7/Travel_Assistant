<template>
	<view class="container">
		<CustomNavBar :showBack="true" title="旅行攻略规划" @back="handleBack" />

		<scroll-view scroll-y="true" class="page-body">
			<!-- 顶部说明 -->
			<view class="hero">
				<text class="hero-title">快速智慧旅行攻略规划</text>
				<text class="hero-sub">告诉小趣你想去哪儿，一键生成专属分日行程</text>
			</view>

			<!-- 输入表单 -->
			<view class="form-card">
				<view class="field">
					<text class="label">目的地</text>
					<input class="input" v-model="form.destination" placeholder="城市或景区，例如：杭州 / 故宫" />
				</view>

				<view class="field-row">
					<view class="field half">
						<text class="label">行程天数</text>
						<view class="stepper">
							<view class="step-btn" @click="changeDays(-1)">－</view>
							<text class="step-val">{{ form.days }}</text>
							<view class="step-btn" @click="changeDays(1)">＋</view>
						</view>
					</view>
					<view class="field half">
						<text class="label">出行人数</text>
						<view class="stepper">
							<view class="step-btn" @click="changeTravelers(-1)">－</view>
							<text class="step-val">{{ form.travelers }}</text>
							<view class="step-btn" @click="changeTravelers(1)">＋</view>
						</view>
					</view>
				</view>

				<view class="field">
					<text class="label">出行风格</text>
					<view class="tag-group">
						<view
							v-for="s in styleOptions"
							:key="s"
							class="tag"
							:class="{ active: form.style === s }"
							@click="form.style = form.style === s ? '' : s"
						>{{ s }}</view>
					</view>
				</view>

				<view class="field">
					<text class="label">每日人均预算（元）</text>
					<input class="input" type="number" v-model="form.budget" placeholder="选填，例如：500" />
				</view>

				<view class="generate-btn" :class="{ disabled: loading || !form.destination.trim() }" @click="generate">
					<text v-if="!loading">🧭 生成专属攻略</text>
					<text v-else>小趣正在规划中...</text>
				</view>
			</view>

			<!-- 加载态 -->
			<view v-if="loading" class="loading-box">
				<view class="loading-dots">
					<view class="dot"></view>
					<view class="dot" style="animation-delay: 150ms"></view>
					<view class="dot" style="animation-delay: 300ms"></view>
				</view>
				<text class="loading-text">正在为你规划 {{ form.destination }} 的 {{ form.days }} 天行程...</text>
			</view>

			<!-- 初始引导 -->
			<view v-else-if="!result" class="empty-tip">
				<text>还没有攻略，填写目的地后点击「生成专属攻略」试试～</text>
			</view>

			<!-- 攻略结果 -->
			<block v-if="!loading && result">
				<!-- 概览 -->
				<view class="result-card">
					<view class="result-head">
						<text class="result-dest">{{ result.destination }}</text>
						<text class="result-days">{{ result.days }} 天行程</text>
					</view>
					<text class="result-summary" v-if="result.summary">{{ result.summary }}</text>
					<view class="result-meta" v-if="result.totalBudget">
						<text class="meta-item">人均预估：¥{{ result.totalBudget }}</text>
					</view>
				</view>

				<!-- 保存到规划历史（可选，不保存则不入历史） -->
				<view class="save-card">
					<view v-if="result.id" class="saved-tip">✓ 已保存到规划历史</view>
					<block v-else>
						<view class="save-btn" :class="{ disabled: saving }" @click="savePlan">
							{{ saving ? '保存中...' : '💾 保存到规划历史' }}
						</view>
						<text class="save-hint">{{ isVip ? '会员最多可保存 5 份攻略，删除可释放名额' : '非会员可保存 1 份攻略，开通会员可扩展至 5 份' }}</text>
					</block>
				</view>

				<!-- 地图（有有效坐标时展示） -->
				<view v-if="hasMap" class="map-card">
					<text class="section-title">路线轨迹</text>
					<map
						class="map"
						:latitude="mapCenter.latitude"
						:longitude="mapCenter.longitude"
						:scale="12"
						:markers="markers"
						:polyline="polylines"
						show-location="false"
					></map>
				</view>

				<!-- 分日行程 -->
				<view class="day-card" v-for="(day, di) in result.plan" :key="di">
					<view class="day-head">
						<view class="day-badge">D{{ day.day || (di + 1) }}</view>
						<text class="day-title">{{ day.title || ('第 ' + (di + 1) + ' 天') }}</text>
					</view>
					<view class="spot" v-for="(spot, si) in day.spots" :key="si">
						<view class="spot-line">
							<view class="spot-dot" :class="dotClass(spot.category)"></view>
							<view class="spot-connector" v-if="si < day.spots.length - 1"></view>
						</view>
						<view class="spot-body">
							<view class="spot-top">
								<text class="spot-time" v-if="spot.time">{{ spot.time }}</text>
								<text class="spot-cat" :class="dotClass(spot.category)" v-if="spot.category">{{ spot.category }}</text>
							</view>
							<text class="spot-name">{{ spot.name }}</text>
							<text class="spot-desc" v-if="spot.description">{{ spot.description }}</text>
							<view class="spot-tip" v-if="spot.tip">
								<text>💡 {{ spot.tip }}</text>
							</view>
						</view>
					</view>
				</view>

				<view class="regen-btn" @click="resetForm">↻ 换个方案重新规划</view>
			</block>

			<view class="bottom-space"></view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { planApi } from '@/common/api/index.js'
import { getUser } from '@/common/utils/auth.js'

const handleBack = () => {
	uni.navigateBack({ delta: 1 })
}

const styleOptions = ['休闲度假', '人文历史', '自然风光', '美食探店', '亲子遛娃', '摄影打卡']

const form = ref({
	destination: '',
	days: 3,
	travelers: 2,
	style: '',
	budget: ''
})

const loading = ref(false)
const saving = ref(false)
const result = ref(null)

const isVip = computed(() => !!(getUser() || {}).vip)

const changeDays = (delta) => {
	const v = (form.value.days || 1) + delta
	form.value.days = Math.min(15, Math.max(1, v))
}
const changeTravelers = (delta) => {
	const v = (form.value.travelers || 1) + delta
	form.value.travelers = Math.min(20, Math.max(1, v))
}

// 收集所有带有效坐标的行程点，用于地图 marker 与折线
const coordPoints = computed(() => {
	const pts = []
	if (!result.value || !result.value.plan) return pts
	result.value.plan.forEach((day) => {
		(day.spots || []).forEach((spot) => {
			if (isValidCoord(spot.latitude, spot.longitude)) {
				pts.push({
					latitude: Number(spot.latitude),
					longitude: Number(spot.longitude),
					name: spot.name || ''
				})
			}
		})
	})
	return pts
})

const hasMap = computed(() => coordPoints.value.length > 0)

const mapCenter = computed(() => {
	const pts = coordPoints.value
	if (!pts.length) return { latitude: 39.9, longitude: 116.4 }
	const sum = pts.reduce((acc, p) => {
		acc.latitude += p.latitude
		acc.longitude += p.longitude
		return acc
	}, { latitude: 0, longitude: 0 })
	return { latitude: sum.latitude / pts.length, longitude: sum.longitude / pts.length }
})

const markers = computed(() =>
	coordPoints.value.map((p, i) => ({
		id: i,
		latitude: p.latitude,
		longitude: p.longitude,
		width: 28,
		height: 28,
		callout: {
			content: p.name || ('第' + (i + 1) + '站'),
			color: '#333333',
			fontSize: 11,
			borderRadius: 8,
			bgcolor: '#ffffff',
			padding: 6,
			display: 'BYCLICK'
		}
	}))
)

const polylines = computed(() => {
	const pts = coordPoints.value
	if (pts.length < 2) return []
	return [
		{
			points: pts.map((p) => ({ latitude: p.latitude, longitude: p.longitude })),
			color: '#4F52FFAA',
			width: 6,
			arrowLine: true
		}
	]
})

function isValidCoord(lat, lng) {
	const a = Number(lat)
	const b = Number(lng)
	return Number.isFinite(a) && Number.isFinite(b) && a !== 0 && b !== 0 && Math.abs(a) <= 90 && Math.abs(b) <= 180
}

function dotClass(category) {
	if (!category) return 'cat-default'
	if (category.indexOf('吃') >= 0 || category.indexOf('食') >= 0) return 'cat-food'
	if (category.indexOf('住') >= 0) return 'cat-stay'
	if (category.indexOf('景') >= 0 || category.indexOf('游') >= 0) return 'cat-spot'
	if (category.indexOf('交通') >= 0) return 'cat-traffic'
	return 'cat-default'
}

// 保存名额已满（4293）：引导去规划历史删除或开通会员
// 注：微信小程序 showModal 按钮文案限 4 个汉字，超出会导致整个弹窗调用静默失败
const showPlanLimitModal = () => {
	console.warn('[名额引导] showModal 即将调用, isVip=', isVip.value)
	uni.showModal({
		title: '保存名额已满',
		content: isVip.value
			? '最多可保存 5 份攻略，请前往规划历史删除旧攻略释放名额'
			: '非会员仅可保存 1 份攻略，开通会员可扩展至 5 份，或删除旧攻略释放名额',
		confirmText: '管理历史',
		cancelText: isVip.value ? '知道了' : '开通会员',
		success: (res) => {
			console.warn('[名额引导] showModal success:', JSON.stringify(res))
			if (res.confirm) {
				uni.navigateTo({ url: '/pages/plan-history/plan-history' })
			} else if (res.cancel && !isVip.value) {
				uni.switchTab({ url: '/pages/user/user' })
			}
		},
		fail: (err) => {
			console.error('[名额引导] showModal fail:', err && err.errMsg)
			// 弹窗失败时降级为长 toast，保证用户至少能看到引导
			uni.showToast({ title: '名额已满：请到规划历史删除或开通会员', icon: 'none', duration: 3000 })
		}
	})
}

const generate = async () => {
	if (loading.value) return
	if (!form.value.destination.trim()) {
		uni.showToast({ title: '请先填写目的地', icon: 'none' })
		return
	}
	const payload = {
		destination: form.value.destination.trim(),
		days: form.value.days,
		travelers: form.value.travelers
	}
	if (form.value.style) payload.style = form.value.style
	const budget = parseInt(form.value.budget, 10)
	if (!isNaN(budget)) payload.budget = budget

	loading.value = true
	result.value = null
	try {
		// 仅生成不保存：是否存入历史由用户在结果页点击「保存到规划历史」决定
		const res = await planApi.generate(payload)
		result.value = res
	} catch (e) {
		// 兼容旧版后端「生成即保存」的 4293：同样弹引导窗，避免完全静默无反应
		console.warn('[generate] 失败对象:', JSON.stringify(e))
		if (Number(e && e.code) === 4293) {
			showPlanLimitModal()
		} else if (e && e.code) {
			console.warn('攻略生成失败:', e.code, e.message)
		}
	} finally {
		loading.value = false
	}
}

// 保存当前攻略到规划历史（名额在此步校验，满额 4293 引导删除历史或开通会员）
const savePlan = async () => {
	if (saving.value || !result.value || result.value.id) return
	saving.value = true
	try {
		// 本地预检名额：不依赖后端 4293，满额时立即弹引导窗（后端未升级/未重启时也能有反应）
		try {
			const saved = await planApi.list()
			console.warn('[savePlan] 预检 list 返回条数=', saved && saved.length, '本地限额=', isVip.value ? 5 : 1)
			if (saved && saved.length >= (isVip.value ? 5 : 1)) {
				showPlanLimitModal()
				return
			}
		} catch (e) {
			console.warn('[savePlan] 预检 list 失败，转由后端校验:', JSON.stringify(e))
		}
		const res = await planApi.save(result.value)
		if (res && res.id) {
			result.value = { ...result.value, id: res.id }
		}
		uni.showToast({ title: '已保存', icon: 'success' })
	} catch (e) {
		// 保存名额已满（4293）：引导删除历史或开通会员
		console.warn('[savePlan] 保存失败对象:', JSON.stringify(e))
		if (Number(e && e.code) === 4293) {
			showPlanLimitModal()
		} else if (e && e.code) {
			console.warn('攻略保存失败:', e.code, e.message)
		}
		// 其他错误提示已在请求封装中统一处理
	} finally {
		saving.value = false
	}
}

const resetForm = () => {
	result.value = null
}

// 从规划历史进入：直接展示已保存的攻略
const loadPlan = async (id) => {
	loading.value = true
	try {
		const res = await planApi.detail(id)
		if (res) {
			result.value = res
		}
	} catch (e) {
		// 错误提示已在请求封装中统一处理
	} finally {
		loading.value = false
	}
}

onLoad((opt) => {
	if (opt && opt.planId) {
		loadPlan(opt.planId)
	}
})
</script>

<style lang="scss" scoped>
page {
	background: linear-gradient(180deg, rgba(79, 82, 255, 0.18) 0.07%, rgba(79, 82, 255, 0) 75.38%);
	min-height: 100vh;
}

.container {
	min-height: 100vh;
	background: transparent;
}

.page-body {
	height: calc(100vh - 88rpx);
	padding: 0 30rpx;
	box-sizing: border-box;
}

.hero {
	padding: 20rpx 6rpx 30rpx;
}
.hero-title {
	display: block;
	font-size: 40rpx;
	font-weight: bold;
	color: #333;
}
.hero-sub {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	color: #888;
}

/* 保存到规划历史 */
.save-card {
	margin-top: 20rpx;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 24rpx 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(79, 82, 255, 0.12);
	display: flex;
	flex-direction: column;
	align-items: center;
}
.save-btn {
	width: 100%;
	background: linear-gradient(90deg, #4f52ff 0%, #6f72ff 100%);
	color: #fff;
	font-size: 28rpx;
	font-weight: bold;
	text-align: center;
	padding: 20rpx 0;
	border-radius: 16rpx;
}
.save-btn.disabled {
	opacity: 0.6;
}
.saved-tip {
	font-size: 26rpx;
	color: #18a058;
	font-weight: bold;
}
.save-hint {
	margin-top: 12rpx;
	font-size: 22rpx;
	color: #999;
}

/* 表单卡片 */
.form-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(79, 82, 255, 0.12);
}
.field {
	margin-bottom: 24rpx;
}
.field-row {
	display: flex;
	gap: 24rpx;
}
.half {
	flex: 1;
}
.label {
	display: block;
	font-size: 26rpx;
	color: #555;
	margin-bottom: 12rpx;
}
.input {
	background: #f4f6fb;
	border-radius: 14rpx;
	padding: 18rpx 22rpx;
	font-size: 28rpx;
	color: #333;
}
.stepper {
	display: flex;
	align-items: center;
	background: #f4f6fb;
	border-radius: 14rpx;
	padding: 8rpx;
}
.step-btn {
	width: 64rpx;
	height: 64rpx;
	line-height: 64rpx;
	text-align: center;
	font-size: 34rpx;
	color: #4f52ff;
	background: #fff;
	border-radius: 12rpx;
}
.step-val {
	flex: 1;
	text-align: center;
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
}
.tag-group {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}
.tag {
	padding: 12rpx 24rpx;
	border-radius: 30rpx;
	background: #f4f6fb;
	color: #666;
	font-size: 24rpx;
}
.tag.active {
	background: linear-gradient(135deg, #4f52ff 0%, #ff9ccf 100%);
	color: #fff;
}
.generate-btn {
	margin-top: 10rpx;
	background: linear-gradient(135deg, #4f52ff 0%, #ff9ccf 100%);
	color: #fff;
	text-align: center;
	padding: 26rpx 0;
	border-radius: 40rpx;
	font-size: 30rpx;
	font-weight: bold;
	box-shadow: 0 6rpx 20rpx rgba(79, 82, 255, 0.3);
}
.generate-btn.disabled {
	opacity: 0.6;
}

/* 加载 */
.loading-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60rpx 0;
}
.loading-dots {
	display: flex;
	gap: 12rpx;
}
.dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	background: #4f52ff;
	animation: bounce 1s infinite ease-in-out;
}
@keyframes bounce {
	0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
	40% { transform: scale(1); opacity: 1; }
}
.loading-text {
	margin-top: 24rpx;
	font-size: 26rpx;
	color: #888;
}

.empty-tip {
	text-align: center;
	padding: 60rpx 30rpx;
	color: #999;
	font-size: 26rpx;
}

/* 结果概览 */
.result-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 30rpx;
	margin-top: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(79, 82, 255, 0.12);
}
.result-head {
	display: flex;
	align-items: baseline;
	gap: 16rpx;
}
.result-dest {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}
.result-days {
	font-size: 24rpx;
	color: #4f52ff;
}
.result-summary {
	display: block;
	margin-top: 16rpx;
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}
.result-meta {
	margin-top: 16rpx;
}
.meta-item {
	font-size: 24rpx;
	color: #ff4757;
}

/* 地图 */
.map-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 24rpx;
	margin-top: 24rpx;
	box-shadow: 0 8rpx 32rpx rgba(79, 82, 255, 0.12);
}
.section-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 16rpx;
	display: block;
}
.map {
	width: 100%;
	height: 460rpx;
	border-radius: 16rpx;
}

/* 分日行程 */
.day-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 30rpx;
	margin-top: 24rpx;
	box-shadow: 0 8rpx 32rpx rgba(79, 82, 255, 0.12);
}
.day-head {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 24rpx;
}
.day-badge {
	background: linear-gradient(135deg, #4f52ff 0%, #7a7dff 100%);
	color: #fff;
	font-size: 24rpx;
	font-weight: bold;
	padding: 8rpx 18rpx;
	border-radius: 14rpx;
}
.day-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.spot {
	display: flex;
}
.spot-line {
	width: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}
.spot-dot {
	width: 20rpx;
	height: 20rpx;
	border-radius: 50%;
	margin-top: 8rpx;
	background: #4f52ff;
	flex-shrink: 0;
}
.spot-dot.cat-food { background: #ff8a4c; }
.spot-dot.cat-stay { background: #9b6cff; }
.spot-dot.cat-spot { background: #34c3a0; }
.spot-dot.cat-traffic { background: #4fa9ff; }
.spot-dot.cat-default { background: #4f52ff; }
.spot-connector {
	flex: 1;
	width: 2rpx;
	background: #e2e5f0;
	margin: 6rpx 0;
}
.spot-body {
	flex: 1;
	padding-bottom: 28rpx;
}
.spot-top {
	display: flex;
	align-items: center;
	gap: 14rpx;
}
.spot-time {
	font-size: 24rpx;
	color: #4f52ff;
	font-weight: bold;
}
.spot-cat {
	font-size: 20rpx;
	color: #fff;
	padding: 2rpx 12rpx;
	border-radius: 20rpx;
	background: #4f52ff;
}
.spot-cat.cat-food { background: #ff8a4c; }
.spot-cat.cat-stay { background: #9b6cff; }
.spot-cat.cat-spot { background: #34c3a0; }
.spot-cat.cat-traffic { background: #4fa9ff; }
.spot-cat.cat-default { background: #4f52ff; }
.spot-name {
	display: block;
	margin-top: 8rpx;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}
.spot-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}
.spot-tip {
	margin-top: 10rpx;
	background: #f4f6fb;
	border-radius: 12rpx;
	padding: 12rpx 16rpx;
	font-size: 24rpx;
	color: #888;
}

.regen-btn {
	margin-top: 30rpx;
	text-align: center;
	padding: 24rpx 0;
	border-radius: 40rpx;
	background: #fff;
	color: #4f52ff;
	font-size: 28rpx;
	font-weight: bold;
	border: 2rpx solid #d8dbff;
}

.bottom-space {
	height: 60rpx;
}
</style>

<template>
  <view class="history-page">
    <CustomNavBar :showBack="true" title="规划历史" @back="handleBack" />

    <!-- 额度概览 -->
    <view class="quota-bar">
      <text class="quota-text">已保存 {{ plans.length }} / {{ saveLimit }} 份</text>
      <text v-if="!isVip" class="quota-open" @click="goOpenVip">开通会员可扩展至 5 份 ></text>
    </view>

    <scroll-view class="plan-list" scroll-y="true">
      <!-- 空状态 -->
      <view class="empty" v-if="!loading && plans.length === 0">
        <text class="empty-title">还没有保存的规划</text>
        <text class="empty-desc">在首页「智能规划」生成攻略后，点击「保存到规划历史」即可存入这里</text>
      </view>

      <!-- 攻略卡片 -->
      <view class="plan-card" v-for="item in plans" :key="item.id" @click="viewPlan(item)">
        <view class="plan-main">
          <view class="plan-title-row">
            <text class="plan-dest">{{ item.destination }}</text>
            <text class="plan-days" v-if="item.days">{{ item.days }} 天行程</text>
          </view>
          <text class="plan-summary" v-if="item.summary">{{ item.summary }}</text>
          <text class="plan-time">{{ item.createTime }}</text>
        </view>
        <view class="plan-del" @click.stop="removePlan(item)">
          <image src="/static/assets/user/icons/清理对话历史.png" class="del-icon"></image>
          <text>删除</text>
        </view>
      </view>

      <view class="bottom-space" v-if="plans.length > 0"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { planApi } from '@/common/api/index.js'
import { getUser } from '@/common/utils/auth.js'

const plans = ref([])
const loading = ref(false)

// 保存上限：非会员 1 份 / 会员 5 份；删除历史可释放名额
const isVip = computed(() => !!(getUser() || {}).vip)
const saveLimit = computed(() => (isVip.value ? 5 : 1))

const handleBack = () => {
  uni.navigateBack({ delta: 1 })
}

const goOpenVip = () => {
  uni.switchTab({ url: '/pages/user/user' })
}

const loadPlans = async () => {
  loading.value = true
  try {
    const res = await planApi.list()
    plans.value = res || []
  } catch (e) {
    // 错误提示已在请求封装中统一处理
  } finally {
    loading.value = false
  }
}

// 查看：跳转智能规划页展示完整攻略
const viewPlan = (item) => {
  uni.navigateTo({ url: '/pages/shopping/shopping?planId=' + item.id })
}

// 删除：释放一个保存名额
const removePlan = (item) => {
  uni.showModal({
    title: '删除规划',
    content: `确定删除「${item.destination}」的攻略吗？删除后可释放 1 个保存名额`,
    confirmText: '删除',
    confirmColor: '#e64340',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await planApi.remove(item.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        loadPlans()
      } catch (e) {
        // 错误提示已在请求封装中统一处理
      }
    }
  })
}

onShow(() => {
  loadPlans()
})
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* 额度条 */
.quota-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 30rpx;
  background-color: #fff7e6;
  border-bottom: 1px solid #ffe7ba;
}
.quota-text {
  font-size: 26rpx;
  color: #ad6800;
}
.quota-open {
  font-size: 24rpx;
  color: #fa8c16;
  font-weight: bold;
}

/* 列表 */
.plan-list {
  flex: 1;
  height: calc(100vh - 220rpx);
  padding: 20rpx 30rpx;
  box-sizing: border-box;
}

.plan-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 26rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}
.plan-main {
  flex: 1;
  overflow: hidden;
}
.plan-title-row {
  display: flex;
  align-items: baseline;
  gap: 14rpx;
  margin-bottom: 10rpx;
}
.plan-dest {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}
.plan-days {
  font-size: 22rpx;
  color: #4f52ff;
}
.plan-summary {
  display: block;
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.plan-time {
  display: block;
  font-size: 22rpx;
  color: #999;
}

/* 删除按钮 */
.plan-del {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10rpx 16rpx;
  margin-left: 16rpx;
  font-size: 22rpx;
  color: #e64340;
}
.del-icon {
  width: 36rpx;
  height: 36rpx;
  margin-bottom: 4rpx;
}

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 180rpx;
}
.empty-title {
  font-size: 30rpx;
  color: #999;
  margin-bottom: 14rpx;
}
.empty-desc {
  font-size: 24rpx;
  color: #bbb;
}

.bottom-space {
  height: 40rpx;
}
</style>

<template>
  <view class="pref-page">
    <!-- 标题 -->
    <view class="header">
      <text class="app-name">智趣AI旅行助手</text>
      <text class="title">偏好设置</text>
    </view>

    <!-- 偏好设置区域 -->
    <scroll-view class="content" scroll-y>

      <!-- 旅行方式 -->
      <view class="section">
        <text class="section-title">旅行方式</text>
        <view class="options">
          <view
            v-for="(mode, index) in travelModes"
            :key="index"
            class="option"
            :class="{ active: preferences.travelMode === mode }"
            @tap="preferences.travelMode = mode"
          >
            {{ mode }}
          </view>
        </view>
      </view>

      <!-- 预算范围 -->
      <view class="section">
        <text class="section-title">预算范围（每日）</text>
        <slider
          :value="preferences.budget"
          min="100"
          max="2000"
          step="50"
          @change="e => preferences.budget = e.detail.value"
        />
        <text class="slider-value">{{ preferences.budget }} 元/天</text>
      </view>

      <!-- 住宿偏好 -->
      <view class="section">
        <text class="section-title">住宿偏好</text>
        <picker
          mode="selector"
          :range="stayOptions"
          @change="e => preferences.stay = stayOptions[e.detail.value]"
        >
          <view class="picker-box">
            {{ preferences.stay || '请选择住宿类型' }}
          </view>
        </picker>
      </view>

      <!-- AI推荐强度 -->
      <view class="section">
        <text class="section-title">AI推荐强度</text>
        <slider
          :value="preferences.aiLevel"
          min="1"
          max="5"
          step="1"
          show-value
          @change="e => preferences.aiLevel = e.detail.value"
        />
        <text class="slider-value">
          {{ aiLevelText(preferences.aiLevel) }}
        </text>
      </view>

      <!-- 是否接收消息通知 -->
      <view class="section switch-section">
        <text class="section-title">接收旅行提醒与推荐</text>
        <switch
          :checked="preferences.notifications"
          @change="e => preferences.notifications = e.detail.value"
        />
      </view>
    </scroll-view>

    <!-- 按钮区域 -->
    <view class="btn-group">
      <button class="save-btn" @tap="savePreferences">保存设置</button>
      <button class="reset-btn" @tap="resetPreferences">恢复默认</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 默认偏好设置
const defaultPreferences = {
  travelMode: '自由行',
  budget: 500,
  stay: '经济型酒店',
  aiLevel: 3,
  notifications: true
}

const preferences = ref({ ...defaultPreferences })

const travelModes = ['自由行', '跟团游', '自驾游', '徒步探险']
const stayOptions = ['经济型酒店', '精品民宿', '高端酒店', '露营/帐篷']

// AI推荐强度说明
function aiLevelText(level) {
  const texts = ['基础推荐', '轻度智能', '标准智能', '深度智能', '完全AI规划']
  return texts[level - 1] || '未知'
}

// 保存偏好设置
function savePreferences() {
  uni.setStorageSync('userPreferences', preferences.value)
  uni.showToast({
    title: '设置已保存',
    icon: 'success'
  })
}

// 恢复默认
function resetPreferences() {
  preferences.value = { ...defaultPreferences }
  uni.showToast({
    title: '已恢复默认设置',
    icon: 'none'
  })
}

// 页面加载时读取本地缓存
onLoad(() => {
  const saved = uni.getStorageSync('userPreferences')
  if (saved) preferences.value = saved
})
</script>

<style scoped>
.pref-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 30rpx;
}

.header {
  text-align: center;
  margin-bottom: 20rpx;
}

.app-name {
  font-size: 40rpx;
  color: #007aff;
  font-weight: bold;
}

.title {
  font-size: 36rpx;
  color: #333;
  margin-top: 10rpx;
}

.content {
  width: 100%;
  max-height: 70vh;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.05);
  overflow-y: scroll;
}

.section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.options {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.option {
  padding: 15rpx 30rpx;
  border-radius: 30rpx;
  border: 1rpx solid #ccc;
  font-size: 28rpx;
  color: #555;
}

.option.active {
  background-color: #007aff;
  color: #fff;
  border-color: #007aff;
}

.slider-value {
  margin-top: 10rpx;
  font-size: 28rpx;
  color: #666;
}

.picker-box {
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333;
  background-color: #f9f9f9;
}

.switch-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 按钮组 */
.btn-group {
  margin-top: 40rpx;
  display: flex;
  gap: 30rpx;
}

.save-btn {
  width: 300rpx;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
}

.reset-btn {
  width: 300rpx;
  height: 80rpx;
  background-color: #ccc;
  color: #333;
  border-radius: 40rpx;
  font-size: 32rpx;
}
</style>

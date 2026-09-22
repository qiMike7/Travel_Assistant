<template>
  <view class="help-page">
    <!-- 标题区 -->
    <view class="header">
      <text class="app-name">智趣AI旅行助手</text>
      <text class="subtitle">让旅行更智能、更有趣！</text>
    </view>

    <!-- 帮助中心标题 -->
    <view class="title">帮助中心</view>

    <!-- 常见问题列表 -->
    <view class="faq-list">
      <view
        class="faq-item"
        v-for="(item, index) in faqList"
        :key="index"
        @tap="toggleFAQ(index)"
      >
        <view class="faq-question">
          <text>{{ item.q }}</text>
          <text class="arrow">{{ item.show ? '▲' : '▼' }}</text>
        </view>
        <view v-if="item.show" class="faq-answer">
          {{ item.a }}
        </view>
      </view>
    </view>

    <!-- 联系客服 -->
    <button class="contact-btn" @tap="contactSupport">
      联系客服
    </button>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const faqList = ref([
  {
    q: '如何创建旅行相册？',
    a: '在“旅行相册”页面点击“添加照片”即可创建，支持上传图片并添加文字说明。',
    show: false
  },
  {
    q: '我的数据会保存在哪里？',
    a: '您的数据仅保存在本地或您授权的云存储中，我们不会上传或泄露任何隐私信息。',
    show: false
  },
  {
    q: 'AI助手可以帮我做什么？',
    a: '智趣AI旅行助手可根据您的目的地、时间和喜好生成智能行程推荐，辅助规划路线与预算。',
    show: false
  },
  {
    q: '如何联系客服？',
    a: '点击页面底部的“联系客服”按钮，即可发送反馈或问题，我们会尽快为您解答。',
    show: false
  }
])

// 展开或收起问题
function toggleFAQ(index) {
  faqList.value[index].show = !faqList.value[index].show
}

// 模拟联系客服
function contactSupport() {
  uni.showModal({
    title: '联系客服',
    content: '请通过微信公众号或小程序内反馈功能联系我们的客服人员。',
    showCancel: false,
    confirmText: '我知道了'
  })
}
</script>

<style scoped>
.help-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx;
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* 顶部标题 */
.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.app-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #007aff;
}

.subtitle {
  font-size: 28rpx;
  color: #555;
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 30rpx;
}

/* FAQ 区域 */
.faq-list {
  width: 100%;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.05);
  padding: 20rpx 30rpx;
}

.faq-item {
  border-bottom: 1rpx solid #eee;
  padding: 20rpx 0;
}

.faq-item:last-child {
  border-bottom: none;
}

.faq-question {
  display: flex;
  justify-content: space-between;
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.arrow {
  font-size: 28rpx;
  color: #999;
}

.faq-answer {
  margin-top: 15rpx;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

/* 联系客服按钮 */
.contact-btn {
  margin-top: 60rpx;
  width: 400rpx;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
}
</style>

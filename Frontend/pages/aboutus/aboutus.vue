<template>
  <view class="about-page">
	<CustomNavBar :showBack="true" title="关于我们" @back="handleBack" />
    <!-- 头部Banner/视觉锤 -->
    <view class="banner">
      <image 
        class="banner-img" 
        src="/static/introduction/banner.png" 
        mode="widthFix"
      ></image>
      <view class="banner-slogan">
        <text class="slogan-main">智在发现，趣享旅途</text>
        <text class="slogan-sub">—— 您的专属中国旅行伙伴</text>
      </view>
    </view>

    <!-- 我们是谁 -->
    <view class="section who-we-are">
      <view class="section-title">
        <text>认识智趣旅行</text>
        <view class="title-line"></view>
      </view>
      
      <view class="who-card">
        <!-- 公司简介 -->
        <view class="card-item">
          <image class="card-icon" src="/static/introduction/logo.png" mode="widthFix"></image>
          <view class="card-content">
            <text class="card-title">智旅科技 & 智趣旅行</text>
            <text class="card-desc">智旅科技，深耕中国旅游市场，致力于以人工智能与科技力量，让每一位旅行者都能轻松定制充满乐趣的中华之旅。“智趣旅行”小程序，就是我们实现这一愿景的核心平台。</text>
          </view>
        </view>
        
        <!-- 品牌理念 -->
        <view class="card-ideas">
          <view class="idea-item">
            
            <view class="idea-text">
              <text class="idea-title">智</text>
              <text class="idea-desc">提供简单易用的智能定制工具，用AI规划可靠顺畅的行程，让您安心享受假期。</text>
            </view>
          </view>
          <view class="idea-item">
           
            <view class="idea-text">
              <text class="idea-title">趣</text>
              <text class="idea-desc">挖掘祖国各地鲜为人知的宝藏玩法，推荐个性化体验，让每一次旅程都充满惊喜与欢乐。</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 我们的故事 -->
    <view class="section our-story">
      <view class="section-title">
        <text>我们的初心</text>
        <view class="title-line"></view>
      </view>
      
      <view class="story-content">
        <image class="story-img" src="/static/introduction/story.png" mode="widthFix"></image>
        <view class="story-text">
          <text class="story-p">在全国各地，我们看到太多游客因行程千篇一律或规划繁琐而错过真正的乐趣。于是，“智趣旅行”诞生了——我们想用AI科技打破常规，让每个人都能轻松定制独一无二、充满趣味的中华探索之旅。</text>
          <text class="story-p">我们深谙这片辽阔土地的魅力所在。从江南水乡到西北大漠，从海滨渔村到雪山秘境，“智趣”连接最地道的资源，只为给您带来最懂你的旅行体验。</text>
        </view>
      </view>
    </view>

    <!-- 我们的优势 -->
    <view class="section our-strengths">
      <view class="section-title">
        <text>选择智趣的理由</text>
        <view class="title-line"></view>
      </view>
      
      <view class="strengths-grid">
        <!-- 优势1 -->
        <view class="strength-card">
          <image class="strength-icon" src="/static/introduction/resource.png" mode="widthFix"></image>
          <text class="strength-title">深度全国资源</text>
          <text class="strength-desc">地道体验，独家合作</text>
        </view>
        
        <!-- 优势2 -->
        <view class="strength-card">
          <image class="strength-icon" src="/static/introduction/engine.png" mode="widthFix"></image>
          <text class="strength-title">智能定制引擎</text>
          <text class="strength-desc">简单几步，生成个性化方案</text>
        </view>
        
        <!-- 优势3 -->
        <view class="strength-card">
          <image class="strength-icon" src="/static/introduction/expert.png" mode="widthFix"></image>
          <text class="strength-title">乐趣体验专家</text>
          <text class="strength-desc">挖掘小众有趣玩法，拒绝平庸</text>
        </view>
        
        <!-- 优势4 -->
        <view class="strength-card">
          <image class="strength-icon" src="/static/introduction/service.png" mode="widthFix"></image>
          <text class="strength-title">贴心全域服务</text>
          <text class="strength-desc">走遍中国，更懂你的需求</text>
        </view>
      </view>
    </view>

    <!-- 联系与合作 -->
    <view class="section contact-us">
      <view class="section-title">
        <text>联系我们</text>
        <view class="title-line"></view>
      </view>
      
      <view class="contact-content">
        <view class="contact-user">
          <text class="contact-title">用户服务</text>
          <view class="contact-methods">
            <button class="contact-btn" hover-class="btn-hover" @click="showServicePhone">
              <image class="method-icon" src="/static/introduction/15电话.png" mode="widthFix"></image>
              <text>客服电话</text>
            </button>
            <button class="contact-btn" hover-class="btn-hover" @click="openQrPopup('在线客服')">
              <image class="method-icon" src="/static/introduction/对话.png" mode="widthFix"></image>
              <text>在线客服</text>
            </button>
          </view>
        </view>
        
        <view class="contact-coop">
          <text class="contact-title">商务合作</text>
          <button class="coop-btn" hover-class="btn-hover" @click="openQrPopup('旅游资源合作')">
            <image class="method-icon" src="/static/introduction/合作.png" mode="widthFix"></image>
            <text>旅游资源合作入口</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 微信二维码弹窗 -->
    <view class="qr-mask" v-if="qrPopupVisible" @click="closeQrPopup">
      <view class="qr-popup" @click.stop>
        <text class="qr-popup-title">{{ qrPopupTitle }}</text>
        <text class="qr-popup-tip">请添加微信好友，长按识别或截图后在微信中扫码</text>
        <image class="qr-popup-img" src="/static/settings/profile/qrcode.png" mode="widthFix"></image>
        <button class="qr-popup-close" hover-class="btn-hover" @click="closeQrPopup">关闭</button>
      </view>
    </view>
  </view>
</template>

<script setup>
// 页面交互逻辑
import { ref } from 'vue'
import CustomNavBar from '@/components/CustomNavBar.vue'

const handleBack = () => {
	uni.navigateBack({
		delta: 1
	});
}

// 客服电话弹窗
const showServicePhone = () => {
	uni.showModal({
		title: '客服电话',
		content: '188888888',
		showCancel: false,
		confirmText: '知道了'
	});
}

// 微信二维码弹窗（模拟添加好友）
const qrPopupVisible = ref(false)
const qrPopupTitle = ref('')

const openQrPopup = (title) => {
	qrPopupTitle.value = title
	qrPopupVisible.value = true
}

const closeQrPopup = () => {
	qrPopupVisible.value = false
}
</script>

<style scoped>
/* 基础样式 */
.about-page {
  background-color: #f9f9f9;
  padding-bottom: 30rpx;
}
.section {
  padding: 40rpx 30rpx;
}
.section-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;
}
.section-title text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}
.title-line {
  width: 80rpx;
  height: 6rpx;
  background-color: #4CAF50; /* 椰林绿作为强调色 */
  border-radius: 3rpx;
}

/* Banner样式 */
.banner {
  position: relative;
  width: 100%;
}
.banner-img {
  width: 100%;
  height: auto;
  border-radius: 0 0 30rpx 30rpx;
}
.banner-slogan {
  position: absolute;
  bottom: 40rpx;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  color: white;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
}
.slogan-main {
  font-size: 44rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
}
.slogan-sub {
  font-size: 28rpx;
}

/* 我们是谁样式 */
.who-we-are {
  background-color: white;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05);
}
.who-card {
  background-image: url('/static/introduction/who.png'); /* 水波纹纹理 */
  background-size: cover;
  background-repeat: no-repeat;
  background-position: bottom;
  padding: 20rpx;
  border-radius: 15rpx;
}
.card-item {
  display: flex;
  margin-bottom: 30rpx;
}
.card-icon {
  width: 80rpx;
  height: 80rpx;
  margin-right: 20rpx;
}
.card-content {
  flex: 1;
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}
.card-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}
.card-ideas {
  display: flex;
  gap: 20rpx;
}
.idea-item {
  flex: 1;
  background-color: rgba(255,255,255,0.8);
  padding: 20rpx;
  border-radius: 15rpx;
  display: flex;
  align-items: flex-start;
}
.idea-icon {
  width: 60rpx;
  height: 60rpx;
  margin-right: 15rpx;
}
.idea-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #FF9800; /* 阳光橙 */
  display: block;
  margin-bottom: 10rpx;
}
.idea-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}

/* 我们的故事样式 */
.our-story {
  background-color: #e8f4f8; /* 浅海蓝背景 */
}
.story-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.story-img {
  width: 100%;
  border-radius: 15rpx;
}
.story-text {
  background-color: white;
  padding: 25rpx;
  border-radius: 15rpx;
}
.story-p {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  display: block;
  margin-bottom: 20rpx;
}
.story-p:last-child {
  margin-bottom: 0;
}

/* 我们的优势样式 */
.our-strengths {
  background-color: white;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05);
}
.strengths-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25rpx;
}
.strength-card {
  background-color: #f0f9ff;
  padding: 25rpx;
  border-radius: 15rpx;
  text-align: center;
}
.strength-icon {
  width: 80rpx;
  height: 80rpx;
  margin: 0 auto 15rpx;
}
.strength-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2196F3; /* 海洋蓝 */
  display: block;
  margin-bottom: 10rpx;
}
.strength-desc {
  font-size: 22rpx;
  color: #666;
}

/* 联系与合作样式 */
.contact-us {
  background-color: white;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05);
}
.contact-content {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}
.contact-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 15rpx;
}
.contact-methods {
  display: flex;
  gap: 20rpx;
}
.contact-btn {
  flex: 1;
  background-color: #4CAF50;
  color: white;
  border-radius: 50rpx;
  padding: 15rpx 0;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.coop-btn {
  width: 100%;
  background-color: #2196F3;
  color: white;
  border-radius: 50rpx;
  padding: 15rpx 0;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.method-icon {
  width: 30rpx;
  height: 30rpx;
  margin-right: 10rpx;
}
.btn-hover {
  opacity: 0.8;
  transform: scale(0.98);
}

/* 微信二维码弹窗样式 */
.qr-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.qr-popup {
  width: 560rpx;
  background-color: white;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.qr-popup-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
}
.qr-popup-tip {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  margin-bottom: 24rpx;
}
.qr-popup-img {
  width: 360rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}
.qr-popup-close {
  width: 300rpx;
  background-color: #4CAF50;
  color: white;
  border-radius: 50rpx;
  padding: 12rpx 0;
  font-size: 28rpx;
}
</style>
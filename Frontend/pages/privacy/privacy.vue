<template>
  <view class="privacy-page">
    <!-- 标题 -->
    <view class="header">
      <text class="app-name">智趣AI旅行助手</text>
      <text class="title">隐私政策</text>
    </view>

    <!-- 正文内容 -->
    <scroll-view class="content" scroll-y>
      <view class="section">
        <text class="section-title">一、我们如何收集和使用您的信息</text>
        <text class="section-text">
          我们仅在提供旅行规划、智能推荐等服务时，收集必要的信息，如位置、行程偏好及使用数据。这些信息仅用于功能实现与体验优化，不会用于任何未经授权的用途。
        </text>
      </view>

      <view class="section">
        <text class="section-title">二、我们如何存储和保护您的信息</text>
        <text class="section-text">
          您的数据将安全地存储在微信云开发或本地设备中，我们采用加密与访问控制措施防止数据泄露、篡改或丢失。
        </text>
      </view>

      <view class="section">
        <text class="section-title">三、我们如何共享您的信息</text>
        <text class="section-text">
          除非获得您的明确同意，或根据法律法规要求，我们不会向任何第三方分享您的个人信息。
        </text>
      </view>

      <view class="section">
        <text class="section-title">四、您的权利</text>
        <text class="section-text">
          您可以随时查看、删除或撤回授权。对于您授权的数据，我们会在您撤销授权后及时停止收集与使用。
        </text>
      </view>

      <view class="section">
        <text class="section-title">五、未成年人保护</text>
        <text class="section-text">
          我们高度重视未成年人的隐私保护。若您未满 18 周岁，请在监护人指导下使用我们的服务。
        </text>
      </view>

      <view class="section">
        <text class="section-title">六、政策更新</text>
        <text class="section-text">
          我们可能会根据业务和法律变化适时更新本政策，更新内容将通过应用内提示或公告的方式通知您。
        </text>
      </view>

      <view class="section">
        <text class="section-title">七、联系我们</text>
        <text class="section-text">
          若您对本隐私政策或个人信息处理有任何疑问，可通过应用内反馈或联系客服邮箱与我们联系。
        </text>
      </view>
    </scroll-view>

    <!-- 返回按钮 -->
    <button class="back-btn" @tap="goBack">返回</button>
  </view>
</template>

<script setup>
// 返回上一页
function goBack() {
  uni.navigateBack({
    delta: 1
  })
}
</script>

<style scoped>
.privacy-page {
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

/* 内容区域 */
.content {
  width: 100%;
  max-height: 70vh;
  padding: 30rpx;
  background-color: #fff;
  border-radius: 20rpx;
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
  display: block;
  margin-bottom: 10rpx;
}

.section-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
  display: block;
}

/* 返回按钮 */
.back-btn {
  margin-top: 40rpx;
  width: 400rpx;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
}
</style>

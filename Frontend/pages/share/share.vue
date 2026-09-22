<template>
  <view class="share-container">
    <!-- 自定义导航栏 -->
   
    <!-- 主要内容区域 -->
    <view class="content">
      <!-- 海报图片作为背景 -->
      <image class="poster-image" src="../../static/assets/海报.png" mode="aspectFill"></image>
      <view class="poster-overlay"></view>
      
      <!-- 分享按钮 -->
      <view class="share-button" @click="handleShare">
        <text class="share-button-text">立即分享</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import CustomNavBar from '@/components/CustomNavBar.vue'
import { onMounted, ref } from 'vue'

// 返回按钮处理
const handleBack = () => {
  uni.navigateBack({
    delta: 1
  })
}

// 微信小程序分享配置
// #ifdef MP-WEIXIN
onMounted(() => {
  // 设置分享给朋友的内容
  uni.onShareAppMessage(() => {
    return {
      title: '趣定旅游 - 精美旅游攻略',
      desc: '发现美好旅程，分享精彩瞬间',
      path: '/pages/share/share',
      imageUrl: '../../static/assets/海报.png'
    }
  })
  
  // 设置分享到朋友圈的内容
  uni.onShareTimeline(() => {
    return {
      title: '趣定旅游 - 精美旅游攻略',
      imageUrl: '../../static/assets/海报.png'
    }
  })
})
// #endif

// 微信小程序分享按钮点击处理
// #ifdef MP-WEIXIN
const onWeChatShare = () => {
  console.log('微信分享按钮被点击')
}

const shareToWeChatFriend = () => {
  // 微信小程序分享给好友
  uni.showModal({
    title: '分享提示',
    content: '请点击右上角的「...」按钮，选择「转发给朋友」进行分享',
    showCancel: false,
    confirmText: '我知道了'
  })
}

// 微信小程序分享到朋友圈
const shareToWeChatTimeline = () => {
  // 微信小程序分享到朋友圈
  uni.showModal({
    title: '分享提示',
    content: '请点击右上角的「...」按钮，选择「分享到朋友圈」进行分享',
    showCancel: false,
    confirmText: '我知道了'
  })
}
// #endif

// 分享按钮处理
const handleShare = () => {
  // #ifdef APP-PLUS
  // App环境，显示所有分享选项
  uni.showActionSheet({
    itemList: ['分享到微信好友', '分享到朋友圈', '保存图片到相册', '复制链接'],
    success: function (res) {
      switch (res.tapIndex) {
        case 0:
          shareToWeChat()
          break
        case 1:
          shareToWeChatMoments()
          break
        case 2:
          saveImageToAlbum()
          break
        case 3:
          copyShareLink()
          break
      }
    },
    fail: function (res) {
      console.log('显示分享菜单失败', res.errMsg)
    }
  })
  // #endif
  
  // #ifdef H5
  // H5环境，显示基础功能
  uni.showActionSheet({
    itemList: ['保存图片到相册', '复制链接'],
    success: function (res) {
      switch (res.tapIndex) {
        case 0:
          saveImageToAlbum()
          break
        case 1:
          copyShareLink()
          break
      }
    },
    fail: function (res) {
      console.log('显示分享菜单失败', res.errMsg)
    }
  })
  // #endif
  
  // #ifdef MP-WEIXIN
  // 微信小程序环境，显示所有功能
  uni.showActionSheet({
    itemList: ['分享给微信好友', '分享到朋友圈', '保存图片到相册', '复制链接'],
    success: function (res) {
      switch (res.tapIndex) {
        case 0:
          shareToWeChatFriend()
          break
        case 1:
          shareToWeChatTimeline()
          break
        case 2:
          saveImageToAlbum()
          break
        case 3:
          copyShareLink()
          break
      }
    },
    fail: function (res) {
      console.log('显示分享菜单失败', res.errMsg)
    }
  })
  // #endif
}

// #ifdef APP-PLUS
// App环境专用的分享函数
const shareToWeChat = () => {
  uni.getProvider({
    service: 'share',
    success: function (res) {
      if (res.provider.includes('weixin')) {
        uni.share({
          provider: 'weixin',
          scene: 'WXSceneSession',
          type: 0,
          href: 'https://your-app-domain.com/share',
          title: '趣定旅游 - 精美旅游攻略',
          summary: '发现美好旅程，分享精彩瞬间',
          imageUrl: '../../static/assets/海报.png',
          success: function (res) {
            uni.showToast({ title: '分享成功', icon: 'success' })
          },
          fail: function (err) {
            uni.showToast({ title: '分享失败', icon: 'none' })
          }
        })
      } else {
        uni.showToast({ title: '当前环境不支持微信分享', icon: 'none' })
      }
    }
  })
}



const shareToWeChatMoments = () => {
  uni.getProvider({
    service: 'share',
    success: function (res) {
      if (res.provider.includes('weixin')) {
        uni.share({
          provider: 'weixin',
          scene: 'WXSceneTimeline',
          type: 0,
          href: 'https://your-app-domain.com/share',
          title: '趣定旅游 - 精美旅游攻略',
          summary: '发现美好旅程，分享精彩瞬间',
          imageUrl: '../../static/assets/海报.png',
          success: function (res) {
            uni.showToast({ title: '分享成功', icon: 'success' })
          },
          fail: function (err) {
            uni.showToast({ title: '分享失败', icon: 'none' })
          }
        })
      } else {
        uni.showToast({ title: '当前环境不支持微信分享', icon: 'none' })
      }
    }
  })
}
// #endif

// 保存图片到相册
const saveImageToAlbum = () => {
  // #ifdef MP-WEIXIN
  // 微信小程序环境，使用绝对路径
  const imagePath = '/static/assets/海报.png'
  // #endif
  
  // #ifndef MP-WEIXIN
  // 其他环境使用相对路径
  const imagePath = '../../static/assets/海报.png'
  // #endif
  
  // 先检查授权状态
  uni.getSetting({
    success: (res) => {
      if (res.authSetting['scope.writePhotosAlbum'] === false) {
        // 用户之前拒绝了授权，需要引导用户手动开启
        uni.showModal({
          title: '提示',
          content: '需要授权访问相册才能保存图片，请在设置中开启相册权限',
          confirmText: '去设置',
          success: function (modalRes) {
            if (modalRes.confirm) {
              uni.openSetting()
            }
          }
        })
        return
      }
      
      // 保存图片到相册
      uni.saveImageToPhotosAlbum({
        filePath: imagePath,
        success: function () {
          console.log('保存图片成功')
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
        },
        fail: function (err) {
          console.log('保存图片失败', err)
          if (err.errMsg.includes('auth') || err.errMsg.includes('permission')) {
            // 请求授权
            uni.authorize({
              scope: 'scope.writePhotosAlbum',
              success: () => {
                // 授权成功，重新保存
                saveImageToAlbum()
              },
              fail: () => {
                uni.showModal({
                  title: '提示',
                  content: '需要授权访问相册才能保存图片',
                  confirmText: '去设置',
                  success: function (modalRes) {
                    if (modalRes.confirm) {
                      uni.openSetting()
                    }
                  }
                })
              }
            })
          } else {
             uni.showToast({
               title: '保存失败: ' + (err.errMsg || '未知错误'),
               icon: 'none'
             })
           }
         }
       })
     }
   })
}

// 复制分享链接
const copyShareLink = () => {
  const shareUrl = 'https://your-app-domain.com/share'
  
  uni.setClipboardData({
    data: shareUrl,
    success: function () {
      console.log('复制链接成功', shareUrl)
      uni.showToast({
        title: '链接已复制',
        icon: 'success',
        duration: 2000
      })
    },
    fail: function (err) {
      console.log('复制链接失败', err)
      uni.showModal({
        title: '复制失败',
        content: '无法复制链接到剪贴板，请手动复制：' + shareUrl,
        showCancel: false,
        confirmText: '知道了'
      })
    }
  })
}
</script>

<style scoped>
.share-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 0;
  position: relative;
}

.poster-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.poster-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(255, 255, 255, 0.1) 0%,
    rgba(255, 255, 255, 0.05) 20%,
    transparent 40%,
    transparent 60%,
    rgba(0, 0, 0, 0.05) 80%,
    rgba(0, 0, 0, 0.15) 100%
  );
  pointer-events: none;
  z-index: 2;
}

/* 分享按钮 */
.share-button {
  position: fixed;
  bottom: 120rpx;
  left: 50%;
  transform: translateX(-50%);
  z-index: 20;
  width: 260rpx;
  height: 90rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 12rpx 32rpx rgba(0, 0, 0, 0.2),
    0 6rpx 16rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20rpx);
  /* 重置button默认样式 */
  padding: 0;
  margin: 0;
  outline: none;
  appearance: none;
  -webkit-appearance: none;
}

.share-button:active {
  transform: translateX(-50%) scale(0.95);
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 
    0 8rpx 24rpx rgba(0, 0, 0, 0.15),
    0 2rpx 6rpx rgba(0, 0, 0, 0.06);
}

.share-button-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}
</style>
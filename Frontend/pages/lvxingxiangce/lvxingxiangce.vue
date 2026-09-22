<template>
  <view class="album-page">
	<CustomNavBar :showBack="true" title="旅行相册" @back="handleBack" />
    <!-- 标题 -->
    <view class="title">来制定属于你的旅行相册</view>

    <!-- 保存按钮 -->
    <button class="save-btn" @tap="saveAlbum">保存相册</button>

    <!-- 动态照片区域 -->
    <view class="photo-list">
      <view
        class="photo-item"
        v-for="(item, index) in photos"
        :key="index"
      >
        <!-- 照片上传 -->
        <view class="photo-box" @tap="chooseImage(index)">
          <image
            v-if="item.url"
            :src="item.url"
            mode="aspectFill"
            class="photo"
          ></image>
          <view v-else class="add-btn">+</view>
        </view>

        <!-- 文本框 -->
        <input
          class="caption-input"
          type="text"
          placeholder="写下这张照片的说明吧～"
          v-model="item.text"
        />
      </view>
    </view>

    <!-- 添加 / 删除 照片按钮 -->
    <button class="add-photo-btn" @tap="addPhoto">添加照片</button>
    <button class="delete-photo-btn" @tap="deletePhoto">删除照片</button>
  </view>
</template>

<script setup>
	import CustomNavBar from '@/components/CustomNavBar.vue'
	
	const handleBack = () => {
		uni.navigateBack({
			delta: 1
		});
	}
import { ref } from 'vue'

const photos = ref([])

// 添加一个新的照片项
function addPhoto() {
  photos.value.push({ url: '', text: '' })
}

// 删除最后一张照片
function deletePhoto() {
  if (photos.value.length === 0) {
    uni.showToast({
      title: '没有可以删除的照片',
      icon: 'none'
    })
    return
  }
  photos.value.pop()
  uni.showToast({
    title: '已删除最后一张照片',
    icon: 'success'
  })
}

// 选择图片
function chooseImage(index) {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: res => {
      photos.value[index].url = res.tempFilePaths[0]
    }
  })
}

// 保存相册（这里仅演示用）
function saveAlbum() {
  if (photos.value.length === 0) {
    uni.showToast({
      title: '请先添加照片',
      icon: 'none'
    })
    return
  }

  const incomplete = photos.value.some(p => !p.url || !p.text.trim())
  if (incomplete) {
    uni.showToast({
      title: '请完善每张照片和文字说明',
      icon: 'none'
    })
    return
  }

  console.log('保存的相册数据：', photos.value)
  uni.showToast({
    title: '相册已保存！',
    icon: 'success'
  })
}
</script>

<style scoped>
.album-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx;
  background-color: #f9f9f9;
  min-height: 100vh;
}

.title {
  margin-top: 100rpx;
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 40rpx;
  text-align: center;
}

/* 保存按钮 */
.save-btn {
  width: 400rpx;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
  margin-bottom: 40rpx;
}

.photo-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 50rpx;
}

.photo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.photo-box {
  width: 500rpx;
  height: 300rpx;
  border: 2rpx dashed #ccc;
  border-radius: 20rpx;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.add-btn {
  font-size: 100rpx;
  color: #bbb;
}

.photo {
  width: 100%;
  height: 100%;
}

.caption-input {
  width: 480rpx;
  margin-top: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  padding: 15rpx;
  font-size: 28rpx;
  background-color: #fff;
}

.add-photo-btn {
  margin-top: 60rpx;
  width: 400rpx;
  height: 80rpx;
  background-color: #4CAF50;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
}

/* 删除按钮 */
.delete-photo-btn {
  margin-top: 20rpx;
  width: 400rpx;
  height: 80rpx;
  background-color: #FF4D4F;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
}
</style>

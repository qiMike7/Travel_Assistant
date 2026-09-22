<template>
  <view class="container">
    <!-- 主信息页面 -->
    <view v-if="currentView === 'main'">
      <view class="list-item" v-for="(item, index) in infoList" :key="index" @click="handleEdit(item)">
        <text class="item-title">{{ item.title }}</text>
        <view class="item-right">
          <template v-if="item.type === 'avatar'">
            <image class="avatar" :src="userInfo[item.title]"></image>
          </template>
          <template v-else-if="item.type === 'qr'">
            <image class="qr-icon" :src="userInfo[item.title]"></image>
          </template>
          <template v-else>
            <text class="item-content">{{ userInfo[item.title] || '未设置' }}</text>
          </template>
          <uni-icons type="right" size="20" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 文本编辑页面 -->
    <view v-if="currentView === 'textEdit'">
      <view class="edit-header">
        <text class="back-btn" @click="currentView = 'main'">← 返回</text>
        <text class="edit-title">编辑 {{ editingKey }}</text>
      </view>
      <input
        class="edit-input"
        :value="editingValue"
        @input="editingValue = $event.detail.value"
        placeholder="请输入内容"
        autofocus
      />
      <button class="save-btn" @click="saveTextEdit">保存</button>
    </view>

    <!-- 性别选择页面 -->
    <view v-if="currentView === 'genderEdit'">
      <view class="edit-header">
        <text class="back-btn" @click="currentView = 'main'">← 返回</text>
        <text class="edit-title">选择性别</text>
      </view>
      <picker mode="selector" :range="genderList" @change="onGenderChange">
        <view class="picker-view">{{ editingValue }}</view>
      </picker>
      <button class="save-btn" @click="saveGenderEdit">保存</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 当前显示的视图：main/textEdit/genderEdit
      currentView: 'main',
      // 正在编辑的字段
      editingKey: '',
      // 正在编辑的值
      editingValue: '',
      // 性别选项
      genderList: ['男', '女', '保密'],
      // 用户信息数据
      userInfo: {
        '头像': '/static/settings/profile/avatar.png',
        '名字': '小趣',
        '性别': '男',
        '地区': '安徽 合肥',
        '手机号': '189********25',
        '微信号': '123456',
        '我的二维码': '/static/settings/profile/qrcode.png',
        '签名': '未填写'
      },
      // 列表配置
      infoList: [
        { title: '头像', type: 'avatar' },
        { title: '名字' },
        { title: '性别' },
        { title: '地区' },
        { title: '手机号' },
        { title: '微信号' },
        { title: '我的二维码', type: 'qr' },
        { title: '签名' }
      ]
    };
  },
  methods: {
    // 处理编辑事件
    handleEdit(item) {
      const key = item.title;
      this.editingKey = key;
      this.editingValue = this.userInfo[key];
      
      if (key === '头像') {
        this.chooseAvatar();
      } else if (key === '性别') {
        this.currentView = 'genderEdit';
      } else {
        this.currentView = 'textEdit';
      }
    },
    
    // 选择头像
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        success: (res) => {
          this.userInfo['头像'] = res.tempFilePaths[0];
        }
      });
    },
    
    // 性别选择变化
    onGenderChange(e) {
      this.editingValue = this.genderList[e.detail.value];
    },
    
    // 保存文本编辑
    saveTextEdit() {
      this.userInfo[this.editingKey] = this.editingValue;
      this.currentView = 'main';
    },
    
    // 保存性别编辑
    saveGenderEdit() {
      this.userInfo['性别'] = this.editingValue;
      this.currentView = 'main';
    }
  }
};
</script>

<style scoped>
/* 样式部分不变 */
.container {
  background-color: #fff;
  min-height: 100vh;
}

/* 主页面样式 */
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  border-bottom: 1px solid #f5f5f5;
}

.item-title {
  font-size: 32rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
}

.item-content {
  font-size: 32rpx;
  color: #666;
  margin-right: 16rpx;
  text-align: right;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.qr-icon {
  width: 48rpx;
  height: 48rpx;
  margin-right: 16rpx;
}

/* 编辑页面样式 */
.edit-header {
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
}

.back-btn {
  font-size: 36rpx;
  color: #333;
  margin-right: 30rpx;
}

.edit-title {
  font-size: 36rpx;
  color: #333;
  flex: 1;
}

.edit-input {
  width: 100%;
  padding: 30rpx;
  font-size: 32rpx;
  border-bottom: 1px solid #f5f5f5;
  box-sizing: border-box;
}

.picker-view {
  width: 100%;
  padding: 30rpx;
  font-size: 32rpx;
  border-bottom: 1px solid #f5f5f5;
  box-sizing: border-box;
  color: #666;
}

.save-btn {
  width: 60rpx;
  margin: 40rpx 30rpx;
  background-color: #007aff;
  color: #fff;
  font-size: 32rpx;
  padding: 20rpx 0;
  border-radius: 8rpx;
}
</style>

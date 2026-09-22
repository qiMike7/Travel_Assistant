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
import { userApi, fileApi } from '@/common/api/index.js'
import { toAbsoluteUrl } from '@/common/utils/request.js'

// 中文标签 -> 后端字段映射
const FIELD_MAP = {
  '头像': 'avatar',
  '名字': 'nickname',
  '性别': 'gender',
  '地区': 'region',
  '手机号': 'phone',
  '微信号': 'wechat',
  '我的二维码': 'qrcode',
  '签名': 'signature'
}

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
      // 用户信息数据（onLoad 时从后端加载）
      userInfo: {
        '头像': '/static/settings/profile/avatar.png',
        '名字': '',
        '性别': '',
        '地区': '',
        '手机号': '',
        '微信号': '',
        '我的二维码': '/static/settings/profile/qrcode.png',
        '签名': ''
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
  onLoad() {
    this.loadProfile();
  },
  methods: {
    // 从后端加载个人资料
    async loadProfile() {
      try {
        const u = await userApi.profile();
        if (!u) return;
        if (u.nickname) this.userInfo['名字'] = u.nickname;
        if (u.gender) this.userInfo['性别'] = u.gender;
        if (u.region) this.userInfo['地区'] = u.region;
        if (u.phone) this.userInfo['手机号'] = u.phone;
        if (u.wechat) this.userInfo['微信号'] = u.wechat;
        if (u.signature) this.userInfo['签名'] = u.signature;
        if (u.avatar) this.userInfo['头像'] = toAbsoluteUrl(u.avatar);
        if (u.qrcode) this.userInfo['我的二维码'] = toAbsoluteUrl(u.qrcode);
      } catch (e) {
        // 未登录时请求封装会跳转登录页
      }
    },

    // 提交单个字段到后端
    async pushField(label, value) {
      const field = FIELD_MAP[label];
      if (!field) return;
      try {
        await userApi.updateProfile({ [field]: value });
        uni.showToast({ title: '已保存', icon: 'success' });
      } catch (e) {
        // 错误提示已统一处理
      }
    },

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
    
    // 选择头像：先上传到后端，再保存 URL
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        success: async (res) => {
          const tempPath = res.tempFilePaths[0];
          try {
            const url = await fileApi.upload(tempPath);
            // 入库保持相对路径（不受模拟器/真机 BASE_URL 差异影响），展示时拼绝对地址
            this.userInfo['头像'] = toAbsoluteUrl(url);
            this.pushField('头像', url);
          } catch (e) {
            // 上传失败提示已在封装中处理
          }
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
      this.pushField(this.editingKey, this.editingValue);
      this.currentView = 'main';
    },
    
    // 保存性别编辑
    saveGenderEdit() {
      this.userInfo['性别'] = this.editingValue;
      this.pushField('性别', this.editingValue);
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

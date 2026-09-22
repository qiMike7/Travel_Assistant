<template>
  <view class="setting-page">
    <!-- 顶部导航栏 -->
    <CustomNavBar :showBack="true" title="设置" @back="handleBack" />

    <!-- 用户信息区域 -->
    <view class="user-info-section" @click="navigateToProfile">
      <view class="avatar-container">
        <image 
          class="avatar" 
          :src="userAvatar" 
          mode="widthFix"
          lazy-load
        ></image>
      </view>
      <view class="user-details">
        <view class="username">{{ username }}</view>
        <view class="user-desc">点击查看或编辑个人资料</view>
      </view>
      <!-- 移除箭头图标 -->
      <view class="arrow-icon"></view>
    </view>

    <!-- 主要设置区域 -->
    <view class="settings-container">
      <!-- 功能设置 -->
      <view class="setting-section">
        <view class="section-title">功能设置</view>
        
        <view class="setting-item" @click="navigateToPreference">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">偏好设置</view>
            <view class="item-desc">设置您的旅行偏好，获得更精准的推荐</view>
          </view>
          <!-- 移除箭头图标 -->
          <view class="arrow-icon"></view>
        </view>
        
        <view class="setting-item">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">通知提醒</view>
            <view class="item-desc">接收行程更新和旅行建议通知</view>
          </view>
          <view class="switch-container">
            <switch 
              :checked="notificationsEnabled" 
              color="#42b983"
              @change="onNotificationChange"
            ></switch>
          </view>
        </view>
        
        <view class="setting-item">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">深色模式</view>
            <view class="item-desc">切换应用显示主题</view>
          </view>
          <view class="switch-container">
            <switch 
              :checked="darkModeEnabled" 
              color="#42b983"
              @change="onDarkModeChange"
            ></switch>
          </view>
        </view>
      </view>
      
      <!-- 数据管理 -->
      <view class="setting-section">
        <view class="section-title">数据管理</view>
        
        <view class="setting-item" @click="clearCache">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">清除缓存</view>
            <view class="item-desc">{{ cacheSize }} MB</view>
          </view>
          <!-- 移除箭头图标 -->
          <view class="arrow-icon"></view>
        </view>
      </view>
      
      <!-- 关于 -->
      <view class="setting-section">
        <view class="section-title">关于</view>
        
        <view class="setting-item" @click="navigateToHelp">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">帮助中心</view>
            <view class="item-desc">使用指南和常见问题解答</view>
          </view>
          <!-- 移除箭头图标 -->
          <view class="arrow-icon"></view>
        </view>
        
        <view class="setting-item" @click="navigateToPrivacy">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">隐私政策</view>
            <view class="item-desc">查看我们的隐私保护条款</view>
          </view>
          <!-- 移除箭头图标 -->
          <view class="arrow-icon"></view>
        </view>
        
        <view class="setting-item">
          <view class="item-icon"></view>
          <view class="item-content">
            <view class="item-title">版本信息</view>
            <view class="item-desc">当前版本 v{{ appVersion }}</view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 退出登录按钮 -->
    <view class="logout-btn" @click="confirmLogout">
      退出登录
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import CustomNavBar from '@/components/CustomNavBar.vue'
import { preferenceApi } from '@/common/api/index.js'
import { getUser, isLogin, clearAuth } from '@/common/utils/auth.js'

const handleBack = () => {
	uni.navigateBack({
		delta: 1
	});
}

// 状态管理
const userAvatar = ref('/static/settings/profile/avatar.png');
const username = ref('旅行者');
const notificationsEnabled = ref(true);
const darkModeEnabled = ref(false);
const cacheSize = ref('2.4');
const appVersion = ref('1.0.0');

// 当前偏好快照，保存时整体提交
const pref = ref({});

// 生命周期
onMounted(() => {
  initSettings();
});

// 初始化设置
const initSettings = async () => {
  // 用户信息（来自登录缓存）
  const u = getUser();
  if (u) {
    if (u.nickname) username.value = u.nickname;
    if (u.avatar) userAvatar.value = u.avatar;
  }
  // 通知/深色模式偏好来自后端
  if (!isLogin()) return;
  try {
    const p = await preferenceApi.get();
    pref.value = p || {};
    notificationsEnabled.value = p.notifications !== false;
    darkModeEnabled.value = !!p.darkMode;
  } catch (e) {
    // 忽略
  }
};

// 保存偏好变更到后端
const savePref = async (patch) => {
  pref.value = { ...pref.value, ...patch };
  if (!isLogin()) return;
  try {
    await preferenceApi.save(pref.value);
  } catch (e) {
    // 错误提示已统一处理
  }
};

// 导航到个人资料页
const navigateToProfile = () => {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/login' });
    return;
  }
  uni.navigateTo({
      url: '/pages/profile/profile'
    });
  };

// 导航到偏好设置页
const navigateToPreference = () => {
  uni.navigateTo({
      url: '/pages/preference/preference'
    });
};

// 通知设置变更
const onNotificationChange = (e) => {
  notificationsEnabled.value = e.detail.value;
  savePref({ notifications: notificationsEnabled.value });
  uni.showToast({
    title: notificationsEnabled.value ? '通知已开启' : '通知已关闭',
    icon: 'none',
    duration: 2000
  });
};

// 深色模式变更
const onDarkModeChange = (e) => {
  darkModeEnabled.value = e.detail.value;
  savePref({ darkMode: darkModeEnabled.value });
  uni.showToast({
    title: darkModeEnabled.value ? '深色模式已开启' : '浅色模式已开启',
    icon: 'none',
    duration: 2000
  });
};

// 清除缓存
const clearCache = () => {
  uni.showLoading({
    title: '清除中...'
  });
  
  setTimeout(() => {
    uni.hideLoading();
    cacheSize.value = '0.0';
    uni.showToast({
      title: '缓存已清除',
      icon: 'success',
      duration: 2000
    });
  }, 1000);
};

// 导航到帮助中心
const navigateToHelp = () => {
  uni.navigateTo({
      url: '/pages/help/help'
    });
};

// 导航到隐私政策
const navigateToPrivacy = () => {
  uni.navigateTo({
      url: '/pages/privacy/privacy'
    });
};

// 确认退出登录：使用系统弹窗（无需 uni-popup 组件），确认后执行 onLogout
const confirmLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出当前账号吗？',
    confirmText: '退出登录',
    confirmColor: '#ff4d4f',
    success: (res) => {
      if (res.confirm) onLogout();
    }
  });
};

// 执行退出登录
const onLogout = () => {
  // 清除本地登录态
  clearAuth();
  
  uni.showToast({
    title: '已退出登录',
    icon: 'none',
    duration: 2000
  });
  
  // 返回“我的”页
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/user/user' });
  }, 1000);
};
</script>

<style scoped>
/* 样式部分保持不变 */
.setting-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  overflow: auto;
}

.back-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.empty {
  width: 44px;
  height: 44px;
}

/* 用户信息区域 */
.user-info-section {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #ffffff;
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.user-info-section:active {
  background-color: #f5f5f5;
}

.avatar-container {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  border: 2px solid #f0f0f0;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.username {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.user-desc {
  font-size: 14px;
  color: #999;
}

/* 设置容器 */
.settings-container {
  padding-bottom: 20px;
}

.setting-section {
  background-color: #ffffff;
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 14px;
  color: #999;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item:active {
  background-color: #f5f5f5;
}

.item-icon {
  display: none;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 2px;
}

.item-desc {
  font-size: 12px;
  color: #999;
}

.switch-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 退出登录按钮 */
.logout-btn {
  margin-bottom: 100rpx;
  margin-left: 30rpx;
  margin-right: 30rpx;
  padding: 12px 0;
  text-align: center;
  background-color: #ffffff;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s;
}

.logout-btn:active {
  background-color: #fff5f5;
}

/* 图标字体样式 */
.iconfont {
  font-family: "iconfont" !important;
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}


/* 自定义图标 */
.icon-back:before { content: "\e600"; }
.icon-arrow-right:before { content: "\e601"; }
.icon-preferences:before { content: "\e602"; }
.icon-notification:before { content: "\e603"; }
.icon-theme:before { content: "\e604"; }
.icon-cache:before { content: "\e605"; }
.icon-export:before { content: "\e606"; }
.icon-help:before { content: "\e607"; }
.icon-privacy:before { content: "\e608"; }
.icon-version:before { content: "\e609"; }
</style>
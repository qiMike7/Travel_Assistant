<template>
  <view class="album-view-page">
    <CustomNavBar :showBack="false" title="我的相册" />

    <scroll-view scroll-y="true" class="album-scroll">
      <!-- 加载中 -->
      <view v-if="loading" class="loading-tip">
        <text class="loading-text">加载中…</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="albums.length === 0" class="empty">
        <image class="empty-icon" src="/static/tabbar/album-selected.png" mode="aspectFit"></image>
        <text class="empty-text">还没有保存的照片</text>
        <text class="empty-sub">去「拍照打卡」记录旅途美好吧～</text>
      </view>

      <!-- 相册列表：按年月大类 + 每日小分类展示 -->
      <view v-else class="album-list">
        <view class="album-block" v-for="group in groups" :key="group.key">
          <view class="album-head">
            <text class="album-title">{{ group.label }}</text>
            <text class="album-date">{{ group.count }} 张</text>
          </view>
          <view class="day-block" v-for="day in group.days" :key="day.key">
            <text class="day-label">{{ day.label }}</text>
            <view class="photo-grid">
              <view
                class="photo-cell"
                v-for="p in day.photos"
                :key="p.id"
                @tap="previewPhoto(day, p)"
              >
                <image class="grid-img" :src="toAbsoluteUrl(p.url)" mode="aspectFill"></image>
                <text v-if="p.caption" class="grid-caption">{{ p.caption }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { albumApi } from '@/common/api/index.js'
import { toAbsoluteUrl } from '@/common/utils/request.js'
import { isLogin } from '@/common/utils/auth.js'

const albums = ref([])
const groups = ref([])
const loading = ref(false)

// 解析 createTime("yyyy-MM-dd HH:mm:ss")：大类取年月，小类取日，忽略时分秒
const parseDate = (createTime) => {
  const fallback = { monthKey: 'unknown', monthLabel: '未分类', dayKey: 'unknown', dayLabel: '' }
  if (!createTime) return fallback
  const datePart = String(createTime).split(' ')[0]
  const [y, m, d] = datePart.split('-')
  if (!y) return fallback
  const month = Number(m)
  const day = Number(d)
  return {
    monthKey: `${y}-${m || '00'}`,
    monthLabel: month ? `${y}年${month}月` : `${y}年`,
    dayKey: `${y}-${m}-${d || '00'}`,
    dayLabel: day ? `${day}日` : ''
  }
}

// 先按年月合并为大分类，再按日拆分为小分类（albums 已按创建时间倒序，故均为最新在前）
const buildGroups = (list) => {
  const monthMap = new Map()
  ;(list || []).forEach(album => {
    const { monthKey, monthLabel, dayKey, dayLabel } = parseDate(album.createTime)
    if (!monthMap.has(monthKey)) {
      monthMap.set(monthKey, { key: monthKey, label: monthLabel, dayMap: new Map() })
    }
    const g = monthMap.get(monthKey)
    if (!g.dayMap.has(dayKey)) g.dayMap.set(dayKey, { key: dayKey, label: dayLabel, photos: [] })
    const day = g.dayMap.get(dayKey)
    ;(album.photos || []).forEach(p => day.photos.push(p))
  })
  groups.value = Array.from(monthMap.values()).map(g => {
    const days = Array.from(g.dayMap.values())
    return { key: g.key, label: g.label, days, count: days.reduce((s, d) => s + d.photos.length, 0) }
  })
}

// 加载我保存的相册：列表接口只返回封面摘要，需再拉取详情获取每张照片
const loadAlbums = async () => {
  if (!isLogin()) {
    albums.value = []
    groups.value = []
    return
  }
  loading.value = true
  try {
    const list = await albumApi.list()
    const details = await Promise.all((list || []).map(a =>
      albumApi.detail(a.id).catch(() => ({ ...a, photos: [] }))
    ))
    albums.value = details
    buildGroups(details)
  } catch (e) {
    // 错误提示已在请求封装中统一处理
  } finally {
    loading.value = false
  }
}

// 每次显示页面时刷新（拍照打卡保存后回到相册可及时看到新照片）
onShow(() => {
  loadAlbums()
})

// 点击照片：全屏预览当日小分类下的所有照片
const previewPhoto = (day, photo) => {
  const urls = (day.photos || []).map(p => toAbsoluteUrl(p.url))
  if (!urls.length) return
  uni.previewImage({ urls, current: toAbsoluteUrl(photo.url) })
}
</script>

<style scoped>
.album-view-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(79, 82, 255, 0.12) 0%, rgba(79, 82, 255, 0) 60%);
}

.album-scroll {
  flex: 1;
  height: calc(100vh - 88rpx);
}

.loading-tip {
  padding-top: 120rpx;
  text-align: center;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 180rpx;
}

.empty-icon {
  width: 120rpx;
  height: 120rpx;
  opacity: 0.6;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
}

.empty-sub {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
}

/* 相册列表 */
.album-list {
  padding: 20rpx 30rpx 60rpx;
}

.album-block {
  margin-bottom: 50rpx;
}

.album-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.album-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.album-date {
  font-size: 24rpx;
  color: #999;
}

/* 每日小分类 */
.day-block {
  margin-bottom: 30rpx;
}

.day-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.photo-cell {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.06);
}

.grid-img {
  width: 100%;
  height: 220rpx;
  background: #f0f0f0;
}

.grid-caption {
  font-size: 22rpx;
  color: #666;
  padding: 10rpx 12rpx;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>

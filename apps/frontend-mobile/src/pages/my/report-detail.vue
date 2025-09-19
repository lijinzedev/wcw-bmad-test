<template>
  <scroll-view class="detail-page" scroll-y>
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else>
      <view class="card">
        <view class="header">
          <text class="title">{{ detail.description }}</text>
          <text class="status" :class="statusClass(detail.status)">{{ detail.status }}</text>
        </view>
        <view class="meta">
          <text>上报时间：{{ formatDate(detail.reportedAt) }}</text>
          <text>等级：{{ detail.level || '未标注' }}</text>
          <text v-if="detail.location">位置：{{ detail.location }}</text>
        </view>
      </view>

      <view class="card" v-if="detail.attachments && detail.attachments.length">
        <text class="section-title">附件</text>
        <view class="attachments">
          <image
            v-for="(item, index) in detail.attachments"
            :key="item.key || index"
            :src="item.url || placeholder"
            class="attachment"
            mode="aspectFill"
            @click="preview(index)"
          />
        </view>
      </view>

      <view class="card">
        <text class="section-title">处理进度</text>
        <view v-if="detail.updates && detail.updates.length" class="timeline">
          <view v-for="item in detail.updates" :key="item.updateId" class="timeline-item">
            <view class="dot"></view>
            <view class="content">
              <text class="action">{{ item.action }}</text>
              <text class="time">{{ formatDate(item.timestamp) }}</text>
              <text v-if="item.details" class="desc">{{ item.details }}</text>
            </view>
          </view>
        </view>
        <view v-else class="empty">暂无进度信息</view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ensureAuth } from '../../utils/auth.js'
import { fetchHazardDetail } from '../../services/api.js'

const detail = ref({})
const loading = ref(true)
const placeholder = '/static/logo.png'

onLoad(async (options) => {
  if (!ensureAuth()) return
  const id = options?.hazardId
  if (!id) {
    uni.showToast({ title: '缺少隐患ID', icon: 'none' })
    return
  }
  await loadDetail(id)
})

async function loadDetail(id) {
  loading.value = true
  try {
    const res = await fetchHazardDetail(id)
    detail.value = res
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) return ''
  try {
    const date = new Date(value)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return value
  }
}

function preview(index) {
  const urls = (detail.value.attachments || []).map(item => item.url || placeholder)
  uni.previewImage({
    current: urls[index],
    urls
  })
}

function statusClass(status) {
  switch (status) {
    case '待指派':
      return 'status-pending'
    case '整改中':
      return 'status-working'
    case '待验收':
      return 'status-review'
    case '已关闭':
      return 'status-done'
    default:
      return ''
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
}
.loading {
  text-align: center;
  margin-top: 120rpx;
  color: #909399;
}
.card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 26rpx;
  box-shadow: 0 12rpx 30rpx rgba(15, 101, 202, 0.09);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2d3d;
}
.status {
  font-size: 26rpx;
  font-weight: 600;
}
.status-pending { color: #f59a23; }
.status-working { color: #409eff; }
.status-review { color: #e6a23c; }
.status-done { color: #67c23a; }
.meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  font-size: 26rpx;
  color: #606266;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}
.attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.attachment {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  background: #f2f3f7;
}
.timeline {
  border-left: 2rpx solid #dcdfe6;
  margin-left: 12rpx;
  padding-left: 28rpx;
}
.timeline-item {
  position: relative;
  margin-bottom: 24rpx;
}
.dot {
  width: 16rpx;
  height: 16rpx;
  background: #0f65ca;
  border-radius: 50%;
  position: absolute;
  left: -36rpx;
  top: 6rpx;
}
.content .action {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}
.content .time {
  display: block;
  font-size: 24rpx;
  color: #909399;
  margin-top: 6rpx;
}
.content .desc {
  font-size: 24rpx;
  color: #606266;
  margin-top: 8rpx;
}
.empty {
  text-align: center;
  color: #909399;
}
</style>

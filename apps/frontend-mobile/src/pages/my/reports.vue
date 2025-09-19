<template>
  <scroll-view class="reports-page" scroll-y @scrolltolower="loadMore">
    <view class="filter">
      <picker mode="selector" :range="statusOptions" @change="onStatusChange">
        <view class="picker">{{ statusLabel }}</view>
      </picker>
      <button size="mini" type="default" @click="refresh">刷新</button>
    </view>

    <view v-if="loading" class="loading">加载中...</view>

    <view v-if="!loading && reports.length === 0" class="empty">
      <text>暂无上报记录</text>
    </view>

    <view v-for="item in reports" :key="item.hazardId" class="card" @click="openDetail(item)">
      <view class="card-header">
        <text class="status" :class="statusClass(item.status)">{{ item.status }}</text>
        <text class="time">{{ formatDate(item.reportedAt) }}</text>
      </view>
      <view class="description">{{ item.description }}</view>
      <view class="meta">
        <text>等级：{{ item.level || '未标注' }}</text>
        <text>附件：{{ (item.attachments || []).length }} 个</text>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { ensureAuth } from '../../utils/auth.js'
import { fetchMyHazards } from '../../services/api.js'

const statusOptions = ['全部', '待指派', '整改中', '待验收', '已关闭', '已作废']
const selectedStatus = ref(0)
const reports = ref([])
const loading = ref(false)
const page = ref(0)
const pageSize = 10
const hasMore = ref(true)

const statusLabel = computed(() => `处理状态：${statusOptions[selectedStatus.value]}`)

onShow(() => {
  if (!ensureAuth()) return
  page.value = 0
  hasMore.value = true
  reports.value = []
  refresh()
})

async function loadData() {
  if (!hasMore.value) return
  loading.value = true
  try {
    const status = statusOptions[selectedStatus.value]
    const params = { page: page.value, size: pageSize }
    if (status !== '全部') {
      params.status = status
    }
    const res = await fetchMyHazards(params)
    if (Array.isArray(res)) {
      if (page.value === 0) {
        reports.value = res
      } else {
        reports.value = reports.value.concat(res)
      }
      if (res.length < pageSize) {
        hasMore.value = false
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// fetchMyHazards returns array, but we need headers for total. We cannot read headers here via uni.request
// so we load sequential pages until empty
async function refresh() {
  reports.value = []
  page.value = 0
  hasMore.value = true
  await loadData()
}

function onStatusChange(e) {
  selectedStatus.value = Number(e.detail.value)
  refresh()
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  page.value += 1
  loadData()
}

function openDetail(item) {
  uni.navigateTo({ url: `/pages/my/report-detail?hazardId=${item.hazardId}` })
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
.reports-page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
}
.filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.picker {
  padding: 16rpx 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.05);
}
.loading, .empty {
  text-align: center;
  margin-top: 60rpx;
  color: #909399;
}
.card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(15, 101, 202, 0.09);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.status {
  font-size: 28rpx;
  font-weight: 600;
}
.status-pending { color: #f59a23; }
.status-working { color: #409eff; }
.status-review { color: #e6a23c; }
.status-done { color: #67c23a; }
.description {
  font-size: 30rpx;
  color: #303133;
  margin-bottom: 18rpx;
}
.meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 24rpx;
}
.time {
  font-size: 24rpx;
  color: #909399;
}
</style>

<template>
  <view class="detail" v-if="alert">
    <view class="header"><text class="title">预警详情</text></view>
    <view class="row"><text class="label">时间</text><text class="val">{{ fmt(alert.occurredAt) }}</text></view>
    <view class="row"><text class="label">指标</text><text class="val">{{ alert.metricCode }}</text></view>
    <view class="row"><text class="label">地点</text><text class="val">{{ alert.location || '-' }}</text></view>
    <view class="row"><text class="label">级别</text><text class="val">{{ alert.severity || '-' }}</text></view>
    <view class="kb" v-if="(alert.kbRecommendations||[]).length">
      <view class="kb-title">推荐处置指引</view>
      <view class="kb-item" v-for="r in alert.kbRecommendations" :key="r.articleId">
        <text class="kb-item__title">{{ r.title }}</text>
        <text class="kb-item__summary">{{ r.summary }}</text>
      </view>
    </view>
  </view>
  <view v-else class="empty">未找到预警</view>
  <view class="ops"><button type="primary" @click="back">返回</button></view>
  
</template>

<script>
import { fetchMyAlerts } from '../../services/monitoring.js'
export default {
  data() { return { alert: null } },
  onLoad(query) { this.load(query?.id) },
  methods: {
    async load(id) {
      try {
        const list = await fetchMyAlerts({ id, includeKb: true })
        if (Array.isArray(list) && list.length) this.alert = list[0]
      } catch (e) {}
    },
    fmt(v) { if (!v) return '-'; const d = new Date(v); return d.toLocaleString('zh-CN', { hour12: false }) },
    back() { uni.navigateBack() }
  }
}
</script>

<style>
.detail { padding: 12px; }
.title { font-size: 18px; font-weight: 600; }
.row { display: flex; justify-content: space-between; margin: 6px 0; }
.label { color: #6b7280; }
.val { color: #111827; }
.kb { margin-top: 12px; }
.kb-title { font-weight: 600; margin-bottom: 6px; }
.kb-item { background: #fff; padding: 8px; border-radius: 8px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); margin-bottom: 8px; }
.kb-item__title { font-weight: 600; }
.kb-item__summary { display: block; color: #4b5563; margin-top: 4px; }
.empty { text-align: center; color: #9ca3af; margin-top: 16px; }
.ops { padding: 12px; }
</style>


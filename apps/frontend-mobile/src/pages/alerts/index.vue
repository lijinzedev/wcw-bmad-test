<template>
  <view class="alerts">
    <view class="header">
      <text class="title">我的预警</text>
      <picker mode="selector" :range="severityOptions" @change="onSeverityChange">
        <view class="picker">级别：{{ severity || '全部' }}</view>
      </picker>
      <switch :checked="onlyUnacked" @change="toggleUnacked">仅未确认</switch>
    </view>
    <view v-for="a in alerts" :key="a.alertId" class="card" @click="openDetail(a)">
      <view class="row"><text class="label">时间</text><text class="val">{{ fmt(a.occurredAt) }}</text></view>
      <view class="row"><text class="label">指标</text><text class="val">{{ a.metricCode }}</text></view>
      <view class="row"><text class="label">数值</text><text class="val">{{ a.measuredValue }} {{ a.unit || '' }}</text></view>
      <view class="row"><text class="label">地点</text><text class="val">{{ a.location || '-' }}</text></view>
      <view class="row"><text class="label">级别</text><text class="val">{{ a.severity || '-' }}</text></view>
      <view class="row ack">
        <text class="label">状态</text>
        <text class="badge" :class="{ on: a.acknowledged }">{{ a.acknowledged ? '已确认' : '未确认' }}</text>
      </view>
      <view class="ops">
        <button type="primary" size="mini" @click="toggleAck(a)">{{ a.acknowledged ? '取消确认' : '确认' }}</button>
      </view>
    </view>
    <view v-if="!alerts.length" class="empty">暂无预警</view>
  </view>
</template>

<script>
import { fetchMyAlerts, ackMyAlert, countMyAlerts } from '../../services/monitoring.js'
export default {
  data() {
    return { alerts: [], severity: '', onlyUnacked: true, severityOptions: ['HIGH','MEDIUM','LOW'], count: 0 }
  },
  onShow() { this.load(); this.refreshCount() },
  methods: {
    fmt(v) { if (!v) return '-'; const d = new Date(v); return d.toLocaleString('zh-CN', { hour12: false }) },
    async load() {
      try {
        const params = { severity: this.severity || undefined, acknowledged: this.onlyUnacked ? false : undefined }
        const data = await fetchMyAlerts(params)
        this.alerts = Array.isArray(data) ? data : []
      } catch (e) { /* toast handled in request */ }
    },
    async toggleAck(a) {
      try {
        const ack = !a.acknowledged
        const updated = await ackMyAlert(a.alertId, ack)
        Object.assign(a, updated)
        this.refreshCount()
      } catch (e) {}
    },
    openDetail(a) { try { uni.navigateTo({ url: `/pages/alerts/detail?id=${a.alertId}` }) } catch (e) {} },
    onSeverityChange(e) { const idx = Number(e.detail.value); this.severity = this.severityOptions[idx]; this.load() },
    toggleUnacked(e) { this.onlyUnacked = e.detail.value; this.load() },
    async refreshCount() { try { const r = await countMyAlerts(); this.count = r?.count || 0; uni.setTabBarBadge && uni.setTabBarBadge({ index: 0, text: String(this.count) }) } catch (e) {} }
  }
}
</script>

<style>
.alerts { padding: 12px; }
.header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.title { font-size: 18px; font-weight: 600; margin-right: 8px; }
.picker { padding: 6px 10px; background: #f3f4f6; border-radius: 6px; }
.card { background: #fff; border-radius: 8px; padding: 10px; margin-bottom: 10px; box-shadow: 0 1px 2px rgba(0,0,0,0.05) }
.row { display: flex; justify-content: space-between; margin: 4px 0; }
.label { color: #6b7280; }
.val { color: #111827; }
.badge { padding: 2px 8px; border-radius: 10px; background: #e5e7eb; }
.badge.on { background: #34d399; color: #fff; }
.ops { display: flex; justify-content: flex-end; margin-top: 8px; }
.empty { color: #9ca3af; text-align: center; margin-top: 12px; }
</style>

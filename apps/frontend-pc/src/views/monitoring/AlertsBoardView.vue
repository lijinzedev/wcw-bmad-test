<template>
  <div class="alerts">
    <header class="alerts__header">
      <h1>预警看板</h1>
      <div class="filters">
        <input v-model="metric" placeholder="指标代码" @input="loadAlerts" />
        <select v-model="severity" @change="loadAlerts">
          <option value="">全部级别</option>
          <option>HIGH</option>
          <option>MEDIUM</option>
          <option>LOW</option>
        </select>
        <label><input type="checkbox" v-model="onlyUnacked" @change="loadAlerts" /> 仅未确认</label>
      </div>
    </header>
    <table class="data-table">
      <thead><tr><th>时间</th><th>指标</th><th>数值</th><th>地点</th><th>级别</th><th>推荐指引</th><th>ACK</th><th>处理人</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="a in alerts" :key="a.alertId">
          <td>{{ fmt(a.occurredAt) }}</td>
          <td>{{ a.metricCode }}</td>
          <td>{{ a.measuredValue }} {{ a.unit || '' }}</td>
          <td>{{ a.location || '-' }}</td>
          <td>{{ a.severity || '-' }}</td>
          <td>
            <div v-if="(a.kbRecommendations||[]).length">
              <details>
                <summary>推荐指引 ({{ a.kbRecommendations.length }})</summary>
                <ul>
                  <li v-for="r in a.kbRecommendations" :key="r.articleId">{{ r.title }} — {{ r.summary }}</li>
                </ul>
              </details>
            </div>
            <span v-else class="empty">—</span>
          </td>
          <td><span :class="{ badge: true, on: a.acknowledged }">{{ a.acknowledged ? '已确认' : '未确认' }}</span></td>
          <td>{{ a.assigneeName || '-' }}</td>
          <td class="ops">
            <button @click="toggleAck(a)">{{ a.acknowledged ? '取消确认' : '确认' }}</button>
            <button @click="assign(a)">指派</button>
            <button :disabled="a.escalatedHazardId" @click="escalate(a)">{{ a.escalatedHazardId ? '已升级' : '升级为隐患' }}</button>
          </td>
        </tr>
        <tr v-if="!alerts.length"><td colspan="8" class="empty">暂无预警</td></tr>
      </tbody>
    </table>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="message" class="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchAlerts, ackAlert, assignAlert, escalateAlert } from '../../services/monitoring.js'

const alerts = ref([])
const error = ref('')
const message = ref('')
const metric = ref('')
const severity = ref('')
const onlyUnacked = ref(false)
const from = ref('')
const to = ref('')
const route = useRoute()

const fmt = v => v ? new Date(v).toLocaleString('zh-CN', { hour12: false }) : '-'

async function loadAlerts() {
  try {
    const params = {
      metricCode: metric.value,
      severity: severity.value,
      acknowledged: onlyUnacked.value ? false : undefined,
      includeKb: true,
      from: from.value || undefined,
      to: to.value || undefined
    }
    const { data } = await fetchAlerts(params)
    alerts.value = data
  } catch (e) { error.value = e.message }
}

async function toggleAck(a) {
  try {
    const updated = await ackAlert(a.alertId, !a.acknowledged)
    Object.assign(a, updated)
  } catch (e) { error.value = e.message }
}

async function assign(a) {
  const name = prompt('请输入指派对象姓名（临时占位）')
  if (!name) return
  try {
    const updated = await assignAlert(a.alertId, { assigneeName: name })
    Object.assign(a, updated)
  } catch (e) { error.value = e.message }
}

async function escalate(a) {
  if (!confirm('确认将该预警升级为隐患？')) return
  try {
    const resp = await escalateAlert(a.alertId)
    a.escalatedHazardId = resp.hazardId
    message.value = '已升级为隐患: ' + resp.hazardId
  } catch (e) { error.value = e.message }
}

function applyQueryFilters() {
  const query = route.query || {}
  metric.value = query.metric || ''
  severity.value = query.severity || ''
  from.value = query.from || ''
  to.value = query.to || ''
  onlyUnacked.value = query.onlyUnacked === 'true' ? true : false
}

onMounted(() => {
  applyQueryFilters()
  loadAlerts()
})

watch(() => route.query, () => {
  applyQueryFilters()
  loadAlerts()
})
</script>

<style scoped>
.alerts { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.alerts__header { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.filters { display: flex; gap: 8px; flex-wrap: wrap; }
.data-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
.data-table th, .data-table td { border: 1px solid #e5e7eb; padding: 8px; }
.data-table th { background: #f8fafc; }
.badge { padding: 2px 8px; border-radius: 12px; background: #e2e8f0; display: inline-block; }
.badge.on { background: #34d399; color: #fff; }
.ops button { margin-right: 6px; }
.empty { color: #94a3b8; text-align: center; }
.error { color: #dc2626; margin-top: 12px; }
.message { color: #0f766e; margin-top: 12px; }
</style>

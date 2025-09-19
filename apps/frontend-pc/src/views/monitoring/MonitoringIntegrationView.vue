<template>
  <div class="monitoring">
    <header class="monitoring__header">
      <h1>安全监控系统对接</h1>
      <p class="hint">配置异常阈值并查看最新预警，实现监控数据与双防系统联动。</p>
    </header>

    <section class="card">
      <header class="section-header">
        <h2>监控阈值 ({{ store.totals.thresholds }})</h2>
        <div class="actions">
          <input v-model="thresholdKeyword" placeholder="搜索指标" @input="loadThresholds" />
          <label>
            <input type="checkbox" v-model="onlyEnabled" @change="loadThresholds" /> 仅显示启用规则
          </label>
        </div>
      </header>
      <table class="data-table">
        <thead>
          <tr>
            <th>指标代码</th>
            <th>名称</th>
            <th>比较</th>
            <th>阈值</th>
            <th>严重级别</th>
            <th>地点匹配</th>
            <th>启用</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in store.thresholds" :key="item.thresholdId">
            <td>{{ item.metricCode }}</td>
            <td>{{ item.metricName || '-' }}</td>
            <td>{{ item.comparisonOperator }}</td>
            <td>{{ item.thresholdValue }} {{ item.unit || '' }}</td>
            <td>{{ item.severity || '-' }}</td>
            <td>{{ item.locationPattern || '全部' }}</td>
            <td><span :class="{ badge: true, on: item.enabled }">{{ item.enabled ? '启用' : '停用' }}</span></td>
            <td>
              <button @click="editThreshold(item)">编辑</button>
              <button @click="removeThreshold(item.thresholdId)">删除</button>
            </td>
          </tr>
          <tr v-if="!store.thresholds.length">
            <td colspan="8" class="empty">暂无阈值配置</td>
          </tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveThreshold">
        <h3>{{ thresholdForm.thresholdId ? '编辑阈值' : '新增阈值' }}</h3>
        <div class="form-row">
          <label>指标代码<input v-model="thresholdForm.metricCode" required /></label>
          <label>指标名称<input v-model="thresholdForm.metricName" /></label>
          <label>比较方式
            <select v-model="thresholdForm.comparisonOperator" required>
              <option value="">请选择</option>
              <option value=">">大于</option>
              <option value=">=">大于等于</option>
              <option value="<">小于</option>
              <option value="<=">小于等于</option>
              <option value="==">等于</option>
              <option value="GREATER_THAN">GREATER_THAN</option>
              <option value="GREATER_THAN_OR_EQUAL">GREATER_THAN_OR_EQUAL</option>
              <option value="LESS_THAN">LESS_THAN</option>
              <option value="LESS_THAN_OR_EQUAL">LESS_THAN_OR_EQUAL</option>
              <option value="EQUAL">EQUAL</option>
            </select>
          </label>
        </div>
        <div class="form-row">
          <label>阈值<input type="number" step="0.01" v-model.number="thresholdForm.thresholdValue" required /></label>
          <label>单位<input v-model="thresholdForm.unit" /></label>
          <label>严重级别<input v-model="thresholdForm.severity" /></label>
        </div>
        <div class="form-row">
          <label>地点匹配<input v-model="thresholdForm.locationPattern" placeholder="可选，支持包含匹配" /></label>
          <label class="checkbox"><input type="checkbox" v-model="thresholdForm.enabled" /> 启用</label>
        </div>
        <div class="form-actions">
          <button type="submit">保存</button>
          <button type="button" @click="resetThresholdForm">重置</button>
        </div>
      </form>
    </section>

    <section class="card">
      <header class="section-header">
        <h2>预警列表 ({{ store.totals.alerts }})</h2>
        <div class="actions">
          <input v-model="alertMetric" placeholder="指标代码" @input="loadAlerts" />
          <input type="datetime-local" v-model="alertFrom" @change="loadAlerts" />
          <input type="datetime-local" v-model="alertTo" @change="loadAlerts" />
          <button type="button" @click="triggerPoll">立即拉取</button>
        </div>
      </header>
      <table class="data-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>指标</th>
            <th>数值</th>
            <th>地点</th>
            <th>严重级别</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in store.alerts" :key="item.alertId">
            <td>{{ fmtDate(item.occurredAt) }}</td>
            <td>{{ item.metricCode }}</td>
            <td>{{ item.measuredValue }}</td>
            <td>{{ item.location || '-' }}</td>
            <td>{{ item.severity || '-' }}</td>
            <td>{{ item.message || '-' }}</td>
          </tr>
          <tr v-if="!store.alerts.length">
            <td colspan="6" class="empty">暂无预警记录</td>
          </tr>
        </tbody>
      </table>

      <p v-if="store.pollingStatus" class="poll-status">
        最近轮询：{{ fmtDate(store.pollingStatus.completedAt) }} - {{ store.pollingStatus.status }} ({{ store.pollingStatus.message || '无说明' }})
      </p>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="message" class="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  fetchThresholds,
  createThreshold,
  updateThreshold,
  deleteThreshold,
  fetchAlerts,
  triggerPoll as apiTriggerPoll
} from '../../services/monitoring.js'
import { useMonitoringStore, setThresholds, setAlerts, setPollingStatus } from '../../stores/monitoringStore.js'

const store = useMonitoringStore()
const error = ref('')
const message = ref('')

const thresholdKeyword = ref('')
const onlyEnabled = ref(true)
const alertMetric = ref('')
const alertFrom = ref('')
const alertTo = ref('')

const thresholdForm = reactive({
  thresholdId: null,
  metricCode: '',
  metricName: '',
  comparisonOperator: '',
  thresholdValue: null,
  unit: '',
  severity: '',
  locationPattern: '',
  enabled: true
})

const fmtDate = value => {
  if (!value) return '-'
  const date = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', { hour12: false })
}

const clone = value => JSON.parse(JSON.stringify(value ?? {}))

async function loadThresholds() {
  try {
    const { data, total } = await fetchThresholds({ keyword: thresholdKeyword.value, enabled: onlyEnabled.value })
    setThresholds(data, total)
  } catch (e) {
    error.value = e.message
  }
}

async function loadAlerts() {
  try {
    const params = {
      metricCode: alertMetric.value,
      from: alertFrom.value ? new Date(alertFrom.value).toISOString() : undefined,
      to: alertTo.value ? new Date(alertTo.value).toISOString() : undefined
    }
    const { data, total } = await fetchAlerts(params)
    setAlerts(data, total)
  } catch (e) {
    error.value = e.message
  }
}

async function saveThreshold() {
  try {
    const payload = clone(thresholdForm)
    if (!payload.comparisonOperator) {
      payload.comparisonOperator = 'GREATER_THAN'
    }
    if (payload.thresholdId) {
      await updateThreshold(payload.thresholdId, payload)
      message.value = '阈值已更新'
    } else {
      await createThreshold(payload)
      message.value = '阈值已创建'
    }
    resetThresholdForm()
    await loadThresholds()
  } catch (e) {
    error.value = e.message
  }
}

async function removeThreshold(id) {
  if (!confirm('确定删除该阈值吗？')) return
  try {
    await deleteThreshold(id)
    await loadThresholds()
  } catch (e) {
    error.value = e.message
  }
}

function editThreshold(item) {
  Object.assign(thresholdForm, clone(item))
}

function resetThresholdForm() {
  Object.assign(thresholdForm, {
    thresholdId: null,
    metricCode: '',
    metricName: '',
    comparisonOperator: '',
    thresholdValue: null,
    unit: '',
    severity: '',
    locationPattern: '',
    enabled: true
  })
}

async function triggerPollAndRefresh() {
  try {
    const result = await apiTriggerPoll()
    setPollingStatus(result)
    message.value = result.message || '已触发轮询'
    await loadAlerts()
  } catch (e) {
    error.value = e.message
  }
}

async function triggerPoll() {
  await triggerPollAndRefresh()
}

onMounted(async () => {
  await loadThresholds()
  await loadAlerts()
})
</script>

<style scoped>
.monitoring { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.monitoring__header { margin-bottom: 16px; }
.hint { color: #64748b; margin-top: 4px; }
.card { margin-bottom: 24px; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; background: #fff; }
.section-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.actions input { padding: 6px 10px; }
.data-table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
.data-table th, .data-table td { border: 1px solid #e5e7eb; padding: 8px; }
.data-table th { background: #f8fafc; }
.empty { color: #94a3b8; text-align: center; }
.badge { padding: 2px 8px; border-radius: 12px; background: #e2e8f0; display: inline-block; }
.badge.on { background: #34d399; color: #fff; }
.form { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.form-row { display: flex; gap: 12px; flex-wrap: wrap; }
.form label { display: flex; flex-direction: column; gap: 4px; flex: 1 1 220px; }
.form input, .form select { padding: 6px 8px; border: 1px solid #cbd5f5; border-radius: 4px; }
.checkbox { align-items: center; flex-direction: row; gap: 8px; }
.form-actions { display: flex; gap: 8px; }
.form button { padding: 8px 16px; }
.poll-status { color: #2563eb; }
.error { color: #dc2626; margin-top: 12px; }
.message { color: #0f766e; margin-top: 12px; }
@media (max-width: 768px) {
  .form-row { flex-direction: column; }
  .actions { flex-direction: column; align-items: flex-start; }
}
</style>

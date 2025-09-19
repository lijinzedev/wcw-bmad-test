<template>
  <div class="notify">
    <header><h1>预警通知联动</h1><p class="hint">配置通知规则与查看发送记录。</p></header>

    <section class="card">
      <header class="section-header">
        <h2>通知规则 ({{ store.totals.rules }})</h2>
        <div class="actions">
          <input v-model="keyword" placeholder="搜索(指标/地点/级别)" @input="loadRules" />
          <label><input type="checkbox" v-model="onlyEnabled" @change="loadRules" /> 仅启用</label>
        </div>
      </header>
      <table class="data-table">
        <thead><tr><th>指标</th><th>地点匹配</th><th>级别</th><th>渠道</th><th>收件人</th><th>启用</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in store.rules" :key="r.ruleId">
            <td>{{ r.metricCode }}</td>
            <td>{{ r.locationPattern || '-' }}</td>
            <td>{{ r.severity || '-' }}</td>
            <td>{{ (r.channels||[]).join(',') }}</td>
            <td>{{ (r.recipients||[]).join(',') }}</td>
            <td><span :class="{ badge: true, on: r.enabled }">{{ r.enabled ? '启用' : '停用' }}</span></td>
            <td>
              <button @click="editRule(r)">编辑</button>
              <button @click="removeRule(r.ruleId)">删除</button>
              <button @click="testRule(r)">测试发送</button>
            </td>
          </tr>
          <tr v-if="!store.rules.length"><td colspan="7" class="empty">暂无规则</td></tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveRule">
        <h3>{{ form.ruleId ? '编辑规则' : '新增规则' }}</h3>
        <div class="form-row">
          <label>指标代码<input v-model="form.metricCode" required /></label>
          <label>地点匹配<input v-model="form.locationPattern" placeholder="包含匹配，可留空" /></label>
          <label>严重级别<input v-model="form.severity" placeholder="可留空" /></label>
        </div>
        <div class="form-row">
          <label>渠道<input v-model="channelsInput" placeholder="SMS,PUSH" /></label>
          <label>收件人<input v-model="recipientsInput" placeholder="逗号分隔" /></label>
          <label class="checkbox"><input type="checkbox" v-model="form.enabled" /> 启用</label>
        </div>
        <div class="form-actions"><button type="submit">保存</button><button type="button" @click="resetForm">重置</button></div>
      </form>
    </section>

    <section class="card">
      <header class="section-header">
        <h2>发送记录 ({{ store.totals.logs }})</h2>
        <div class="actions">
          <select v-model="filterChannel" @change="loadLogs"><option value="">全部渠道</option><option>SMS</option><option>PUSH</option></select>
          <select v-model="filterStatus" @change="loadLogs"><option value="">全部状态</option><option>PENDING</option><option>SUCCESS</option><option>FAILED</option></select>
        </div>
      </header>
      <table class="data-table">
        <thead><tr><th>时间</th><th>渠道</th><th>收件人</th><th>主题</th><th>状态</th><th>错误</th></tr></thead>
        <tbody>
          <tr v-for="l in store.logs" :key="l.logId">
            <td>{{ fmt(l.createdAt) }}</td>
            <td>{{ l.channel }}</td>
            <td>{{ (l.recipients||[]).join(',') }}</td>
            <td>{{ l.subject }}</td>
            <td>{{ l.status }}</td>
            <td>{{ l.error || '-' }}</td>
          </tr>
          <tr v-if="!store.logs.length"><td colspan="6" class="empty">暂无记录</td></tr>
        </tbody>
      </table>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="message" class="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { fetchRules, createRule, updateRule, deleteRule, testSend, fetchLogs } from '../../services/notification.js'
import { useNotificationStore, setRules, setLogs } from '../../stores/notificationStore.js'

const store = useNotificationStore()
const error = ref('')
const message = ref('')
const keyword = ref('')
const onlyEnabled = ref(true)
const filterChannel = ref('')
const filterStatus = ref('')

const form = reactive({ ruleId: null, metricCode: '', locationPattern: '', severity: '', channels: [], recipients: [], enabled: true })
const channelsInput = ref('SMS')
const recipientsInput = ref('')

const fmt = v => v ? new Date(v).toLocaleString('zh-CN', { hour12: false }) : '-'
const clone = v => JSON.parse(JSON.stringify(v ?? {}))

async function loadRules() {
  try {
    const { data, total } = await fetchRules({ keyword: keyword.value, enabled: onlyEnabled.value })
    setRules(data, total)
  } catch (e) { error.value = e.message }
}

async function loadLogs() {
  try {
    const { data, total } = await fetchLogs({ channel: filterChannel.value, status: filterStatus.value })
    setLogs(data, total)
  } catch (e) { error.value = e.message }
}

async function saveRule() {
  try {
    const payload = clone(form)
    payload.channels = String(channelsInput.value || '').split(',').map(s => s.trim()).filter(Boolean)
    payload.recipients = String(recipientsInput.value || '').split(',').map(s => s.trim()).filter(Boolean)
    if (payload.ruleId) await updateRule(payload.ruleId, payload); else await createRule(payload)
    resetForm(); await loadRules(); message.value = '已保存规则'
  } catch (e) { error.value = e.message }
}

async function removeRule(id) {
  if (!confirm('确定删除该规则吗？')) return
  try { await deleteRule(id); await loadRules() } catch (e) { error.value = e.message }
}

function editRule(r) {
  Object.assign(form, clone(r))
  channelsInput.value = (r.channels || []).join(',')
  recipientsInput.value = (r.recipients || []).join(',')
}

function resetForm() {
  Object.assign(form, { ruleId: null, metricCode: '', locationPattern: '', severity: '', channels: [], recipients: [], enabled: true })
  channelsInput.value = 'SMS'; recipientsInput.value = ''
}

async function testRule(r) {
  try { await testSend(r.ruleId); message.value = '测试通知已提交' } catch (e) { error.value = e.message }
}

onMounted(async () => { await loadRules(); await loadLogs() })
</script>

<style scoped>
.notify { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.hint { color: #64748b; margin: 4px 0 16px; }
.card { margin-bottom: 24px; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; background: #fff; }
.section-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
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
.error { color: #dc2626; margin-top: 12px; }
.message { color: #0f766e; margin-top: 12px; }
</style>


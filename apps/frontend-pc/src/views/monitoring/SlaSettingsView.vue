<template>
  <div class="sla">
    <header><h1>预警SLA配置</h1><p class="hint">按指标/级别配置确认与处置时限与升级策略。</p></header>
    <section class="card">
      <header class="section-header">
        <h2>规则 ({{ store.totals.rules }})</h2>
        <div class="actions">
          <input v-model="keyword" placeholder="搜索(指标/地点/级别)" @input="loadRules" />
          <label><input type="checkbox" v-model="onlyEnabled" @change="loadRules" /> 仅启用</label>
        </div>
      </header>
      <table class="data-table">
        <thead><tr><th>指标</th><th>级别</th><th>地点匹配</th><th>确认(分)</th><th>处置(分)</th><th>渠道</th><th>收件人</th><th>启用</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in store.rules" :key="r.ruleId">
            <td>{{ r.metricCode }}</td>
            <td>{{ r.severity || '-' }}</td>
            <td>{{ r.locationPattern || '-' }}</td>
            <td>{{ r.ackDeadlineMinutes ?? '-' }}</td>
            <td>{{ r.resolveDeadlineMinutes ?? '-' }}</td>
            <td>{{ (r.escalationChannels||[]).join(',') }}</td>
            <td>{{ (r.escalationRecipients||[]).join(',') }}</td>
            <td><span :class="{ badge: true, on: r.enabled }">{{ r.enabled ? '启用' : '停用' }}</span></td>
            <td>
              <button @click="editRule(r)">编辑</button>
              <button @click="removeRule(r.ruleId)">删除</button>
            </td>
          </tr>
          <tr v-if="!store.rules.length"><td colspan="9" class="empty">暂无规则</td></tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveRule">
        <h3>{{ form.ruleId ? '编辑规则' : '新增规则' }}</h3>
        <div class="form-row">
          <label>指标<input v-model="form.metricCode" required /></label>
          <label>级别<input v-model="form.severity" placeholder="可留空" /></label>
          <label>地点匹配<input v-model="form.locationPattern" placeholder="包含匹配，可留空" /></label>
        </div>
        <div class="form-row">
          <label>确认时限(分)<input type="number" v-model.number="form.ackDeadlineMinutes" min="0" /></label>
          <label>处置时限(分)<input type="number" v-model.number="form.resolveDeadlineMinutes" min="0" /></label>
          <label class="checkbox"><input type="checkbox" v-model="form.autoEscalateToHazard" /> 自动升级为隐患</label>
        </div>
        <div class="form-row">
          <label>渠道<input v-model="channelsInput" placeholder="SMS,PUSH" /></label>
          <label>收件人<input v-model="recipientsInput" placeholder="逗号分隔" /></label>
          <label class="checkbox"><input type="checkbox" v-model="form.enabled" /> 启用</label>
        </div>
        <div class="form-actions"><button type="submit">保存</button><button type="button" @click="resetForm">重置</button></div>
      </form>
    </section>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="message" class="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { fetchSlaRules, createSlaRule, updateSlaRule, deleteSlaRule } from '../../services/sla.js'
import { useSlaStore, setSlaRules } from '../../stores/slaStore.js'

const store = useSlaStore()
const error = ref('')
const message = ref('')
const keyword = ref('')
const onlyEnabled = ref(true)
const form = reactive({ ruleId: null, metricCode: '', severity: '', locationPattern: '', ackDeadlineMinutes: null, resolveDeadlineMinutes: null, autoEscalateToHazard: false, enabled: true })
const channelsInput = ref('SMS')
const recipientsInput = ref('')

async function loadRules() {
  try {
    const { data, total } = await fetchSlaRules({ keyword: keyword.value, enabled: onlyEnabled.value })
    setSlaRules(data, total)
  } catch (e) { error.value = e.message }
}

async function saveRule() {
  try {
    const payload = { ...form, escalationChannels: String(channelsInput.value||'').split(',').map(s=>s.trim()).filter(Boolean), escalationRecipients: String(recipientsInput.value||'').split(',').map(s=>s.trim()).filter(Boolean) }
    if (payload.ruleId) await updateSlaRule(payload.ruleId, payload); else await createSlaRule(payload)
    message.value = '已保存规则'; resetForm(); await loadRules()
  } catch (e) { error.value = e.message }
}

async function removeRule(id) { if (!confirm('确定删除该规则吗？')) return; try { await deleteSlaRule(id); await loadRules() } catch (e) { error.value = e.message } }

function editRule(r) { Object.assign(form, r); channelsInput.value = (r.escalationChannels||[]).join(','); recipientsInput.value = (r.escalationRecipients||[]).join(',') }
function resetForm() { Object.assign(form, { ruleId: null, metricCode: '', severity: '', locationPattern: '', ackDeadlineMinutes: null, resolveDeadlineMinutes: null, autoEscalateToHazard: false, enabled: true }); channelsInput.value = 'SMS'; recipientsInput.value = '' }

onMounted(loadRules)
</script>

<style scoped>
.sla { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.hint { color: #64748b; margin: 4px 0 16px; }
.card { margin-bottom: 24px; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; background: #fff; }
.section-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { border: 1px solid #e5e7eb; padding: 8px; }
.badge { padding: 2px 8px; border-radius: 12px; background: #e2e8f0; display: inline-block; }
.badge.on { background: #34d399; color: #fff; }
.form { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.form-row { display: flex; gap: 12px; flex-wrap: wrap; }
.form label { display: flex; flex-direction: column; gap: 4px; flex: 1 1 220px; }
.form input { padding: 6px 8px; border: 1px solid #cbd5f5; border-radius: 4px; }
.form-actions { display: flex; gap: 8px; }
.error { color: #dc2626; margin-top: 12px; }
.message { color: #0f766e; margin-top: 12px; }
</style>


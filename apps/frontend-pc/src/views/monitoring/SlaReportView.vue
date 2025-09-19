<template>
  <div class="sla-report">
    <header class="sla-report__header">
      <h1>预警SLA报表</h1>
      <div class="filters">
        <select v-model="type" @change="loadBreaches">
          <option value="">全部类型</option>
          <option value="ACK">确认超时</option>
          <option value="RESOLVE">处置超时</option>
        </select>
      </div>
    </header>
    <section class="card">
      <h2>摘要</h2>
      <p>确认达标率：{{ (store.summary.ackOnTimeRate||0)*100 | toPct }}%，处置达标率：{{ (store.summary.resolveOnTimeRate||0)*100 | toPct }}%</p>
    </section>
    <section class="card">
      <h2>违约列表 ({{ store.totals.breaches }})</h2>
      <table class="data-table">
        <thead><tr><th>时间</th><th>类型</th><th>指标</th><th>地点</th><th>级别</th><th>预期时间</th></tr></thead>
        <tbody>
          <tr v-for="b in store.breaches" :key="b.breachId">
            <td>{{ fmt(b.createdAt) }}</td>
            <td>{{ b.type }}</td>
            <td>{{ b.metricCode }}</td>
            <td>{{ b.location || '-' }}</td>
            <td>{{ b.severity || '-' }}</td>
            <td>{{ fmt(b.expectedAt) }}</td>
          </tr>
          <tr v-if="!store.breaches.length"><td colspan="6" class="empty">暂无违约记录</td></tr>
        </tbody>
      </table>
    </section>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchSlaSummary, fetchSlaBreaches } from '../../services/sla.js'
import { useSlaStore, setSlaSummary, setSlaBreaches } from '../../stores/slaStore.js'

const store = useSlaStore()
const error = ref('')
const type = ref('')

const fmt = v => v ? new Date(v).toLocaleString('zh-CN', { hour12: false }) : '-'

async function loadSummary() {
  try { const s = await fetchSlaSummary({}); setSlaSummary(s) } catch (e) { error.value = e.message }
}
async function loadBreaches() {
  try { const { data, total } = await fetchSlaBreaches({ type: type.value }); setSlaBreaches(data, total) } catch (e) { error.value = e.message }
}

onMounted(async () => { await loadSummary(); await loadBreaches() })
</script>

<script>
export default { filters: { toPct(v){ return Number(v).toFixed(1) } } }
</script>

<style scoped>
.sla-report { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.filters { display: flex; gap: 8px; flex-wrap: wrap; }
.card { margin-top: 12px; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; background: #fff; }
.data-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
.data-table th, .data-table td { border: 1px solid #e5e7eb; padding: 8px; }
.empty { color: #94a3b8; text-align: center; }
.error { color: #dc2626; margin-top: 12px; }
</style>


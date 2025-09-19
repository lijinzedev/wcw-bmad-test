<template>
  <div class="accident-dashboard">
    <header class="toolbar">
      <div class="filters">
        <label>
          起始时间
          <input type="datetime-local" v-model="filters.from" />
        </label>
        <label>
          截止时间
          <input type="datetime-local" v-model="filters.to" />
        </label>
        <label>
          事故类型
          <input v-model="filters.type" placeholder="如：FALL" />
        </label>
        <label>
          责任单位 ID
          <input v-model="filters.organizationId" placeholder="UUID" />
        </label>
        <button @click="load">查询</button>
        <button @click="reset">重置</button>
      </div>
      <div class="actions">
        <button @click="create">新建事故</button>
      </div>
    </header>

    <section class="summary">
      <div>
        <h3>事故趋势</h3>
        <table v-if="trends.length">
          <thead>
            <tr><th>日期</th><th>事故数</th><th>死亡</th><th>受伤</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in trends" :key="item.bucket">
              <td>{{ formatDay(item.bucket) }}</td>
              <td>{{ item.total }}</td>
              <td>{{ item.fatalities }}</td>
              <td>{{ item.injuries }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">暂无趋势数据</p>
      </div>
      <div>
        <h3>高频风险点</h3>
        <table v-if="topRisks.length">
          <thead>
            <tr><th>风险描述</th><th>类别</th><th>关联次数</th></tr>
          </thead>
          <tbody>
            <tr v-for="risk in topRisks" :key="risk.riskId">
              <td>{{ risk.riskDescription }}</td>
              <td>{{ risk.category }}</td>
              <td>{{ risk.occurrences }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">暂无高频风险数据</p>
      </div>
    </section>

    <section class="list">
      <h3>事故记录</h3>
      <table v-if="accidents.length">
        <thead>
          <tr>
            <th>时间</th>
            <th>标题</th>
            <th>地点</th>
            <th>类型</th>
            <th>死亡/伤</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in accidents" :key="item.accidentId">
            <td>{{ formatDate(item.occurredAt) }}</td>
            <td>{{ item.title || '(未命名)' }}</td>
            <td>{{ item.location || '-' }}</td>
            <td>{{ item.accidentType || '-' }}</td>
            <td>{{ item.fatalityCount || 0 }} / {{ item.injuryCount || 0 }}</td>
            <td>{{ item.status || '-' }}</td>
            <td>
              <button @click="edit(item.accidentId)">查看/编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">暂无事故记录</p>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  fetchAccidents,
  fetchAccidentTrends,
  fetchAccidentTopRisks
} from '../../services/analysis.js'
import {
  setAccidents,
  setAccidentTrends,
  setAccidentTopRisks,
  useAnalysisStore
} from '../../stores/analysisStore.js'

const router = useRouter()
const store = useAnalysisStore()
const accidents = ref([])
const error = ref('')
const filters = ref({ from: '', to: '', type: '', organizationId: '' })

const trends = ref([])
const topRisks = ref([])

onMounted(() => {
  accidents.value = store.accidents
  trends.value = store.accidentTrends
  topRisks.value = store.accidentTopRisks
  load()
})

async function load() {
  error.value = ''
  try {
    const query = buildQuery()
    const { data } = await fetchAccidents(query)
    accidents.value = data
    setAccidents(data)

    const trendData = await fetchAccidentTrends(query)
    trends.value = Array.isArray(trendData) ? trendData : []
    setAccidentTrends(trends.value)

    const riskData = await fetchAccidentTopRisks({ ...query, limit: 5 })
    topRisks.value = Array.isArray(riskData) ? riskData : []
    setAccidentTopRisks(topRisks.value)
  } catch (e) {
    error.value = e.message || '加载失败'
  }
}

function buildQuery() {
  const params = {}
  if (filters.value.from) params.from = new Date(filters.value.from).toISOString()
  if (filters.value.to) params.to = new Date(filters.value.to).toISOString()
  if (filters.value.type) params.type = filters.value.type
  if (filters.value.organizationId) params.organizationId = filters.value.organizationId
  params.size = 50
  return params
}

function reset() {
  filters.value = { from: '', to: '', type: '', organizationId: '' }
  load()
}

function create() {
  router.push({ name: 'accident-create' })
}

function edit(id) {
  router.push({ name: 'accident-edit', params: { id } })
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2,'0')}-${date.getDate().toString().padStart(2,'0')} ${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`
}

function formatDay(value) {
  if (!value) return '-'
  const date = new Date(value)
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2,'0')}-${date.getDate().toString().padStart(2,'0')}`
}
</script>

<style scoped>
.accident-dashboard { padding: 16px; text-align: left; }
.toolbar { display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 12px; }
.filters { display: flex; flex-wrap: wrap; gap: 12px; }
.filters label { display: flex; flex-direction: column; font-size: 14px; }
.filters input { margin-top: 4px; padding: 6px; font-size: 14px; min-width: 180px; }
.actions button { padding: 8px 12px; }
.summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; margin: 20px 0; }
.summary table, .list table { width: 100%; border-collapse: collapse; }
.summary th, .summary td, .list th, .list td { border: 1px solid #ddd; padding: 6px; font-size: 13px; }
.list { margin-top: 24px; }
.empty { color: #666; font-size: 13px; }
.error { color: #d33; margin-top: 16px; }
button { cursor: pointer; }
</style>

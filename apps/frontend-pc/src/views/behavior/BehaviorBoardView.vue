<template>
  <div class="behavior-board">
    <h1>不安全行为台账</h1>
    <div class="actions">
      <div class="filters">
        <label>
          状态
          <select v-model="filters.status">
            <option value="">全部</option>
            <option value="未处理">未处理</option>
            <option value="已处理">已处理</option>
          </select>
        </label>
        <label>
          行为类型
          <input v-model="filters.behaviorType" placeholder="如：违章操作" />
        </label>
        <label>
          当事人
          <input v-model="filters.personName" placeholder="姓名" />
        </label>
        <label>
          起始时间
          <input type="datetime-local" v-model="filters.from" />
        </label>
        <label>
          截止时间
          <input type="datetime-local" v-model="filters.to" />
        </label>
        <button @click="query">查询</button>
        <button @click="reset">重置</button>
      </div>
      <div class="toolbar">
        <button @click="goCreate">录入行为</button>
        <button @click="downloadExport" :disabled="loading">导出台账</button>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="loading" class="loading">加载中...</div>

    <table v-if="behaviors.length" class="table">
      <thead>
        <tr>
          <th>发生时间</th>
          <th>当事人</th>
          <th>类型</th>
          <th>地点</th>
          <th>状态</th>
          <th>处理人</th>
          <th>处理时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in behaviors" :key="item.behaviorId">
          <td>{{ formatDateTime(item.occurredAt) }}</td>
          <td>{{ item.personName || '-' }}</td>
          <td>{{ item.behaviorType || '-' }}</td>
          <td>{{ item.location || '-' }}</td>
          <td><span :class="['tag', item.status === '已处理' ? 'tag-success' : 'tag-pending']">{{ item.status }}</span></td>
          <td>{{ item.handlerName || '-' }}</td>
          <td>{{ formatDateTime(item.handledAt) }}</td>
          <td><button @click="openDetail(item.behaviorId)">详情</button></td>
        </tr>
      </tbody>
    </table>
    <div v-else-if="!loading" class="empty">暂无数据</div>

    <div class="pager">
      <button @click="prev" :disabled="page === 0 || loading">上一页</button>
      <span>第 {{ page + 1 }} / {{ totalPages }} 页，共 {{ total }} 条</span>
      <button @click="next" :disabled="page + 1 >= totalPages || loading">下一页</button>
    </div>

    <section class="stats" v-if="stats">
      <h2>统计分析</h2>
      <div class="stat-grid">
        <div>
          <h3>类型排行</h3>
          <ol>
            <li v-for="item in stats.byType" :key="item.name">{{ item.name }}：{{ item.count }}</li>
          </ol>
        </div>
        <div>
          <h3>人员排行</h3>
          <ol>
            <li v-for="item in stats.byPerson" :key="item.name">{{ item.name }}：{{ item.count }}</li>
          </ol>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchBehaviors, exportBehaviors, fetchBehaviorStats } from '../../services/behavior.js'

const router = useRouter()
const filters = ref({ status: '', behaviorType: '', personName: '', from: '', to: '' })
const behaviors = ref([])
const stats = ref(null)
const loading = ref(false)
const error = ref('')
const page = ref(0)
const size = ref(10)
const total = ref(0)
const totalPages = ref(1)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data, total: count, totalPages: pages } = await fetchBehaviors({
      page: page.value,
      size: size.value,
      status: filters.value.status,
      behaviorType: filters.value.behaviorType,
      personName: filters.value.personName,
      from: toIso(filters.value.from),
      to: toIso(filters.value.to)
    })
    behaviors.value = Array.isArray(data) ? data : []
    total.value = count
    totalPages.value = pages
  } catch (e) {
    error.value = e.message || '加载失败'
    behaviors.value = []
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    stats.value = await fetchBehaviorStats()
  } catch (e) {
    console.warn('加载统计失败', e)
  }
}

function query() {
  page.value = 0
  load()
  loadStats()
}

function reset() {
  filters.value = { status: '', behaviorType: '', personName: '', from: '', to: '' }
  page.value = 0
  load()
  loadStats()
}

function prev() {
  if (page.value === 0) return
  page.value -= 1
  load()
}

function next() {
  if (page.value + 1 >= totalPages.value) return
  page.value += 1
  load()
}

function goCreate() {
  router.push({ name: 'behavior-create' })
}

function openDetail(id) {
  router.push({ name: 'behavior-edit', params: { id } })
}

async function downloadExport() {
  try {
    const blob = await exportBehaviors({
      status: filters.value.status,
      behaviorType: filters.value.behaviorType,
      personName: filters.value.personName,
      from: toIso(filters.value.from),
      to: toIso(filters.value.to)
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'behaviors.csv'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    error.value = e.message || '导出失败'
  }
}

function formatDateTime(value) {
  if (!value) return '-'
  try {
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return value
  }
}

function toIso(value) {
  return value ? new Date(value).toISOString() : ''
}

onMounted(() => {
  load()
  loadStats()
})
</script>

<style scoped>
.behavior-board {
  padding: 24px;
  display: grid;
  gap: 16px;
}
.actions {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.filters label {
  display: grid;
  gap: 4px;
  font-size: 14px;
}
.toolbar {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  border: 1px solid #e5e7eb;
  padding: 8px;
}
.pager {
  display: flex;
  gap: 12px;
  align-items: center;
}
.loading {
  color: #1d4ed8;
}
.error {
  color: #e11d48;
}
.empty {
  color: #6b7280;
}
.tag {
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 12px;
}
.tag-success { background: #bbf7d0; color: #166534; }
.tag-pending { background: #fde68a; color: #92400e; }
.stats {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}
.stat-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}
ol {
  margin: 0;
  padding-left: 20px;
}
</style>

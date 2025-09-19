<template>
  <div class="hazards-board">
    <h1>隐患闭环管理</h1>
    <div class="filters">
      <label>
        状态
        <select v-model="filters.status">
          <option value="">全部</option>
          <option v-for="item in statusOptions" :key="item" :value="item">{{ item }}</option>
        </select>
      </label>
      <label>
        关键词
        <input v-model="filters.q" placeholder="描述或地点" />
      </label>
      <button @click="runQuery" :disabled="loading">查询</button>
      <button @click="reset" :disabled="loading">重置</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="loading" class="loading">数据加载中...</div>

    <table v-if="hazards.length" class="hazard-table">
      <thead>
        <tr>
          <th>描述</th>
          <th>状态</th>
          <th>等级</th>
          <th>负责人</th>
          <th>截止日期</th>
          <th>更新时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="hazard in hazards" :key="hazard.hazardId">
          <td>{{ hazard.description }}</td>
          <td><span :class="['tag', `tag-${statusTag(hazard.status)}`]">{{ hazard.status }}</span></td>
          <td>{{ hazard.level || '-' }}</td>
          <td>{{ shortId(hazard.rectifierId) }}</td>
          <td>{{ formatDate(hazard.rectificationDeadline) }}</td>
          <td>{{ formatDateTime(hazard.updatedAt || hazard.reportedAt) }}</td>
          <td><button @click="openDetail(hazard.hazardId)">详情</button></td>
        </tr>
      </tbody>
    </table>

    <div v-else-if="!loading" class="empty">暂无隐患数据</div>

    <div class="pager">
      <button @click="prev" :disabled="page === 0 || loading">上一页</button>
      <span>第 {{ page + 1 }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button @click="next" :disabled="page + 1 >= totalPages || loading">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchHazards } from '../../services/hazard.js'

const router = useRouter()
const statusOptions = ['待指派', '整改中', '待验收', '已关闭', '已作废']
const filters = ref({ status: '', q: '' })
const hazards = ref([])
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
    const { data, total: count, totalPages: pages } = await fetchHazards({
      page: page.value,
      size: size.value,
      status: filters.value.status,
      q: filters.value.q
    })
    hazards.value = Array.isArray(data) ? data : []
    total.value = count || hazards.value.length
    totalPages.value = pages || 1
  } catch (e) {
    error.value = e.message || '加载失败'
    hazards.value = []
  } finally {
    loading.value = false
  }
}

function runQuery() {
  page.value = 0
  load()
}

function reset() {
  filters.value = { status: '', q: '' }
  page.value = 0
  load()
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

function openDetail(id) {
  router.push({ name: 'hazard-detail', params: { id } })
}

function shortId(id) {
  if (!id) return '-'
  return id.slice(0, 8)
}

function formatDate(value) {
  if (!value) return '-'
  return value
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

function statusTag(status) {
  if (!status) return 'default'
  if (status.includes('指派')) return 'pending'
  if (status.includes('整改')) return 'working'
  if (status.includes('验收')) return 'review'
  if (status.includes('关闭')) return 'success'
  return 'default'
}

onMounted(load)
</script>

<style scoped>
.hazards-board {
  padding: 24px;
}
.filters {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 16px;
}
.filters label {
  display: grid;
  gap: 4px;
  font-size: 14px;
}
.hazard-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}
.hazard-table th,
.hazard-table td {
  border: 1px solid #e5e7eb;
  padding: 8px;
  text-align: left;
}
.hazard-table tr:hover {
  background: #f9fafb;
}
.pager {
  display: flex;
  gap: 12px;
  align-items: center;
}
.error {
  color: #e11d48;
  margin-bottom: 8px;
}
.loading {
  margin: 12px 0;
  color: #1d4ed8;
}
.empty {
  margin: 24px 0;
  color: #6b7280;
}
.tag {
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 12px;
}
.tag-pending { background: #fde68a; color: #92400e; }
.tag-working { background: #bfdbfe; color: #1d4ed8; }
.tag-review { background: #fcd34d; color: #92400e; }
.tag-success { background: #bbf7d0; color: #166534; }
.tag-default { background: #e5e7eb; color: #374151; }
</style>

<template>
  <div class="inspection-board">
    <h1>检查计划管理</h1>
    <div class="filters">
      <label>级别
        <select v-model="filters.level">
          <option value="">全部</option>
          <option value="COMPANY">公司级</option>
          <option value="GROUP">集团级</option>
        </select>
      </label>
      <label>矿井
        <input v-model="filters.mineName" placeholder="矿井名称" />
      </label>
      <label>状态
        <select v-model="filters.status">
          <option value="">全部</option>
          <option value="计划中">计划中</option>
          <option value="执行中">执行中</option>
          <option value="已完成">已完成</option>
        </select>
      </label>
      <label>开始时间
        <input type="datetime-local" v-model="filters.from" />
      </label>
      <label>结束时间
        <input type="datetime-local" v-model="filters.to" />
      </label>
      <button @click="query">查询</button>
      <button @click="reset">重置</button>
      <button @click="downloadExport" :disabled="loading">导出计划</button>
      <button @click="goCreate">新建计划</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="loading" class="loading">加载中...</div>

    <table class="table" v-if="plans.length">
      <thead>
        <tr>
          <th>标题</th>
          <th>级别</th>
          <th>矿井</th>
          <th>状态</th>
          <th>时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="plan in plans" :key="plan.planId">
          <td>{{ plan.title }}</td>
          <td>{{ plan.level }}</td>
          <td>{{ plan.mineName || '-' }}</td>
          <td>{{ plan.status }}</td>
          <td>{{ formatDateTime(plan.startAt) }} - {{ formatDateTime(plan.endAt) }}</td>
          <td><button @click="openDetail(plan.planId)">详情</button></td>
        </tr>
      </tbody>
    </table>
    <div v-else-if="!loading" class="empty">暂无计划</div>

    <div class="pager">
      <button @click="prev" :disabled="page === 0 || loading">上一页</button>
      <span>第 {{ page + 1 }} / {{ totalPages }} 页，共 {{ total }} 条</span>
      <button @click="next" :disabled="page + 1 >= totalPages || loading">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchPlans, exportPlans } from '../../services/inspection.js'

const router = useRouter()
const filters = ref({ level: '', mineName: '', status: '', from: '', to: '' })
const plans = ref([])
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
    const { data, total: count, totalPages: pages } = await fetchPlans({
      page: page.value,
      size: size.value,
      level: filters.value.level,
      mineName: filters.value.mineName,
      status: filters.value.status,
      from: toIso(filters.value.from),
      to: toIso(filters.value.to)
    })
    plans.value = Array.isArray(data) ? data : []
    total.value = count
    totalPages.value = pages
  } catch (e) {
    error.value = e.message || '加载失败'
    plans.value = []
  } finally {
    loading.value = false
  }
}

function query() {
  page.value = 0
  load()
}

function reset() {
  filters.value = { level: '', mineName: '', status: '', from: '', to: '' }
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

function goCreate() {
  router.push({ name: 'inspection-create' })
}

function openDetail(id) {
  router.push({ name: 'inspection-detail', params: { id } })
}

async function downloadExport() {
  try {
    const blob = await exportPlans({
      level: filters.value.level,
      mineName: filters.value.mineName,
      status: filters.value.status,
      from: toIso(filters.value.from),
      to: toIso(filters.value.to)
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'inspection-plans.csv'
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
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } catch (e) {
    return value
  }
}

function toIso(value) {
  return value ? new Date(value).toISOString() : ''
}

onMounted(load)
</script>

<style scoped>
.inspection-board {
  padding: 24px;
  display: grid;
  gap: 16px;
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
}
.filters label {
  display: grid;
  gap: 4px;
  font-size: 14px;
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
.error { color: #e11d48; }
.loading { color: #1d4ed8; }
.empty { color: #6b7280; }
</style>

<template>
  <div class="risks">
    <h2>风险清单</h2>
    <div class="filters">
      <input v-model="filters.q" placeholder="关键词搜索(描述/地点)" />
      <input v-model="filters.category" placeholder="专业" />
      <input v-model="filters.location" placeholder="区域/地点" />
      <select v-model="filters.level">
        <option value="">风险等级</option>
        <option>重大</option>
        <option>较大</option>
        <option>一般</option>
        <option>低</option>
      </select>
      <button @click="load">查询</button>
      <button @click="reset">重置</button>
      <button @click="exportExcel">导出Excel</button>
      <button @click="goCreate">新增</button>
    </div>
    <table class="list">
      <thead>
        <tr>
          <th>描述</th><th>专业</th><th>地点</th><th>等级</th><th>责任单位</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in risks" :key="r.riskId">
          <td>{{ r.description }}</td>
          <td>{{ r.category }}</td>
          <td>{{ r.location }}</td>
          <td>{{ r.level }}</td>
          <td>{{ r.responsibleOrgId?.slice(0,8) }}</td>
          <td><button @click="goEdit(r.riskId)">编辑</button></td>
        </tr>
      </tbody>
    </table>
    <div class="pager">
      <button :disabled="page===0" @click="page--; load()">上一页</button>
      <span>第 {{ page+1 }} / {{ totalPages }} 页, 共 {{ total }} 条</span>
      <button :disabled="page+1>=totalPages" @click="page++; load()">下一页</button>
    </div>
  </div>
  <div v-if="error" class="error">{{ error }}</div>
  <div v-if="loading">加载中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../services/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const filters = ref({ q: '', category: '', location: '', level: '' })
const risks = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(0)
const size = ref(10)
const total = ref(0)
const totalPages = ref(1)

async function load() {
  loading.value = true; error.value = ''
  const params = new URLSearchParams({ page: String(page.value), size: String(size.value) })
  for (const k of ['q','category','location','level']) {
    if (filters.value[k]) params.set(k, filters.value[k])
  }
  try {
    const resp = await fetch(`/api/v1/risks?${params.toString()}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('auth_token') || ''}` }
    })
    if (!resp.ok) throw new Error(`加载失败 ${resp.status}`)
    const list = await resp.json()
    risks.value = list
    total.value = Number(resp.headers.get('X-Total-Count') || '0')
    totalPages.value = Number(resp.headers.get('X-Total-Pages') || '1')
  } catch (e) {
    error.value = e.message || '请求失败'
  } finally {
    loading.value = false
  }
}

function reset() {
  filters.value = { q: '', category: '', location: '', level: '' }
  page.value = 0
  load()
}

async function exportExcel() {
  try {
    const params = new URLSearchParams()
    for (const k of ['q','category','location','level']) {
      if (filters.value[k]) params.set(k, filters.value[k])
    }
    const blob = await api.download(`/api/v1/risks/export?${params.toString()}`)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'risks.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    error.value = e.message || '导出失败'
  }
}

function goCreate() { router.push({ name: 'risk-create' }) }
function goEdit(id) { router.push({ name: 'risk-edit', params: { id } }) }

onMounted(load)
</script>

<style scoped>
.filters { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
.list { width: 100%; border-collapse: collapse; }
.list th, .list td { border: 1px solid #ddd; padding: 6px; }
.pager { margin-top: 8px; }
.error { color: #c00; margin-top: 8px; }
</style>

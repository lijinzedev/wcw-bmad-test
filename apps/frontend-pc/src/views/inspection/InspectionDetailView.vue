<template>
  <div class="inspection-detail" v-if="plan">
    <h1>检查计划详情</h1>
    <section class="info">
      <div><strong>标题：</strong>{{ plan.title }}</div>
      <div><strong>级别：</strong>{{ plan.level }}</div>
      <div><strong>矿井：</strong>{{ plan.mineName || '-' }}</div>
      <div><strong>状态：</strong>{{ plan.status }}</div>
      <div><strong>时间：</strong>{{ formatDateTime(plan.startAt) }} - {{ formatDateTime(plan.endAt) }}</div>
      <div><strong>备注：</strong>{{ plan.notes || '-' }}</div>
    </section>

    <section class="actions">
      <button @click="goEdit">编辑计划</button>
      <button @click="loadReport">生成报告</button>
      <a v-if="reportUrl" :href="reportUrl" download>下载报告</a>
    </section>

    <section class="records">
      <h2>执行记录</h2>
      <table v-if="plan.records && plan.records.length">
        <thead>
          <tr>
            <th>项目</th>
            <th>结果</th>
            <th>备注</th>
            <th>隐患ID</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in plan.records" :key="record.recordId">
            <td>{{ record.item }}</td>
            <td>{{ record.result }}</td>
            <td>{{ record.remarks || '-' }}</td>
            <td>{{ record.hazardId || '-' }}</td>
            <td>{{ formatDateTime(record.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无执行记录</div>
    </section>

    <section class="record-form">
      <h2>新增执行记录</h2>
      <form @submit.prevent="submitRecord">
        <label>检查项<input v-model="recordForm.item" required /></label>
        <label>结果
          <select v-model="recordForm.result" required>
            <option value="符合">符合</option>
            <option value="存在隐患">存在隐患</option>
          </select>
        </label>
        <label>备注<textarea v-model="recordForm.remarks" /></label>
        <label class="checkbox">
          <input type="checkbox" v-model="recordForm.createHazard" /> 创建督办隐患
        </label>
        <div v-if="recordForm.createHazard" class="hazard-fields">
          <label>隐患描述<textarea v-model="recordForm.hazard.description" /></label>
          <label>隐患等级<input v-model="recordForm.hazard.level" placeholder="重大/一般" /></label>
          <label>隐患地点<input v-model="recordForm.hazard.location" /></label>
        </div>
        <label>附件<input type="file" multiple @change="onFiles" /></label>
        <ul v-if="files.length" class="files">
          <li v-for="file in files" :key="file.name">{{ file.name }}</li>
        </ul>
        <div class="form-actions">
          <button type="submit" :disabled="saving">提交记录</button>
          <span v-if="recordError" class="error">{{ recordError }}</span>
        </div>
      </form>
    </section>
  </div>
  <div v-else class="loading">加载中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPlan, addRecord, fetchReport } from '../../services/inspection.js'

const route = useRoute()
const router = useRouter()
const plan = ref(null)
const files = ref([])
const saving = ref(false)
const recordError = ref('')
const reportUrl = ref('')
const recordForm = ref({
  item: '',
  result: '符合',
  remarks: '',
  createHazard: false,
  hazard: { description: '', level: '', location: '' }
})

async function loadPlan() {
  plan.value = await fetchPlan(route.params.id)
}

onMounted(loadPlan)

function goEdit() {
  router.push({ name: 'inspection-edit', params: { id: route.params.id } })
}

async function loadReport() {
  const text = await fetchReport(route.params.id)
  const blob = new Blob([text], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  reportUrl.value = url
}

function onFiles(event) {
  files.value = Array.from(event.target.files || [])
}

async function submitRecord() {
  saving.value = true
  recordError.value = ''
  try {
    await addRecord(route.params.id, recordForm.value, files.value)
    recordForm.value = { item: '', result: '符合', remarks: '', createHazard: false, hazard: { description: '', level: '', location: '' } }
    files.value = []
    await loadPlan()
  } catch (e) {
    recordError.value = e.message || '提交失败'
  } finally {
    saving.value = false
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
</script>

<style scoped>
.inspection-detail {
  padding: 24px;
  display: grid;
  gap: 20px;
}
.info {
  display: grid;
  gap: 8px;
}
.records table {
  width: 100%;
  border-collapse: collapse;
}
.records th,
.records td {
  border: 1px solid #e5e7eb;
  padding: 8px;
}
.empty { color: #6b7280; }
.record-form form {
  display: grid;
  gap: 12px;
  max-width: 720px;
}
.checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
}
.files {
  list-style: disc;
  margin-left: 20px;
}
.form-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.error { color: #e11d48; }
.loading { padding: 40px; text-align: center; }
</style>

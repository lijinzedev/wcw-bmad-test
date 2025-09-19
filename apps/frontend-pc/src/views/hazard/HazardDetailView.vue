<template>
  <div class="hazard-detail" v-if="hazard">
    <h1>隐患详情</h1>
    <section class="info">
      <h2>基础信息</h2>
      <div class="grid">
        <div><strong>描述：</strong>{{ hazard.description }}</div>
        <div><strong>状态：</strong>{{ hazard.status }}</div>
        <div><strong>等级：</strong>{{ hazard.level || '-' }}</div>
        <div><strong>上报时间：</strong>{{ formatDateTime(hazard.reportedAt) }}</div>
        <div><strong>整改截止：</strong>{{ hazard.rectificationDeadline || '-' }}</div>
        <div><strong>整改负责人：</strong>{{ hazard.rectifierId || '-' }}</div>
        <div><strong>验收人：</strong>{{ hazard.verifierId || '-' }}</div>
      </div>
    </section>

    <section class="timeline">
      <h2>流程记录</h2>
      <ul v-if="hazard.updates && hazard.updates.length">
        <li v-for="update in hazard.updates" :key="update.updateId">
          <div class="timeline-row">
            <span class="action">{{ translateAction(update.action) }}</span>
            <span class="time">{{ formatDateTime(update.timestamp) }}</span>
          </div>
          <div class="details" v-if="update.details">{{ update.details }}</div>
          <ul class="attachments" v-if="update.attachments && update.attachments.length">
            <li v-for="att in update.attachments" :key="att.key">
              <a :href="att.url" target="_blank" rel="noopener">{{ att.originalName || att.key }}</a>
            </li>
          </ul>
        </li>
      </ul>
      <div v-else class="empty">暂无流程记录</div>
    </section>

    <section v-if="isAdmin" class="panel" data-test="assign-panel">
      <h2>指派整改</h2>
      <div class="form">
        <label>整改负责人
          <select v-model="assignForm.rectifierId">
            <option value="">请选择</option>
            <option v-for="user in users" :key="user.userId" :value="user.userId">{{ user.fullName || user.username }}</option>
          </select>
        </label>
        <label>截止日期
          <input type="date" v-model="assignForm.rectificationDeadline" />
        </label>
        <label>验收人（可选）
          <select v-model="assignForm.verifierId">
            <option value="">请选择</option>
            <option v-for="user in users" :key="`verifier-${user.userId}`" :value="user.userId">{{ user.fullName || user.username }}</option>
          </select>
        </label>
        <label>备注
          <textarea v-model="assignForm.note" placeholder="指派说明"></textarea>
        </label>
        <button @click="submitAssign" :disabled="assigning">指派</button>
        <span v-if="assignError" class="error">{{ assignError }}</span>
      </div>
    </section>

    <section class="panel" data-test="update-panel">
      <h2>整改记录</h2>
      <div class="form">
        <label>说明
          <textarea v-model="updateForm.details" placeholder="输入整改情况"></textarea>
        </label>
        <label class="checkbox">
          <input type="checkbox" v-model="updateForm.completed" /> 已完成整改，提交验收
        </label>
        <label>附件
          <input type="file" multiple @change="onFilesChange" />
        </label>
        <ul class="selected-files" v-if="selectedFiles.length">
          <li v-for="file in selectedFiles" :key="file.name">{{ file.name }}</li>
        </ul>
        <button @click="submitUpdate" :disabled="updating">提交记录</button>
        <span v-if="updateError" class="error">{{ updateError }}</span>
      </div>
    </section>

    <section v-if="isAdmin" class="panel" data-test="review-panel">
      <h2>验收操作</h2>
      <div class="form">
        <label>验收结果
          <select v-model="reviewForm.approved">
            <option :value="true">通过</option>
            <option :value="false">驳回</option>
          </select>
        </label>
        <label>说明
          <textarea v-model="reviewForm.details" placeholder="验收意见"></textarea>
        </label>
        <button @click="submitReview" :disabled="reviewing">提交验收</button>
        <span v-if="reviewError" class="error">{{ reviewError }}</span>
      </div>
    </section>
  </div>
  <div v-else-if="loading" class="loading">加载中...</div>
  <div v-else class="error">{{ error || '无法加载隐患详情' }}</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { fetchHazardDetail, assignHazard, submitHazardUpdate, reviewHazard, fetchAssignableUsers } from '../../services/hazard.js'
import { auth } from '../../services/auth.js'

const route = useRoute()
const hazard = ref(null)
const loading = ref(true)
const error = ref('')

const assignForm = ref({ rectifierId: '', rectificationDeadline: '', verifierId: '', note: '' })
const assigning = ref(false)
const assignError = ref('')

const updateForm = ref({ details: '', completed: false })
const updating = ref(false)
const updateError = ref('')
const selectedFiles = ref([])

const reviewForm = ref({ approved: true, details: '' })
const reviewing = ref(false)
const reviewError = ref('')

const users = ref([])

const isAdmin = auth.getRoles().includes('ADMIN')

async function loadHazard() {
  loading.value = true
  error.value = ''
  try {
    hazard.value = await fetchHazardDetail(route.params.id)
    assignForm.value.rectifierId = hazard.value.rectifierId || ''
    assignForm.value.verifierId = hazard.value.verifierId || ''
  } catch (e) {
    error.value = e.message || '加载失败'
    hazard.value = null
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  if (!isAdmin) return
  try {
    users.value = await fetchAssignableUsers()
  } catch (e) {
    console.warn('无法加载用户列表', e)
  }
}

async function submitAssign() {
  if (!assignForm.value.rectifierId) {
    assignError.value = '请选择整改负责人'
    return
  }
  assignError.value = ''
  assigning.value = true
  try {
    await assignHazard(route.params.id, assignForm.value)
    await loadHazard()
  } catch (e) {
    assignError.value = e.message || '指派失败'
  } finally {
    assigning.value = false
  }
}

function onFilesChange(event) {
  const files = Array.from(event.target.files || [])
  selectedFiles.value = files
}

async function submitUpdate() {
  if (!updateForm.value.details.trim() && !selectedFiles.value.length) {
    updateError.value = '请至少填写说明或上传附件'
    return
  }
  updateError.value = ''
  updating.value = true
  try {
    await submitHazardUpdate(route.params.id, updateForm.value, selectedFiles.value)
    updateForm.value = { details: '', completed: false }
    selectedFiles.value = []
    await loadHazard()
  } catch (e) {
    updateError.value = e.message || '提交失败'
  } finally {
    updating.value = false
  }
}

async function submitReview() {
  reviewError.value = ''
  reviewing.value = true
  try {
    await reviewHazard(route.params.id, reviewForm.value)
    await loadHazard()
  } catch (e) {
    reviewError.value = e.message || '验收失败'
  } finally {
    reviewing.value = false
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

function translateAction(action) {
  switch (action) {
    case 'REPORTED':
      return '上报'
    case 'ASSIGNED':
      return '指派'
    case 'UPDATED':
      return '整改进展'
    case 'SUBMITTED':
      return '整改完成'
    case 'APPROVED':
      return '验收通过'
    case 'REJECTED':
      return '验收驳回'
    default:
      return action || '记录'
  }
}

onMounted(async () => {
  await Promise.all([loadHazard(), loadUsers()])
})
</script>

<style scoped>
.hazard-detail {
  padding: 24px;
  display: grid;
  gap: 24px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}
.timeline ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 16px;
}
.timeline-row {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
}
.attachments {
  margin-top: 8px;
  list-style: none;
  padding-left: 0;
}
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}
.form {
  display: grid;
  gap: 12px;
}
.form label {
  display: grid;
  gap: 6px;
  font-size: 14px;
}
textarea {
  min-height: 80px;
}
.checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
}
.error {
  color: #e11d48;
}
.loading {
  padding: 32px;
  text-align: center;
}
.selected-files {
  list-style: disc;
  margin-left: 20px;
  font-size: 14px;
}
</style>

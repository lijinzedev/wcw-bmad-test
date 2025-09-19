<template>
  <div class="behavior-form" v-if="ready">
    <h1>{{ isEdit ? '编辑不安全行为' : '录入不安全行为' }}</h1>
    <form @submit.prevent="submit">
      <label>发生时间<input type="datetime-local" v-model="form.occurredAt" required /></label>
      <label>地点<input v-model="form.location" placeholder="地点" /></label>
      <label>当事人姓名<input v-model="form.personName" required /></label>
      <label>行为类型<input v-model="form.behaviorType" required /></label>
      <label>行为描述<textarea v-model="form.description" required /></label>
      <label>违反规定<textarea v-model="form.ruleViolated" /></label>
      <label>处理措施<textarea v-model="form.actionTaken" /></label>
      <label>状态
        <select v-model="form.status">
          <option value="未处理">未处理</option>
          <option value="已处理">已处理</option>
        </select>
      </label>
      <label>附件<input type="file" multiple @change="onFiles" /></label>
      <ul class="files" v-if="files.length">
        <li v-for="file in files" :key="file.name">{{ file.name }}</li>
      </ul>
      <div class="actions">
        <button type="submit" :disabled="saving">{{ isEdit ? '保存' : '提交' }}</button>
        <button type="button" @click="back">返回</button>
      </div>
      <div v-if="error" class="error">{{ error }}</div>
    </form>
    <section v-if="behavior && behavior.actions && behavior.actions.length" class="timeline">
      <h2>处理记录</h2>
      <ul>
        <li v-for="item in behavior.actions" :key="item.actionId">
          <div class="row">
            <span>{{ translateAction(item.action) }}</span>
            <span>{{ formatDateTime(item.createdAt) }}</span>
          </div>
          <div class="details" v-if="item.details">{{ item.details }}</div>
          <ul class="attachments" v-if="item.attachments && item.attachments.length">
            <li v-for="att in item.attachments" :key="att.key"><a :href="att.url" target="_blank">{{ att.originalName || att.key }}</a></li>
          </ul>
        </li>
      </ul>
    </section>
  </div>
  <div v-else class="loading">加载中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createBehavior, updateBehavior, fetchBehaviorDetail, recordBehaviorAction } from '../../services/behavior.js'

const router = useRouter()
const route = useRoute()
const behavior = ref(null)
const ready = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const error = ref('')
const files = ref([])

const form = ref({
  occurredAt: '',
  location: '',
  personName: '',
  behaviorType: '',
  description: '',
  ruleViolated: '',
  actionTaken: '',
  status: '未处理'
})

onMounted(async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    try {
      const data = await fetchBehaviorDetail(id)
      behavior.value = data
      populateForm(data)
    } catch (e) {
      error.value = e.message || '加载失败'
    }
  } else {
    form.value.occurredAt = new Date().toISOString().slice(0, 16)
  }
  ready.value = true
})

function populateForm(data) {
  form.value = {
    occurredAt: data.occurredAt ? data.occurredAt.slice(0, 16) : '',
    location: data.location || '',
    personName: data.personName || '',
    behaviorType: data.behaviorType || '',
    description: data.description || '',
    ruleViolated: data.ruleViolated || '',
    actionTaken: data.actionTaken || '',
    status: data.status || '未处理'
  }
}

async function submit() {
  saving.value = true
  error.value = ''
  const payload = {
    occurredAt: form.value.occurredAt ? new Date(form.value.occurredAt).toISOString() : null,
    location: form.value.location,
    personName: form.value.personName,
    behaviorType: form.value.behaviorType,
    description: form.value.description,
    ruleViolated: form.value.ruleViolated,
    actionTaken: form.value.actionTaken,
    status: form.value.status
  }
  try {
    if (isEdit.value) {
      await updateBehavior(route.params.id, payload, files.value)
      if (form.value.status === '已处理') {
        await recordBehaviorAction(route.params.id, { handled: true, details: form.value.actionTaken })
      }
    } else {
      await createBehavior(payload, files.value)
    }
    router.push({ name: 'behavior-board' })
  } catch (e) {
    error.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function back() {
  router.back()
}

function onFiles(event) {
  files.value = Array.from(event.target.files || [])
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
    case 'CREATED': return '录入'
    case 'UPDATED': return '更新'
    case 'HANDLED': return '已处理'
    default: return action || '记录'
  }
}
</script>

<style scoped>
.behavior-form {
  padding: 24px;
  max-width: 720px;
  margin: 0 auto;
  display: grid;
  gap: 16px;
}
form {
  display: grid;
  gap: 12px;
}
label {
  display: grid;
  gap: 6px;
  font-size: 14px;
}
textarea {
  min-height: 80px;
}
.actions {
  display: flex;
  gap: 12px;
}
.error {
  color: #e11d48;
}
.loading {
  padding: 32px;
  text-align: center;
}
.files {
  list-style: disc;
  margin-left: 20px;
  font-size: 14px;
}
.timeline {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}
.timeline ul {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 12px;
}
.row {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
}
.attachments {
  list-style: disc;
  margin-left: 20px;
}
</style>

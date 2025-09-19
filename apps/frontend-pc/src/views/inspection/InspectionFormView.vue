<template>
  <div class="inspection-form">
    <h1>{{ isEdit ? '编辑检查计划' : '新建检查计划' }}</h1>
    <form @submit.prevent="submit">
      <label>计划标题<input v-model="form.title" required /></label>
      <label>级别
        <select v-model="form.level" required>
          <option value="">请选择</option>
          <option value="COMPANY">公司级</option>
          <option value="GROUP">集团级</option>
        </select>
      </label>
      <label>矿井名称<input v-model="form.mineName" /></label>
      <label>计划范围<textarea v-model="form.scope" /></label>
      <label>开始时间<input type="datetime-local" v-model="form.startAt" /></label>
      <label>结束时间<input type="datetime-local" v-model="form.endAt" /></label>
      <label>状态
        <select v-model="form.status" required>
          <option value="计划中">计划中</option>
          <option value="执行中">执行中</option>
          <option value="已完成">已完成</option>
        </select>
      </label>
      <label>备注<textarea v-model="form.notes" /></label>
      <div class="actions">
        <button type="submit" :disabled="saving">保存</button>
        <button type="button" @click="back">返回</button>
      </div>
      <div v-if="error" class="error">{{ error }}</div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPlan, createPlan, updatePlan } from '../../services/inspection.js'

const router = useRouter()
const route = useRoute()
const isEdit = ref(false)
const saving = ref(false)
const error = ref('')
const form = ref({
  title: '',
  level: 'COMPANY',
  mineName: '',
  scope: '',
  startAt: '',
  endAt: '',
  status: '计划中',
  notes: ''
})

onMounted(async () => {
  if (route.params.id) {
    isEdit.value = true
    try {
      const data = await fetchPlan(route.params.id)
      form.value = {
        title: data.title,
        level: data.level,
        mineName: data.mineName || '',
        scope: data.scope || '',
        startAt: data.startAt ? data.startAt.slice(0, 16) : '',
        endAt: data.endAt ? data.endAt.slice(0, 16) : '',
        status: data.status || '计划中',
        notes: data.notes || ''
      }
    } catch (e) {
      error.value = e.message || '加载失败'
    }
  }
})

async function submit() {
  saving.value = true
  error.value = ''
  const payload = {
    title: form.value.title,
    level: form.value.level,
    mineName: form.value.mineName,
    scope: form.value.scope,
    startAt: form.value.startAt ? new Date(form.value.startAt).toISOString() : null,
    endAt: form.value.endAt ? new Date(form.value.endAt).toISOString() : null,
    status: form.value.status,
    notes: form.value.notes
  }
  try {
    if (isEdit.value) {
      await updatePlan(route.params.id, payload)
    } else {
      await createPlan(payload)
    }
    router.push({ name: 'inspection-board' })
  } catch (e) {
    error.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function back() {
  router.back()
}
</script>

<style scoped>
.inspection-form {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px;
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
.error { color: #e11d48; }
</style>

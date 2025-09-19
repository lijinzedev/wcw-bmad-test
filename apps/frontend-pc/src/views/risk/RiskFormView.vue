<template>
  <div class="risk-form">
    <h2>{{ isEdit ? '编辑风险' : '新增风险' }}</h2>
    <form @submit.prevent="submit">
      <label>描述<input v-model="form.description" required /></label>
      <label>专业<input v-model="form.category" /></label>
      <label>地点<input v-model="form.location" /></label>
      <label>等级
        <select v-model="form.level">
          <option value="">请选择</option>
          <option>重大</option>
          <option>较大</option>
          <option>一般</option>
          <option>低</option>
        </select>
      </label>
      <label>管控措施<textarea v-model="form.controlMeasures" /></label>
      <div class="actions">
        <button type="submit">保存</button>
        <button type="button" @click="goBack">取消</button>
      </div>
    </form>
    <div v-if="error" class="error">{{ error }}</div>
  </div>
  <div v-if="loading">加载中...</div>
  
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { api } from '../../services/auth'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const isEdit = computed(() => !!id)
const loading = ref(false)
const error = ref('')
const form = ref({ description: '', category: '', location: '', level: '', controlMeasures: '' })

async function load() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const headers = { 'Authorization': `Bearer ${localStorage.getItem('auth_token') || ''}` }
    const resp = await fetch(`/api/v1/risks/${id}`, { headers })
    if (!resp.ok) throw new Error(`加载失败 ${resp.status}`)
    const data = await resp.json()
    form.value = {
      description: data.description || '',
      category: data.category || '',
      location: data.location || '',
      level: data.level || '',
      controlMeasures: data.controlMeasures || ''
    }
  } catch (e) {
    error.value = e.message || '请求失败'
  } finally { loading.value = false }
}

async function submit() {
  error.value = ''
  try {
    if (isEdit.value) {
      await api.put(`/api/v1/risks/${id}`, form.value)
    } else {
      await api.post('/api/v1/risks', form.value)
    }
    router.push({ name: 'risks' })
  } catch (e) {
    error.value = e.message || '保存失败'
  }
}

function goBack() { router.back() }

onMounted(load)
</script>

<style scoped>
form { display: grid; gap: 8px; max-width: 480px; }
label { display: grid; gap: 4px; }
textarea { min-height: 80px; }
.actions { display: flex; gap: 8px; margin-top: 8px; }
.error { color: #b00; margin-top: 8px; }
</style>


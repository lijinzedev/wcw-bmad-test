<template>
  <div class="accident-form">
    <h1>{{ isEdit ? '编辑事故' : '新建事故' }}</h1>
    <form @submit.prevent="save">
      <section class="grid">
        <label>
          事故标题
          <input v-model="form.title" placeholder="如：井筒提升事故" required />
        </label>
        <label>
          发生时间
          <input type="datetime-local" v-model="form.occurredAtLocal" required />
        </label>
        <label>
          事故类型
          <input v-model="form.accidentType" placeholder="如：FALL" />
        </label>
        <label>
          严重级别
          <select v-model="form.severity">
            <option value="">选择严重级别</option>
            <option>特别重大</option>
            <option>重大</option>
            <option>较大</option>
            <option>一般</option>
          </select>
        </label>
      </section>

      <section class="grid">
        <label>
          发生地点
          <input v-model="form.location" placeholder="地点" />
        </label>
        <label>
          责任单位 ID
          <input v-model="form.organizationId" placeholder="UUID" />
        </label>
        <label>
          责任单位名称
          <input v-model="form.organizationName" placeholder="单位名称" />
        </label>
      </section>

      <section class="grid numbers">
        <label>
          死亡人数
          <input type="number" min="0" v-model.number="form.fatalityCount" />
        </label>
        <label>
          受伤人数
          <input type="number" min="0" v-model.number="form.injuryCount" />
        </label>
        <label>
          经济损失（万元）
          <input type="number" step="0.01" v-model.number="form.economicLoss" />
        </label>
      </section>

      <label>
        伤亡情况摘要
        <textarea v-model="form.casualtySummary" rows="2"></textarea>
      </label>

      <label>
        事故描述
        <textarea v-model="form.description" rows="4" placeholder="事故经过、原因等"></textarea>
      </label>

      <label>
        当前状态
        <input v-model="form.status" placeholder="调查中 / 已整改等" />
      </label>

      <section class="reference">
        <div class="dual">
          <div>
            <h3>关联风险</h3>
            <div class="lookup">
              <input v-model="riskKeyword" placeholder="搜索风险" @keyup.enter.prevent="loadRisks" />
              <button type="button" @click="loadRisks">搜索</button>
            </div>
            <ul class="options">
              <li v-for="risk in riskOptions" :key="risk.riskId">
                <button type="button" @click="toggleRisk(risk.riskId)">
                  {{ selectedRiskIds.includes(risk.riskId) ? '移除' : '关联' }}
                </button>
                <span class="code">{{ risk.riskId.slice(0, 8) }}</span>
                <span>{{ risk.description }}</span>
              </li>
            </ul>
            <p v-if="!riskOptions.length" class="hint">无匹配风险，可调整检索条件。</p>
          </div>
          <div>
            <h3>关联隐患</h3>
            <div class="lookup">
              <input v-model="hazardKeyword" placeholder="搜索隐患" @keyup.enter.prevent="loadHazards" />
              <button type="button" @click="loadHazards">搜索</button>
            </div>
            <ul class="options">
              <li v-for="item in hazardOptions" :key="item.hazardId">
                <button type="button" @click="toggleHazard(item.hazardId)">
                  {{ selectedHazardIds.includes(item.hazardId) ? '移除' : '关联' }}
                </button>
                <span class="code">{{ item.hazardId.slice(0, 8) }}</span>
                <span>{{ item.description }}</span>
              </li>
            </ul>
            <p v-if="!hazardOptions.length" class="hint">无匹配隐患，可调整检索条件。</p>
          </div>
        </div>
        <div class="selected">
          <h4>已关联风险</h4>
          <ul>
            <li v-for="id in selectedRiskIds" :key="id">{{ id }}</li>
          </ul>
          <h4>已关联隐患</h4>
          <ul>
            <li v-for="id in selectedHazardIds" :key="id">{{ id }}</li>
          </ul>
        </div>
      </section>

      <section class="attachments">
        <header>
          <h3>调查资料</h3>
          <input type="file" multiple @change="handleFileChange" />
        </header>
        <ul>
          <li v-for="(file, index) in form.attachments" :key="file.key">
            <span>{{ file.originalName }} ({{ formatSize(file.size) }})</span>
            <button type="button" @click="removeAttachment(index)">移除</button>
          </li>
        </ul>
        <p v-if="uploadMessage" class="hint">{{ uploadMessage }}</p>
      </section>

      <div class="actions">
        <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存事故' }}</button>
        <button type="button" @click="goBack">返回</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="message" class="message">{{ message }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auth } from '../../services/auth.js'
import { fetchAccident, createAccident, updateAccident, presignAccidentAttachment } from '../../services/analysis.js'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = ref(emptyForm())
const saving = ref(false)
const error = ref('')
const message = ref('')
const uploadMessage = ref('')

const riskKeyword = ref('')
const hazardKeyword = ref('')
const riskOptions = ref([])
const hazardOptions = ref([])

const selectedRiskIds = computed(() => form.value.relatedRiskIds)
const selectedHazardIds = computed(() => form.value.relatedHazardIds)

function emptyForm() {
  return {
    title: '',
    occurredAtLocal: localDateTime(new Date()),
    location: '',
    organizationId: '',
    organizationName: '',
    accidentType: '',
    severity: '',
    fatalityCount: 0,
    injuryCount: 0,
    casualtySummary: '',
    economicLoss: null,
    description: '',
    status: '',
    relatedRiskIds: [],
    relatedHazardIds: [],
    attachments: []
  }
}

onMounted(async () => {
  await Promise.all([loadRisks(), loadHazards()])
  if (isEdit.value) {
    await loadAccident()
  }
})

async function loadAccident() {
  try {
    const data = await fetchAccident(route.params.id)
    form.value = {
      title: data.title || '',
      occurredAtLocal: data.occurredAt ? localDateTime(new Date(data.occurredAt)) : localDateTime(new Date()),
      location: data.location || '',
      organizationId: data.organizationId || '',
      organizationName: data.organizationName || '',
      accidentType: data.accidentType || '',
      severity: data.severity || '',
      fatalityCount: data.fatalityCount ?? 0,
      injuryCount: data.injuryCount ?? 0,
      casualtySummary: data.casualtySummary || '',
      economicLoss: data.economicLoss ?? null,
      description: data.description || '',
      status: data.status || '',
      relatedRiskIds: (data.relatedRisks || []).map(r => r.id),
      relatedHazardIds: (data.relatedHazards || []).map(h => h.id),
      attachments: data.attachments || []
    }
  } catch (e) {
    error.value = e.message || '加载事故失败'
  }
}

async function loadRisks() {
  await fetchReference('/api/v1/risks', riskKeyword.value, riskOptions)
}

async function loadHazards() {
  await fetchReference('/api/v1/hazards', hazardKeyword.value, hazardOptions)
}

async function fetchReference(url, keyword, target) {
  try {
    const params = new URLSearchParams({ size: '50' })
    if (keyword) params.set('q', keyword)
    const token = auth.getToken()
    const resp = await fetch(`${url}?${params.toString()}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (!resp.ok) throw new Error(`加载失败 ${resp.status}`)
    const data = await resp.json()
    target.value = Array.isArray(data) ? data : []
  } catch (e) {
    target.value = []
    message.value = e.message || '检索失败'
  }
}

function toggleRisk(id) {
  const list = form.value.relatedRiskIds
  const idx = list.indexOf(id)
  if (idx >= 0) {
    list.splice(idx, 1)
  } else {
    list.push(id)
  }
}

function toggleHazard(id) {
  const list = form.value.relatedHazardIds
  const idx = list.indexOf(id)
  if (idx >= 0) {
    list.splice(idx, 1)
  } else {
    list.push(id)
  }
}

async function handleFileChange(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) return
  uploadMessage.value = ''
  for (const file of files) {
    try {
      const presigned = await presignAccidentAttachment({
        fileName: file.name,
        contentType: file.type,
        size: file.size
      })
      if (presigned) {
        await fetch(presigned.uploadUrl, {
          method: 'PUT',
          headers: { 'Content-Type': presigned.contentType || file.type || 'application/octet-stream' },
          body: file
        })
        form.value.attachments.push({
          key: presigned.key,
          url: presigned.uploadUrl,
          downloadUrl: presigned.downloadUrl,
          contentType: presigned.contentType || file.type || 'application/octet-stream',
          size: file.size,
          originalName: file.name
        })
      } else {
        uploadMessage.value = '对象存储未配置，已使用占位记录'
        form.value.attachments.push({
          key: `local-${Date.now()}-${file.name}`,
          url: '',
          downloadUrl: '',
          contentType: file.type || 'application/octet-stream',
          size: file.size,
          originalName: file.name
        })
      }
    } catch (e) {
      error.value = e.message || '附件上传失败'
    }
  }
  event.target.value = ''
}

function removeAttachment(index) {
  form.value.attachments.splice(index, 1)
}

function localDateTime(date) {
  if (!date) return ''
  const tzOffset = date.getTimezoneOffset() * 60000
  const local = new Date(date.getTime() - tzOffset)
  return local.toISOString().slice(0, 16)
}

function toIso(local) {
  if (!local) return null
  return new Date(local).toISOString()
}

function formatSize(bytes) {
  if (!bytes) return '0B'
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)}KB`
  return `${(kb / 1024).toFixed(1)}MB`
}

async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    const payload = {
      title: form.value.title,
      occurredAt: toIso(form.value.occurredAtLocal),
      location: form.value.location,
      organizationId: form.value.organizationId || null,
      organizationName: form.value.organizationName || null,
      accidentType: form.value.accidentType || null,
      severity: form.value.severity || null,
      fatalityCount: form.value.fatalityCount,
      injuryCount: form.value.injuryCount,
      casualtySummary: form.value.casualtySummary,
      economicLoss: form.value.economicLoss,
      description: form.value.description,
      status: form.value.status,
      relatedRiskIds: [...form.value.relatedRiskIds],
      relatedHazardIds: [...form.value.relatedHazardIds],
      attachments: form.value.attachments
    }
    if (isEdit.value) {
      await updateAccident(route.params.id, payload)
      message.value = '更新成功'
    } else {
      await createAccident(payload)
      message.value = '创建成功'
      form.value = emptyForm()
    }
    setTimeout(goBack, 600)
  } catch (e) {
    error.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push({ name: 'accident-dashboard' })
}
</script>

<style scoped>
.accident-form { max-width: 960px; margin: 0 auto; text-align: left; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin-bottom: 12px; }
.grid label { display: flex; flex-direction: column; font-size: 14px; }
.grid input, .grid select, textarea { margin-top: 4px; padding: 6px; font-size: 14px; }
.numbers input { width: 100%; }
.reference { margin: 16px 0; }
.reference .dual { display: flex; gap: 20px; }
.reference h3 { margin: 0 0 8px; }
.lookup { display: flex; gap: 6px; margin-bottom: 8px; }
.options { list-style: none; padding: 0; margin: 0; max-height: 180px; overflow: auto; }
.options li { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
.options button { font-size: 12px; }
.options .code { font-family: monospace; color: #666; }
.selected ul { list-style: none; padding: 0 0 0 12px; }
.attachments header { display: flex; justify-content: space-between; align-items: center; }
.attachments ul { list-style: none; padding: 0; }
.attachments li { display: flex; justify-content: space-between; padding: 4px 0; }
.actions { margin-top: 16px; display: flex; gap: 12px; }
.error { color: #d33; }
.message { color: #0a7; }
.hint { color: #666; font-size: 12px; }
button { cursor: pointer; }
</style>

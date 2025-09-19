<template>
  <div class="benchmark-view">
    <h1>跨单位安全对标</h1>

    <section class="panel">
      <div class="group">
        <label>
          对标模板
          <select v-model="selectedTemplateId" @change="onTemplateChange">
            <option value="">自定义</option>
            <option v-for="tpl in templates" :key="tpl.templateId" :value="tpl.templateId">
              {{ tpl.name }}
            </option>
          </select>
        </label>
        <button @click="refreshTemplates" :disabled="loadingTemplates">
          {{ loadingTemplates ? '刷新中…' : '刷新模板' }}
        </button>
        <button @click="loadLatest" :disabled="!selectedTemplateId || loadingLatest">
          {{ loadingLatest ? '加载中…' : '查看最新快照' }}
        </button>
      </div>

      <details open class="template-details">
        <summary>模板配置</summary>
        <div class="grid">
          <label>
            模板名称
            <input v-model="templateForm.name" placeholder="请输入模板名称" />
          </label>
          <label>
            可见范围
            <select v-model="templateForm.visibility">
              <option value="GROUP">集团可见</option>
              <option value="COMPANY">公司可见</option>
              <option value="PRIVATE">仅自己</option>
            </select>
          </label>
          <label>
            默认单位层级
            <select v-model="templateForm.organizationLevel">
              <option value="GROUP">集团</option>
              <option value="COMPANY">公司</option>
              <option value="MINE">矿级</option>
            </select>
          </label>
        </div>
        <label>
          模板说明
          <textarea v-model="templateForm.description" rows="2" placeholder="用于记录指标口径、备注"></textarea>
        </label>
        <div class="template-actions">
          <button @click="applyCurrentAsDefault" :disabled="!selectedOrganizations.length">将当前单位设为默认</button>
          <button @click="restoreDefaultOrgs" :disabled="!templateForm.defaultOrganizationIds.length">恢复模板默认单位</button>
          <button class="primary" @click="saveTemplate" :disabled="savingTemplate || !canSaveTemplate">
            {{ savingTemplate ? '保存中…' : (templateForm.templateId ? '更新模板' : '保存为新模板') }}
          </button>
        </div>
      </details>
    </section>

    <section class="panel">
      <div class="group">
        <label>
          对标开始时间
          <input type="datetime-local" v-model="filters.from" />
        </label>
        <label>
          对标截止时间
          <input type="datetime-local" v-model="filters.to" />
        </label>
        <label>
          使用缓存
          <input type="checkbox" v-model="filters.useCache" />
        </label>
      </div>

      <div class="split">
        <div>
          <h3>选择对标单位</h3>
          <div class="org-select">
            <select multiple size="8" v-model="selectedOrganizations">
              <option v-for="org in organizations" :key="org.organizationId" :value="org.organizationId">
                {{ org.name }}（{{ org.type }}）
              </option>
            </select>
          </div>
          <small>按住 Ctrl/Cmd 可多选；模板默认值会自动填充。</small>
        </div>
        <div>
          <h3>对标指标</h3>
          <div class="metric-add">
            <select v-model="newMetricCode">
              <option v-for="option in metricOptions" :key="option.code" :value="option.code">
                {{ option.label }}
              </option>
            </select>
            <button @click="addMetric" :disabled="!newMetricCode">添加指标</button>
          </div>
          <table class="metric-table" v-if="selectedMetrics.length">
            <thead>
              <tr>
                <th>指标</th>
                <th>权重</th>
                <th>高值更好</th>
                <th>聚合方式</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(metric, index) in selectedMetrics" :key="metric.code">
                <td>{{ metric.displayName }}</td>
                <td>
                  <input type="number" min="0" step="0.1" v-model.number="metric.weight" />
                </td>
                <td class="center">
                  <input type="checkbox" v-model="metric.higherBetter" />
                </td>
                <td>
                  <select v-model="metric.aggregation">
                    <option value="SUM">SUM</option>
                    <option value="AVG">AVG</option>
                  </select>
                </td>
                <td>
                  <button @click="removeMetric(metric.code)">删除</button>
                  <span class="order">{{ index + 1 }}</span>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-else class="empty">请选择至少一个指标</p>
        </div>
      </div>

      <div class="actions">
        <button class="primary" @click="runCompare" :disabled="loadingCompare || !canCompare">
          {{ loadingCompare ? '对标计算中…' : '执行对标' }}
        </button>
      </div>
    </section>

    <section v-if="message" class="message">{{ message }}</section>
    <section v-if="error" class="error">{{ error }}</section>

    <section v-if="result.rows && result.rows.length" class="panel results">
      <h2>对标结果</h2>
      <table class="result-table">
        <thead>
          <tr>
            <th>排名</th>
            <th>单位</th>
            <th>综合得分</th>
            <th v-for="metric in metricHeaders" :key="metric.code">{{ metric.displayName }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in result.rows" :key="row.organizationId">
            <td>{{ row.rankOrder }}</td>
            <td>{{ row.organizationName }}</td>
            <td>{{ formatNumber(row.score) }}</td>
            <td v-for="metric in row.metrics" :key="metric.code">{{ formatNumber(metric.value) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="result.series && result.series.length" class="series">
        <div v-for="series in result.series" :key="series.code" class="series-card">
          <h3>{{ series.name }}</h3>
          <table>
            <thead>
              <tr>
                <th>单位</th>
                <th>值</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(value, index) in series.data" :key="index">
                <td>{{ result.categories[index] }}</td>
                <td>{{ formatNumber(value) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <section v-else-if="!loadingCompare" class="empty">暂无对标结果，请选择指标并执行对标。</section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  fetchBenchmarkTemplates,
  fetchBenchmarkTemplate,
  createBenchmarkTemplate,
  updateBenchmarkTemplate,
  compareBenchmark,
  fetchBenchmarkLatest
} from '../../services/analysis.js'
import { fetchOrganizations } from '../../services/discipline.js'
import {
  useAnalysisStore,
  setBenchmarkTemplates,
  cacheBenchmarkResult,
  getBenchmarkResult,
  setBenchmarkLatest,
  getBenchmarkLatest
} from '../../stores/analysisStore.js'

const store = useAnalysisStore()

const loadingTemplates = ref(false)
const loadingCompare = ref(false)
const loadingLatest = ref(false)
const savingTemplate = ref(false)

const templates = computed(() => store.benchmarkTemplates)
const organizations = ref([])

const selectedTemplateId = ref('')
const selectedOrganizations = ref([])

const filters = reactive({
  from: '',
  to: '',
  useCache: true
})

const templateForm = reactive({
  templateId: '',
  name: '',
  description: '',
  visibility: 'GROUP',
  organizationLevel: 'MINE',
  defaultOrganizationIds: []
})

const metricOptions = [
  { code: 'HAZARD_OPEN_TOTAL', label: '在办隐患总数', higherBetter: false },
  { code: 'HAZARD_OVERDUE_TOTAL', label: '超期隐患数', higherBetter: false },
  { code: 'ACCIDENT_TOTAL', label: '事故数量', higherBetter: false },
  { code: 'ACCIDENT_FATALITIES', label: '事故死亡人数', higherBetter: false }
]

const selectedMetrics = ref([])
const newMetricCode = ref(metricOptions[0]?.code || '')

const result = reactive({ rows: [], categories: [], series: [] })
const message = ref('')
const error = ref('')

const canCompare = computed(() => selectedOrganizations.value.length > 0 && selectedMetrics.value.length > 0)
const canSaveTemplate = computed(() => String(templateForm.name || '').trim().length > 0 && selectedMetrics.value.length > 0)
const metricHeaders = computed(() => {
  if (!result.rows || !result.rows.length) return []
  return result.rows[0].metrics || []
})

const cacheKey = computed(() => {
  const base = {
    templateId: selectedTemplateId.value || null,
    from: filters.from || null,
    to: filters.to || null,
    orgs: [...selectedOrganizations.value].sort(),
    metrics: selectedMetrics.value.map(m => ({ code: m.code, weight: m.weight, higherBetter: m.higherBetter }))
  }
  return JSON.stringify(base)
})

onMounted(async () => {
  await refreshTemplates()
  await loadOrganizations()
})

async function refreshTemplates() {
  loadingTemplates.value = true
  try {
    const { data } = await fetchBenchmarkTemplates({ size: 50 })
    setBenchmarkTemplates(data)
  } catch (e) {
    error.value = e.message || '加载模板失败'
  } finally {
    loadingTemplates.value = false
  }
}

async function loadOrganizations() {
  try {
    const list = await fetchOrganizations('MINE')
    organizations.value = Array.isArray(list) ? list : []
  } catch (e) {
    // 组织列表不是硬性要求，失败时提示即可
    message.value = '未能加载单位列表，请手动输入或稍后重试'
  }
}

async function onTemplateChange() {
  clearResult()
  if (!selectedTemplateId.value) {
    resetTemplateForm()
    return
  }
  try {
    const detail = await fetchBenchmarkTemplate(selectedTemplateId.value)
    templateForm.templateId = detail.templateId
    templateForm.name = detail.name
    templateForm.description = detail.description || ''
    templateForm.visibility = detail.visibility || 'GROUP'
    templateForm.organizationLevel = detail.organizationLevel || 'MINE'
    templateForm.defaultOrganizationIds = Array.isArray(detail.defaultOrganizationIds) ? detail.defaultOrganizationIds.map(String) : []
    selectedOrganizations.value = templateForm.defaultOrganizationIds.length ? [...templateForm.defaultOrganizationIds] : []
    selectedMetrics.value = Array.isArray(detail.metrics)
      ? detail.metrics.map((m, idx) => ({
          code: m.code,
          displayName: m.displayName || m.code,
          higherBetter: m.higherBetter !== false,
          aggregation: m.aggregation || 'SUM',
          weight: m.weight != null ? Number(m.weight) : 1,
          sortOrder: m.sortOrder != null ? m.sortOrder : idx
        })).sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
      : []
  } catch (e) {
    error.value = e.message || '加载模板详情失败'
  }
}

function resetTemplateForm() {
  templateForm.templateId = ''
  templateForm.name = ''
  templateForm.description = ''
  templateForm.visibility = 'GROUP'
  templateForm.organizationLevel = 'MINE'
  templateForm.defaultOrganizationIds = []
  selectedMetrics.value = []
  selectedOrganizations.value = []
}

function addMetric() {
  const code = newMetricCode.value
  if (!code || selectedMetrics.value.some(item => item.code === code)) return
  const option = metricOptions.find(item => item.code === code)
  selectedMetrics.value.push({
    code,
    displayName: option ? option.label : code,
    higherBetter: option ? option.higherBetter : true,
    aggregation: 'SUM',
    weight: 1,
    sortOrder: selectedMetrics.value.length
  })
}

function removeMetric(code) {
  selectedMetrics.value = selectedMetrics.value.filter(item => item.code !== code)
}

function applyCurrentAsDefault() {
  templateForm.defaultOrganizationIds = [...selectedOrganizations.value]
  message.value = '已将当前对标单位保存为模板默认值（未提交）'
}

function restoreDefaultOrgs() {
  selectedOrganizations.value = [...templateForm.defaultOrganizationIds]
}

async function saveTemplate() {
  if (!canSaveTemplate.value) return
  savingTemplate.value = true
  error.value = ''
  message.value = ''
  const payload = buildTemplatePayload()
  try {
    let response
    if (templateForm.templateId) {
      response = await updateBenchmarkTemplate(templateForm.templateId, payload)
    } else {
      response = await createBenchmarkTemplate(payload)
      templateForm.templateId = response.templateId
      selectedTemplateId.value = response.templateId
    }
    await refreshTemplates()
    message.value = '模板保存成功'
    // 确保模板表单与服务器同步
    if (response) {
      templateForm.name = response.name
      templateForm.description = response.description || ''
      templateForm.visibility = response.visibility || 'GROUP'
      templateForm.organizationLevel = response.organizationLevel || 'MINE'
      templateForm.defaultOrganizationIds = Array.isArray(response.defaultOrganizationIds) ? response.defaultOrganizationIds.map(String) : []
    }
  } catch (e) {
    error.value = e.message || '保存模板失败'
  } finally {
    savingTemplate.value = false
  }
}

async function runCompare() {
  if (!canCompare.value) return
  const cached = getBenchmarkResult(cacheKey.value)
  error.value = ''
  message.value = ''
  if (cached) {
    applyResult(cached)
    message.value = '已加载缓存结果（客户端）'
    return
  }
  loadingCompare.value = true
  try {
    const payload = buildComparePayload()
    const data = await compareBenchmark(payload)
    applyResult(data)
    cacheBenchmarkResult(cacheKey.value, data)
    if (payload.templateId) {
      setBenchmarkLatest(payload.templateId, data)
    }
    message.value = '对标计算完成'
  } catch (e) {
    error.value = e.message || '对标计算失败'
  } finally {
    loadingCompare.value = false
  }
}

async function loadLatest() {
  if (!selectedTemplateId.value) return
  loadingLatest.value = true
  error.value = ''
  message.value = ''
  try {
    const cached = getBenchmarkLatest(selectedTemplateId.value)
    if (cached) {
      applyResult(cached)
      message.value = '已使用最新快照（缓存）'
      return
    }
    const data = await fetchBenchmarkLatest(selectedTemplateId.value)
    applyResult(data)
    setBenchmarkLatest(selectedTemplateId.value, data)
    message.value = '已加载模板最新快照'
  } catch (e) {
    error.value = e.message === 'Request failed: 404' ? '尚无快照，请执行一次对标' : (e.message || '加载快照失败')
  } finally {
    loadingLatest.value = false
  }
}

function buildTemplatePayload() {
  return {
    name: templateForm.name,
    description: templateForm.description,
    visibility: templateForm.visibility,
    organizationLevel: templateForm.organizationLevel,
    defaultOrganizationIds: [...templateForm.defaultOrganizationIds],
    metrics: selectedMetrics.value.map((metric, index) => ({
      code: metric.code,
      displayName: metric.displayName,
      aggregation: metric.aggregation || 'SUM',
      higherBetter: !!metric.higherBetter,
      weight: metric.weight != null ? Number(metric.weight) : 1,
      sortOrder: index
    }))
  }
}

function buildComparePayload() {
  return {
    templateId: templateForm.templateId || selectedTemplateId.value || null,
    from: filters.from ? new Date(filters.from).toISOString() : null,
    to: filters.to ? new Date(filters.to).toISOString() : null,
    organizationIds: [...selectedOrganizations.value],
    metrics: selectedMetrics.value.map((metric, index) => ({
      code: metric.code,
      displayName: metric.displayName,
      aggregation: metric.aggregation || 'SUM',
      higherBetter: !!metric.higherBetter,
      weight: metric.weight != null ? Number(metric.weight) : 1,
      sortOrder: index
    })),
    useCache: !!filters.useCache
  }
}

function applyResult(data) {
  result.rows = Array.isArray(data?.rows) ? data.rows : []
  result.categories = Array.isArray(data?.categories) ? data.categories : []
  result.series = Array.isArray(data?.series) ? data.series : []
}

function clearResult() {
  result.rows = []
  result.categories = []
  result.series = []
  message.value = ''
  error.value = ''
}

function formatNumber(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '-'
  return Number(value).toFixed(2)
}
</script>

<style scoped>
.benchmark-view { padding: 16px; text-align: left; display: flex; flex-direction: column; gap: 16px; }
.panel { border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; background: #fff; }
.group { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.group label { display: flex; flex-direction: column; font-size: 14px; }
.group input, .group select, textarea { margin-top: 4px; padding: 6px; font-size: 14px; }
textarea { resize: vertical; }
.template-actions { margin-top: 12px; display: flex; gap: 12px; flex-wrap: wrap; }
.primary { background: #2563eb; color: #fff; border: none; padding: 8px 14px; border-radius: 4px; cursor: pointer; }
button { cursor: pointer; padding: 8px 12px; border: 1px solid #d1d5db; background: #f9fafb; border-radius: 4px; }
button:disabled { opacity: 0.6; cursor: not-allowed; }
.split { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-top: 16px; }
.org-select select { width: 100%; min-height: 200px; }
.metric-add { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.metric-table { width: 100%; border-collapse: collapse; }
.metric-table th, .metric-table td { border: 1px solid #e5e7eb; padding: 6px; font-size: 13px; }
.metric-table th { background: #f8fafc; }
.metric-table input[type="number"] { width: 80px; }
.metric-table select { width: 100px; }
.metric-table .center { text-align: center; }
.metric-table .order { margin-left: 6px; color: #6b7280; font-size: 12px; }
.actions { margin-top: 16px; display: flex; gap: 12px; }
.message { color: #0f766e; }
.error { color: #dc2626; }
.empty { color: #6b7280; }
.results { overflow-x: auto; }
.result-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
.result-table th, .result-table td { border: 1px solid #e5e7eb; padding: 6px; font-size: 13px; }
.result-table th { background: #f1f5f9; }
.series { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-top: 16px; }
.series-card { border: 1px solid #e5e7eb; border-radius: 6px; padding: 12px; background: #f9fafb; }
.series-card table { width: 100%; border-collapse: collapse; }
.series-card th, .series-card td { border: 1px solid #e5e7eb; padding: 4px; font-size: 12px; }
.template-details { margin-top: 12px; }
.template-details summary { cursor: pointer; font-weight: 600; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin-top: 12px; }
</style>

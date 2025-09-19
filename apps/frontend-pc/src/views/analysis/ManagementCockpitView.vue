<template>
  <div class="cockpit">
    <header class="cockpit__header">
      <div>
        <h1>管理驾驶舱</h1>
        <p class="hint">按层级聚合关键安全指标，支持一键刷新与下钻。</p>
      </div>
      <div class="controls">
        <label>
          视角
          <select v-model="selectedScope" @change="onScopeChange" :disabled="loading || refreshing">
            <option v-for="option in scopeOptions" :key="option.code" :value="option.code">
              {{ option.label }}
            </option>
          </select>
        </label>
        <button @click="refresh" :disabled="refreshing">{{ refreshing ? '刷新中…' : '刷新数据' }}</button>
      </div>
    </header>

    <div class="status" v-if="dashboard">
      <span>最近生成：{{ formatDateTime(dashboard.generatedAt) }}</span>
      <span v-if="dashboard.cacheExpiresAt">缓存有效期：{{ formatDateTime(dashboard.cacheExpiresAt) }}</span>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-else-if="loading" class="loading">正在加载驾驶舱数据…</div>
    <div v-else-if="!dashboard" class="empty">暂无驾驶舱数据</div>

    <template v-else>
      <section class="summary" v-if="dashboard.summary?.metrics?.length">
        <article v-for="metric in dashboard.summary.metrics" :key="metric.code" class="metric-card">
          <header>{{ metric.label }}</header>
          <strong>{{ formatMetric(metric) }}</strong>
          <span v-if="metric.unit" class="unit">{{ metric.unit }}</span>
          <p v-if="metric.extras?.organizationName" class="meta">最佳单位：{{ metric.extras.organizationName }}</p>
          <p v-else-if="metric.extras?.majorTotal != null" class="meta">重大隐患：{{ metric.extras.majorTotal }}</p>
        </article>
      </section>

      <section v-for="module in dashboard.modules" :key="module.code" class="module">
        <header>
          <div>
            <h2>{{ module.title }}</h2>
            <p v-if="module.description" class="module-desc">{{ module.description }}</p>
          </div>
        </header>
        <div class="module-metrics" v-if="module.metrics?.length">
          <article v-for="metric in module.metrics" :key="metric.code" class="metric-mini">
            <span class="label">{{ metric.label }}</span>
            <span class="value">{{ formatMetric(metric) }}</span>
            <span v-if="metric.unit" class="unit">{{ metric.unit }}</span>
          </article>
        </div>
        <div class="module-series" v-for="series in module.series" :key="series.code">
          <div class="series-header">
            <h3>{{ series.name }}</h3>
            <button v-if="series.drillRouteName" @click="navigate(series)">查看详情</button>
          </div>
          <p v-if="series.description" class="series-desc">{{ series.description }}</p>
          <table>
            <thead>
              <tr>
                <th>对象</th>
                <th>数值</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(category, index) in series.categories" :key="`${series.code}-${category}-${index}`">
                <td>{{ category }}</td>
                <td>{{ formatSeriesValue(series.data[index]) }}</td>
              </tr>
              <tr v-if="!series.categories.length">
                <td colspan="2" class="empty">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </div>
</template>

<script>
import { computed, onMounted, reactive, toRefs } from 'vue'
import { useRouter } from 'vue-router'
import {
  useAnalysisStore,
  getDashboard,
  setDashboard,
  setDashboardScopes
} from '../../stores/analysisStore.js'
import { fetchDashboard, fetchDashboardByScope } from '../../services/analysis.js'

export default {
  name: 'ManagementCockpitView',
  setup() {
    const router = useRouter()
    const store = useAnalysisStore()
    const state = reactive({
      selectedScope: 'GROUP',
      loading: false,
      refreshing: false,
      error: ''
    })

    const dashboard = computed(() => getDashboard(state.selectedScope))
    const scopeOptions = computed(() => store.dashboardScopes?.length ? store.dashboardScopes : [
      { code: 'GROUP', label: '集团', description: '' },
      { code: 'COMPANY', label: '公司', description: '' },
      { code: 'MINE', label: '矿井', description: '' }
    ])

    const loadInitial = async () => {
      state.loading = true
      state.error = ''
      try {
        const response = await fetchDashboard()
        applyDashboard(response.scope || 'GROUP', response, { replaceScopes: true })
        state.selectedScope = (response.scope || 'GROUP').toUpperCase()
      } catch (err) {
        state.error = err.message || '驾驶舱数据加载失败'
      } finally {
        state.loading = false
      }
    }

    const applyDashboard = (scope, payload, options = {}) => {
      const normalized = (scope || 'GROUP').toUpperCase()
      setDashboard(normalized, payload)
      if (options.replaceScopes) {
        setDashboardScopes(payload.scopes || [])
      } else if (Array.isArray(payload.scopes) && payload.scopes.length) {
        setDashboardScopes(payload.scopes)
      }
    }

    const loadDashboard = async ({ scope, refresh } = {}) => {
      const targetScope = (scope || state.selectedScope || 'GROUP').toUpperCase()
      const cached = !refresh ? getDashboard(targetScope) : null
      if (cached && !refresh) {
        if (Array.isArray(cached.scopes)) {
          setDashboardScopes(cached.scopes)
        }
        return
      }
      if (refresh) {
        state.refreshing = true
      } else {
        state.loading = true
      }
      state.error = ''
      try {
        const params = refresh ? { refresh: true } : {}
        const response = await fetchDashboardByScope(targetScope, params)
        applyDashboard(targetScope, response, { replaceScopes: true })
      } catch (err) {
        state.error = err.message || '驾驶舱数据加载失败'
      } finally {
        state.refreshing = false
        state.loading = false
      }
    }

    const onScopeChange = async () => {
      await loadDashboard({ scope: state.selectedScope })
    }

    const refresh = async () => {
      await loadDashboard({ scope: state.selectedScope, refresh: true })
    }

    const navigate = (series) => {
      if (!series?.drillRouteName) return
      const params = series.drillParams && typeof series.drillParams === 'object'
        ? { ...series.drillParams }
        : {}
      router.push({ name: series.drillRouteName, query: params })
    }

    const formatMetric = (metric) => {
      if (!metric || metric.value == null || Number.isNaN(metric.value)) return '-'
      const precision = metric.precision ?? (metric.unit === '%' ? 1 : 0)
      return Number(metric.value).toLocaleString('zh-CN', {
        minimumFractionDigits: precision,
        maximumFractionDigits: precision
      })
    }

    const formatSeriesValue = (value) => {
      if (value == null || Number.isNaN(value)) return '-'
      return Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 1 })
    }

    const formatDateTime = (value) => {
      if (!value) return '未知'
      const date = typeof value === 'string' ? new Date(value) : value
      if (Number.isNaN(date.getTime())) return '未知'
      return date.toLocaleString('zh-CN', {
        hour12: false
      })
    }

    onMounted(loadInitial)

    return {
      store,
      dashboard,
      scopeOptions,
      ...toRefs(state),
      onScopeChange,
      refresh,
      navigate,
      formatMetric,
      formatSeriesValue,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.cockpit {
  max-width: 1080px;
  margin: 0 auto;
  padding: 16px;
  text-align: left;
}
.cockpit__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.controls { display: flex; gap: 12px; align-items: flex-end; }
.controls select { min-width: 160px; padding: 6px; }
.controls button { padding: 8px 14px; }
.hint { color: #6b7280; margin-top: 4px; }
.status { display: flex; gap: 24px; color: #475569; margin-bottom: 12px; font-size: 14px; }
.error { color: #dc2626; margin: 12px 0; }
.loading, .empty { color: #6b7280; margin: 16px 0; }
.summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
.metric-card { background: #f8fafc; padding: 16px; border-radius: 8px; position: relative; }
.metric-card header { font-size: 14px; color: #475569; margin-bottom: 8px; }
.metric-card strong { font-size: 28px; display: block; }
.metric-card .unit { position: absolute; top: 16px; right: 16px; color: #64748b; }
.metric-card .meta { margin-top: 8px; font-size: 12px; color: #64748b; }
.module { margin-bottom: 32px; }
.module h2 { margin: 0; }
.module-desc { color: #64748b; margin: 4px 0 12px; }
.module-metrics { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.metric-mini { background: #f1f5f9; border-radius: 6px; padding: 8px 12px; display: flex; flex-direction: column; }
.metric-mini .label { font-size: 12px; color: #475569; }
.metric-mini .value { font-weight: 600; font-size: 18px; }
.metric-mini .unit { font-size: 12px; color: #94a3b8; }
.module-series { margin-bottom: 20px; }
.series-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.series-header button { padding: 6px 12px; }
.series-desc { color: #6b7280; margin: 8px 0; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #e2e8f0; padding: 8px; text-align: left; }
th { background: #f8fafc; }
tbody tr:nth-child(even) { background: #f9fafb; }
.table .empty, td.empty { text-align: center; color: #94a3b8; }
@media (max-width: 768px) {
  .controls { flex-direction: column; align-items: stretch; }
  .controls button { width: 100%; }
}
</style>

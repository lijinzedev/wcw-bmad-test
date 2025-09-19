<template>
  <div class="assessment-dashboard">
    <h1>安全绩效考核看板</h1>

    <div class="toolbar">
      <label>
        考核周期
        <select v-model="selectedCycleId" @change="loadResults" :disabled="loadingCycles">
          <option value="" disabled>请选择周期</option>
          <option v-for="cycle in store.cycles" :key="cycle.cycleId" :value="cycle.cycleId">
            {{ cycle.name }} ({{ cycle.level }})
          </option>
        </select>
      </label>
      <button @click="recalculate" :disabled="!selectedCycleId || recalculating">
        {{ recalculating ? '计算中...' : '重新计算' }}
      </button>
      <button @click="loadResults" :disabled="!selectedCycleId || loadingResults">
        {{ loadingResults ? '刷新中...' : '刷新结果' }}
      </button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="message" class="message">{{ message }}</div>

    <table v-if="results.length" class="results">
      <thead>
        <tr>
          <th>排名</th>
          <th>单位</th>
          <th>得分</th>
          <th>指标明细</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="result in results" :key="result.resultId">
          <td>{{ result.rankOrder ?? '-' }}</td>
          <td>{{ result.organizationName || result.organizationId }}</td>
          <td>{{ Number(result.score || 0).toFixed(2) }}</td>
          <td>
            <details>
              <summary>查看</summary>
              <ul>
                <li v-for="detail in result.details" :key="detail.indicatorId">
                  <strong>{{ detail.indicatorName }}:</strong>
                  原始值 {{ Number(detail.rawValue || 0).toFixed(2) }} ，加权 {{ Number(detail.weightedScore || 0).toFixed(2) }} / {{ detail.maxScore != null ? Number(detail.maxScore).toFixed(2) : '-' }}
                </li>
              </ul>
            </details>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else-if="selectedCycleId && !loadingResults" class="empty">暂无考核结果</div>
  </div>
</template>

<script>
import { fetchCycles, fetchResults, recalculateCycle } from '../../services/analysis.js'
import { useAnalysisStore, setCycles, cacheResults, getCachedResults } from '../../stores/analysisStore.js'

export default {
  name: 'AssessmentDashboardView',
  data() {
    return {
      store: useAnalysisStore(),
      selectedCycleId: '',
      results: [],
      loadingCycles: false,
      loadingResults: false,
      recalculating: false,
      error: '',
      message: ''
    }
  },
  created() {
    this.loadCycles()
  },
  methods: {
    async loadCycles() {
      this.loadingCycles = true
      this.error = ''
      try {
        const { data } = await fetchCycles({ size: 50 })
        setCycles(data)
        if (!this.selectedCycleId && this.store.cycles.length) {
          this.selectedCycleId = this.store.cycles[0].cycleId
          await this.loadResults()
        }
      } catch (e) {
        this.error = e.message || '加载考核周期失败'
      } finally {
        this.loadingCycles = false
      }
    },
    async loadResults() {
      if (!this.selectedCycleId) return
      this.loadingResults = true
      this.error = ''
      this.message = ''
      try {
        const cached = getCachedResults(this.selectedCycleId)
        if (cached) {
          this.results = cached
          if (!cached.length) {
            this.message = '该周期暂无考核结果'
          }
          return
        }
        const data = await fetchResults(this.selectedCycleId)
        this.results = Array.isArray(data) ? data : []
        cacheResults(this.selectedCycleId, this.results)
        if (!this.results.length) {
          this.message = '该周期暂无考核结果'
        }
      } catch (e) {
        this.error = e.message || '加载考核结果失败'
      } finally {
        this.loadingResults = false
      }
    },
    async recalculate() {
      if (!this.selectedCycleId) return
      this.recalculating = true
      this.error = ''
      this.message = ''
      try {
        const res = await recalculateCycle(this.selectedCycleId)
        this.message = `已重新计算，共评估 ${res.organizationsEvaluated ?? 0} 个单位`
        await this.loadResults()
      } catch (e) {
        this.error = e.message || '重新计算失败'
      } finally {
        this.recalculating = false
      }
    }
  }
}
</script>

<style scoped>
.assessment-dashboard { max-width: 960px; margin: 0 auto; text-align: left; }
.toolbar { display: flex; gap: 16px; align-items: flex-end; margin-bottom: 16px; flex-wrap: wrap; }
select { min-width: 240px; padding: 6px; }
button { padding: 8px 14px; }
.error { color: #dc2626; margin-bottom: 12px; }
.message { color: #0f766e; margin-bottom: 12px; }
.results { width: 100%; border-collapse: collapse; }
.results th, .results td { border: 1px solid #e5e7eb; padding: 8px; }
.results th { background: #f8fafc; text-align: left; }
.empty { color: #6b7280; }
details { cursor: pointer; }
</style>

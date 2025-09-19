import { reactive } from 'vue'

const state = reactive({
  cycles: [],
  resultCache: {},
  accidents: [],
  accidentTrends: [],
  accidentTopRisks: [],
  benchmarkTemplates: [],
  benchmarkResultCache: {},
  benchmarkLatest: {},
  dashboards: {},
  dashboardScopes: []
})

export function useAnalysisStore() {
  return state
}

export function setCycles(cycles) {
  state.cycles = Array.isArray(cycles) ? cycles : []
}

export function cacheResults(cycleId, results) {
  if (!cycleId) return
  state.resultCache[cycleId] = Array.isArray(results) ? results : []
}

export function getCachedResults(cycleId) {
  if (!cycleId) return undefined
  return state.resultCache[cycleId]
}

export function clearCache() {
  state.resultCache = {}
  state.accidentTrends = []
  state.accidentTopRisks = []
  state.benchmarkResultCache = {}
  state.benchmarkLatest = {}
  state.dashboards = {}
  state.dashboardScopes = []
}

export function setAccidents(accidents) {
  state.accidents = Array.isArray(accidents) ? accidents : []
}

export function setAccidentTrends(trends) {
  state.accidentTrends = Array.isArray(trends) ? trends : []
}

export function setAccidentTopRisks(list) {
  state.accidentTopRisks = Array.isArray(list) ? list : []
}

export function setBenchmarkTemplates(list) {
  state.benchmarkTemplates = Array.isArray(list) ? list : []
}

export function cacheBenchmarkResult(key, result) {
  if (!key) return
  state.benchmarkResultCache[key] = result
}

export function getBenchmarkResult(key) {
  return key ? state.benchmarkResultCache[key] : undefined
}

export function setBenchmarkLatest(templateId, payload) {
  if (!templateId) return
  state.benchmarkLatest[templateId] = payload
}

export function getBenchmarkLatest(templateId) {
  return templateId ? state.benchmarkLatest[templateId] : undefined
}

export function setDashboard(scope, payload) {
  const key = (scope || 'GROUP').toUpperCase()
  state.dashboards[key] = payload
}

export function getDashboard(scope) {
  const key = (scope || 'GROUP').toUpperCase()
  return state.dashboards[key]
}

export function setDashboardScopes(scopes) {
  state.dashboardScopes = Array.isArray(scopes) ? scopes : []
}

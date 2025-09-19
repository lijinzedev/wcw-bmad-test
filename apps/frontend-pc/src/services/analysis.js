import { api, auth } from './auth.js'

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.set(key, value)
  })
  return search.toString()
}

export async function fetchCycles(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/analysis/assessments${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchCycle(id) {
  return api.get(`/api/v1/analysis/assessments/${id}`)
}

export function createCycle(payload) {
  return api.post('/api/v1/analysis/assessments', payload)
}

export function updateCycle(id, payload) {
  return api.put(`/api/v1/analysis/assessments/${id}`, payload)
}

export function recalculateCycle(id) {
  return api.post(`/api/v1/analysis/assessments/${id}/recalculate`, {})
}

export function fetchResults(cycleId) {
  return api.get(`/api/v1/analysis/assessments/${cycleId}/results`)
}

export function fetchResultDetail(cycleId, organizationId) {
  return api.get(`/api/v1/analysis/assessments/${cycleId}/results/${organizationId}`)
}

export async function fetchAccidents(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/analysis/accidents${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchAccident(id) {
  return api.get(`/api/v1/analysis/accidents/${id}`)
}

export function createAccident(payload) {
  return api.post('/api/v1/analysis/accidents', payload)
}

export function updateAccident(id, payload) {
  return api.put(`/api/v1/analysis/accidents/${id}`, payload)
}

export async function presignAccidentAttachment(payload) {
  const token = auth.getToken()
  const resp = await fetch('/api/v1/analysis/accidents/uploads/presign', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload)
  })
  if (resp.status === 501) return null
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export function fetchAccidentTrends(params = {}) {
  const query = buildQuery(params)
  return api.get(`/api/v1/analysis/accidents/trends${query ? `?${query}` : ''}`)
}

export function fetchAccidentTopRisks(params = {}) {
  const query = buildQuery(params)
  return api.get(`/api/v1/analysis/accidents/top-related-risks${query ? `?${query}` : ''}`)
}

export async function fetchBenchmarkTemplates(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/analysis/benchmarks/templates${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchBenchmarkTemplate(id) {
  return api.get(`/api/v1/analysis/benchmarks/templates/${id}`)
}

export function createBenchmarkTemplate(payload) {
  return api.post('/api/v1/analysis/benchmarks/templates', payload)
}

export function updateBenchmarkTemplate(id, payload) {
  return api.put(`/api/v1/analysis/benchmarks/templates/${id}`, payload)
}

export function compareBenchmark(payload) {
  return api.post('/api/v1/analysis/benchmarks/compare', payload)
}

export function fetchBenchmarkLatest(templateId) {
  return api.get(`/api/v1/analysis/benchmarks/templates/${templateId}/results`)
}

export function fetchDashboard(params = {}) {
  const query = buildQuery(params)
  return api.get(`/api/v1/analysis/dashboard${query ? `?${query}` : ''}`)
}

export function fetchDashboardByScope(scope, params = {}) {
  const query = buildQuery(params)
  const normalized = (scope || 'GROUP').toUpperCase()
  return api.get(`/api/v1/analysis/dashboard/${normalized}${query ? `?${query}` : ''}`)
}

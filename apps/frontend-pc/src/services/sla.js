import { api } from './auth.js'

function queryString(params = {}) {
  const s = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') s.set(k, v) })
  return s.toString()
}

async function getWithPagination(path, params = {}) {
  const q = queryString(params)
  const resp = await fetch(`${path}${q ? `?${q}` : ''}`)
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchSlaRules(params = {}) {
  return getWithPagination('/api/v1/monitoring/sla-rules', params)
}

export function createSlaRule(payload) {
  return api.post('/api/v1/monitoring/sla-rules', payload)
}

export function updateSlaRule(id, payload) {
  return api.put(`/api/v1/monitoring/sla-rules/${id}`, payload)
}

export function deleteSlaRule(id) {
  return api.delete(`/api/v1/monitoring/sla-rules/${id}`)
}

export function fetchSlaSummary(params = {}) {
  return api.get(`/api/v1/analysis/alerts/sla/summary${queryString(params) ? `?${queryString(params)}` : ''}`)
}

export function fetchSlaBreaches(params = {}) {
  return getWithPagination('/api/v1/analysis/alerts/sla/breaches', params)
}


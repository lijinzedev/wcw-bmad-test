import { api, auth } from './auth.js'

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.set(key, value)
  })
  return search.toString()
}

export async function fetchRules(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/discipline/rules${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function createRule(payload) {
  return api.post('/api/v1/discipline/rules', payload)
}

export function updateRule(id, payload) {
  return api.put(`/api/v1/discipline/rules/${id}`, payload)
}

export async function fetchFlags(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/discipline/flags${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function createFlag(payload) {
  return api.post('/api/v1/discipline/flags', payload)
}

export function resolveFlag(id, payload = {}) {
  return api.patch(`/api/v1/discipline/flags/${id}/resolve`, payload)
}

export function evaluateRules() {
  return api.post('/api/v1/discipline/flags/evaluate', {})
}

export function fetchOrganizations(level) {
  const query = level ? `?level=${encodeURIComponent(level)}` : ''
  return api.get(`/api/v1/discipline/organizations${query}`)
}

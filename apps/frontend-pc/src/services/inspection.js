import { api, auth } from './auth.js'

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  return search.toString()
}

export async function fetchPlans(params = {}) {
  const query = buildQuery(params)
  const url = query ? `/api/v1/inspections?${query}` : '/api/v1/inspections'
  const headers = {}
  const token = auth.getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(url, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchPlan(id) {
  return api.get(`/api/v1/inspections/${id}`)
}

export function createPlan(payload) {
  return api.post('/api/v1/inspections', payload)
}

export function updatePlan(id, payload) {
  return api.put(`/api/v1/inspections/${id}`, payload)
}

export async function addRecord(id, payload, files = []) {
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const hasFiles = Array.isArray(files) && files.length > 0
  if (!hasFiles) {
    return api.post(`/api/v1/inspections/${id}/records`, payload)
  }
  const form = new FormData()
  form.append('record', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach(file => form.append('files', file))
  const resp = await fetch(`/api/v1/inspections/${id}/records`, {
    method: 'POST',
    headers,
    body: form
  })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export async function exportPlans(params = {}) {
  const query = buildQuery(params)
  const headers = {}
  const token = auth.getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/inspections/export?${query}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.blob()
}

export async function fetchReport(id) {
  const headers = {}
  const token = auth.getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/inspections/${id}/report`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.text()
}

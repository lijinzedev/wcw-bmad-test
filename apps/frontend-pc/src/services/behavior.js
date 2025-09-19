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

export async function fetchBehaviors(params = {}) {
  const query = buildQuery(params)
  const url = query ? `/api/v1/behaviors?${query}` : '/api/v1/behaviors'
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(url, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchBehaviorDetail(id) {
  return api.get(`/api/v1/behaviors/${id}`)
}

export async function createBehavior(payload, files = []) {
  const hasFiles = Array.isArray(files) && files.length > 0
  if (!hasFiles) {
    return api.post('/api/v1/behaviors', payload)
  }
  const form = new FormData()
  form.append('behavior', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach(file => form.append('files', file))
  const headers = {}
  const token = auth.getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch('/api/v1/behaviors', {
    method: 'POST',
    headers,
    body: form
  })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export async function updateBehavior(id, payload, files = []) {
  const hasFiles = Array.isArray(files) && files.length > 0
  if (!hasFiles) {
    return api.put(`/api/v1/behaviors/${id}`, payload)
  }
  const form = new FormData()
  form.append('behavior', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach(file => form.append('files', file))
  const headers = {}
  const token = auth.getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/behaviors/${id}`, {
    method: 'PUT',
    headers,
    body: form
  })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export async function recordBehaviorAction(id, payload, files = []) {
  const hasFiles = Array.isArray(files) && files.length > 0
  if (!hasFiles) {
    return api.post(`/api/v1/behaviors/${id}/actions`, payload)
  }
  const form = new FormData()
  form.append('action', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach(file => form.append('files', file))
  const headers = {}
  const token = auth.getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/behaviors/${id}/actions`, {
    method: 'POST',
    headers,
    body: form
  })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export function fetchBehaviorStats() {
  return api.get('/api/v1/behaviors/stats')
}

export async function exportBehaviors(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/behaviors/export?${query}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.blob()
}

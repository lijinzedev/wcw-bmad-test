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

export async function fetchHazards(params = {}) {
  const query = buildQuery(params)
  const url = query ? `/api/v1/hazards?${query}` : '/api/v1/hazards'
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

export function fetchHazardDetail(id) {
  return api.get(`/api/v1/hazards/${id}`)
}

export function assignHazard(id, payload) {
  return api.patch(`/api/v1/hazards/${id}/assign`, payload)
}

export async function submitHazardUpdate(id, payload, files = []) {
  const token = auth.getToken()
  const hasFiles = Array.isArray(files) && files.length > 0
  if (!hasFiles) {
    return api.post(`/api/v1/hazards/${id}/updates`, payload)
  }
  const form = new FormData()
  form.append('update', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach(file => form.append('files', file))
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/hazards/${id}/updates`, {
    method: 'POST',
    headers,
    body: form
  })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export function reviewHazard(id, payload) {
  return api.patch(`/api/v1/hazards/${id}/review`, payload)
}

export function fetchAssignableUsers() {
  return api.get('/api/v1/admin/users')
}

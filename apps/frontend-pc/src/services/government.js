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

export async function govLogin(username, password) {
  const resp = await fetch('/api/v1/gov/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  if (!resp.ok) {
    const err = await resp.json().catch(() => ({ message: 'Login failed' }))
    throw new Error(err.message || 'Login failed')
  }
  const data = await resp.json()
  const roles = Array.isArray(data.roles) ? data.roles : []
  auth.setSession(data.token, roles)
  return data
}

export async function fetchGovRisks(params = {}) {
  const query = buildQuery(params)
  const url = query ? `/api/v1/gov/risks?${query}` : '/api/v1/gov/risks'
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

export async function fetchGovHazards(params = {}) {
  const query = buildQuery(params)
  const url = query ? `/api/v1/gov/hazards?${query}` : '/api/v1/gov/hazards'
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

export async function createGovHazard(payload, files = []) {
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const hasFiles = Array.isArray(files) && files.length > 0
  if (!hasFiles) {
    return api.post('/api/v1/gov/hazards', payload)
  }
  const form = new FormData()
  form.append('hazard', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach(file => form.append('files', file))
  const resp = await fetch('/api/v1/gov/hazards', { method: 'POST', headers, body: form })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}


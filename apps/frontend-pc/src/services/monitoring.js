import { api, auth } from './auth.js'

function queryString(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.set(key, value)
  })
  return search.toString()
}

async function getWithPagination(path, params = {}) {
  const query = queryString(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`${path}${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchThresholds(params = {}) {
  return getWithPagination('/api/v1/monitoring/thresholds', params)
}

export function createThreshold(payload) {
  return api.post('/api/v1/monitoring/thresholds', payload)
}

export function updateThreshold(id, payload) {
  return api.put(`/api/v1/monitoring/thresholds/${id}`, payload)
}

export function deleteThreshold(id) {
  return api.delete(`/api/v1/monitoring/thresholds/${id}`)
}

export function fetchAlerts(params = {}) {
  return getWithPagination('/api/v1/monitoring/alerts', params)
}

export function triggerPoll() {
  return api.post('/api/v1/monitoring/poll', {})
}

export function ackAlert(id, acknowledged, note = '') {
  return api.patch(`/api/v1/monitoring/alerts/${id}/ack`, { acknowledged, note })
}

export function assignAlert(id, payload) {
  return api.post(`/api/v1/monitoring/alerts/${id}/assign`, payload || {})
}

export function escalateAlert(id) {
  return api.post(`/api/v1/monitoring/alerts/${id}/escalate`, {})
}

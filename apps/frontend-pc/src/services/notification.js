import { api } from './auth.js'

function queryString(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === '') return
    search.set(k, v)
  })
  return search.toString()
}

async function getWithPagination(path, params = {}) {
  const query = queryString(params)
  const resp = await fetch(`${path}${query ? `?${query}` : ''}`, { headers: {} })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchRules(params = {}) {
  return getWithPagination('/api/v1/notifications/settings', params)
}

export function createRule(payload) {
  return api.post('/api/v1/notifications/settings', payload)
}

export function updateRule(id, payload) {
  return api.put(`/api/v1/notifications/settings/${id}`, payload)
}

export function deleteRule(id) {
  return api.delete(`/api/v1/notifications/settings/${id}`)
}

export function testSend(id, recipients = []) {
  return api.post(`/api/v1/notifications/settings/${id}/test`, { recipients })
}

export function fetchLogs(params = {}) {
  return getWithPagination('/api/v1/notifications/logs', params)
}


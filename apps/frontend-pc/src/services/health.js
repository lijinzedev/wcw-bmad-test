import { api, auth } from './auth.js'

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.set(key, value)
  })
  return search.toString()
}

async function requestWithPagination(path, params = {}) {
  const query = buildQuery(params)
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

export function fetchHazardFactors(params = {}) {
  return requestWithPagination('/api/v1/health/factors', params)
}

export function createHazardFactor(payload) {
  return api.post('/api/v1/health/factors', payload)
}

export function updateHazardFactor(id, payload) {
  return api.put(`/api/v1/health/factors/${id}`, payload)
}

export function deleteHazardFactor(id) {
  return api.delete(`/api/v1/health/factors/${id}`)
}

export function fetchExposures(params = {}) {
  return requestWithPagination('/api/v1/health/exposures', params)
}

export function createExposure(payload) {
  return api.post('/api/v1/health/exposures', payload)
}

export function updateExposure(id, payload) {
  return api.put(`/api/v1/health/exposures/${id}`, payload)
}

export function deleteExposure(id) {
  return api.delete(`/api/v1/health/exposures/${id}`)
}

export function fetchChecks(params = {}) {
  return requestWithPagination('/api/v1/health/checks', params)
}

export function createCheck(payload) {
  return api.post('/api/v1/health/checks', payload)
}

export function updateCheck(id, payload) {
  return api.put(`/api/v1/health/checks/${id}`, payload)
}

export function deleteCheck(id) {
  return api.delete(`/api/v1/health/checks/${id}`)
}

export function fetchCases(params = {}) {
  return requestWithPagination('/api/v1/health/cases', params)
}

export function createCase(payload) {
  return api.post('/api/v1/health/cases', payload)
}

export function updateCase(id, payload) {
  return api.put(`/api/v1/health/cases/${id}`, payload)
}

export function deleteCase(id) {
  return api.delete(`/api/v1/health/cases/${id}`)
}

export async function fetchFollowUps() {
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch('/api/v1/health/follow-ups', { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

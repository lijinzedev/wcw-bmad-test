import { auth } from './auth.js'

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.set(key, value)
  })
  return search.toString()
}

async function getJson(path, params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`${path}${query ? `?${query}` : ''}`, { headers })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.json()
}

export function fetchAlertTrend(params = {}) {
  return getJson('/api/v1/analysis/alerts/trend', params)
}

export function fetchAlertDistribution(params = {}) {
  return getJson('/api/v1/analysis/alerts/distribution', params)
}

export function fetchAlertTopMetrics(params = {}) {
  return getJson('/api/v1/analysis/alerts/top-metrics', params)
}

export async function exportAlertAnalyticsCsv(params = {}) {
  const query = buildQuery(params)
  const token = auth.getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const resp = await fetch(`/api/v1/analysis/alerts/export${query ? `?${query}` : ''}`, {
    headers
  })
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  return resp.blob()
}

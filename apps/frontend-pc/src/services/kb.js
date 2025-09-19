import { api } from './auth.js'

function qs(params = {}) { const s = new URLSearchParams(); Object.entries(params).forEach(([k,v]) => { if (v!==undefined&&v!==null&&v!=='') s.set(k,v) }); return s.toString() }

async function getWithPagination(path, params={}) {
  const q = qs(params)
  const resp = await fetch(`${path}${q?`?${q}`:''}`)
  if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
  const data = await resp.json()
  const total = Number(resp.headers.get('X-Total-Count') || data.length || 0)
  const totalPages = Number(resp.headers.get('X-Total-Pages') || 1)
  return { data, total, totalPages }
}

export function fetchArticles(params={}) { return getWithPagination('/api/v1/kb/articles', params) }
export function createArticle(payload) { return api.post('/api/v1/kb/articles', payload) }
export function updateArticle(id, payload) { return api.put(`/api/v1/kb/articles/${id}`, payload) }
export function deleteArticle(id) { return api.delete(`/api/v1/kb/articles/${id}`) }

export function fetchMappings(params={}) { return getWithPagination('/api/v1/kb/mappings', params) }
export function createMapping(payload) { return api.post('/api/v1/kb/mappings', payload) }
export function updateMapping(id, payload) { return api.put(`/api/v1/kb/mappings/${id}`, payload) }
export function deleteMapping(id) { return api.delete(`/api/v1/kb/mappings/${id}`) }


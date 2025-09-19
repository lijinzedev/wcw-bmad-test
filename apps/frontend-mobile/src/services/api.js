import { getToken } from '../utils/auth.js'

function resolveBase() {
  try {
    if (typeof import !== 'undefined' && typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_BASE) {
      return import.meta.env.VITE_API_BASE
    }
  } catch (e) {}
  if (typeof process !== 'undefined' && process.env && process.env.UNI_APP_API_BASE) {
    return process.env.UNI_APP_API_BASE
  }
  return 'http://localhost:8080'
}

const API_BASE = resolveBase()

function buildUrl(path) {
  if (!path.startsWith('/')) {
    path = '/' + path
  }
  return API_BASE.replace(/\/$/, '') + path
}

export function request({ url, method = 'GET', data = {}, headers = {}, responseType = 'json' }) {
  const token = getToken()
  const finalHeaders = { ...headers }
  if (token) {
    finalHeaders['Authorization'] = `Bearer ${token}`
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: buildUrl(url),
      method,
      data,
      header: finalHeaders,
      responseType,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const message = res.data?.message || `请求失败(${res.statusCode})`
          uni.showToast({ title: message, icon: 'none' })
          reject(new Error(message))
        }
      },
      fail(err) {
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function login(username, password) {
  return request({
    url: '/api/v1/auth/login',
    method: 'POST',
    data: { username, password },
    headers: { 'Content-Type': 'application/json' }
  })
}

export function fetchMyHazards(params = {}) {
  return request({
    url: '/api/v1/hazards/mine',
    method: 'GET',
    data: params
  })
}

export function fetchHazardDetail(id) {
  return request({
    url: `/api/v1/hazards/${id}`,
    method: 'GET'
  })
}

export function searchRisks(keyword) {
  return request({
    url: '/api/v1/risks',
    method: 'GET',
    data: { q: keyword, size: 5 }
  })
}

export function uploadHazard(payload, filePaths = []) {
  const token = getToken()
  const hasFiles = Array.isArray(filePaths) && filePaths.length > 0
  if (!hasFiles) {
    return request({
      url: '/api/v1/hazards',
      method: 'POST',
      data: payload,
      headers: { 'Content-Type': 'application/json' }
    })
  }
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: buildUrl('/api/v1/hazards'),
      files: filePaths.map(path => ({ name: 'files', uri: path })),
      header: token ? { Authorization: `Bearer ${token}` } : {},
      formData: { hazard: JSON.stringify(payload) },
      success(res) {
        try {
          const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(data)
          } else {
            const message = data?.message || `上传失败(${res.statusCode})`
            uni.showToast({ title: message, icon: 'none' })
            reject(new Error(message))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail(err) {
        uni.showToast({ title: '上传失败，请稍后再试', icon: 'none' })
        reject(err)
      }
    })
  })
}

const TOKEN_KEY = 'mobile_auth_token'
const ROLES_KEY = 'mobile_auth_roles'

export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY) || ''
  } catch (e) {
    return ''
  }
}

export function getRoles() {
  try {
    const raw = uni.getStorageSync(ROLES_KEY)
    if (!raw) return []
    return JSON.parse(raw)
  } catch (e) {
    return []
  }
}

export function setAuth(token, roles = []) {
  uni.setStorageSync(TOKEN_KEY, token || '')
  uni.setStorageSync(ROLES_KEY, JSON.stringify(roles || []))
}

export function clearAuth() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(ROLES_KEY)
}

export function ensureAuth(redirect = true) {
  const token = getToken()
  if (!token) {
    if (redirect) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
    return false
  }
  return true
}

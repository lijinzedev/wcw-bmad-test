const TOKEN_KEY = 'auth_token'
const ROLES_KEY = 'auth_roles'

function loadToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

function loadRoles() {
  try {
    const raw = localStorage.getItem(ROLES_KEY)
    return raw ? JSON.parse(raw) : []
  } catch (e) {
    return []
  }
}

export const auth = {
  _token: loadToken(),
  _roles: loadRoles(),

  isAuthenticated() {
    return !!this._token
  },

  getToken() {
    return this._token
  },

  getRoles() {
    return Array.isArray(this._roles) ? this._roles : []
  },

  setSession(token, roles = []) {
    this._token = token || ''
    const normalized = Array.isArray(roles) ? roles.map(r => (r.startsWith('ROLE_') ? r.substring(5) : r)) : []
    this._roles = normalized
    localStorage.setItem(TOKEN_KEY, this._token)
    localStorage.setItem(ROLES_KEY, JSON.stringify(this._roles))
  },

  async login(username, password) {
    const resp = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    })
    if (!resp.ok) {
      const err = await resp.json().catch(() => ({ message: 'Login failed' }))
      throw new Error(err.message || 'Login failed')
    }
    const data = await resp.json()
    this.setSession(data.token, Array.isArray(data.roles) ? data.roles : [])
  },

  logout() {
    this._token = ''
    this._roles = []
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLES_KEY)
  }
}

export const api = {
  async get(url) {
    const headers = {}
    const t = auth.getToken()
    if (t) headers['Authorization'] = `Bearer ${t}`
    const resp = await fetch(url, { headers })
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
    return resp.json()
  },
  async post(url, body) {
    const headers = { 'Content-Type': 'application/json' }
    const t = auth.getToken()
    if (t) headers['Authorization'] = `Bearer ${t}`
    const resp = await fetch(url, { method: 'POST', headers, body: JSON.stringify(body) })
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
    return resp.json()
  },
  async put(url, body) {
    const headers = { 'Content-Type': 'application/json' }
    const t = auth.getToken()
    if (t) headers['Authorization'] = `Bearer ${t}`
    const resp = await fetch(url, { method: 'PUT', headers, body: JSON.stringify(body) })
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
    return resp.json()
  },
  async patch(url, body) {
    const headers = { 'Content-Type': 'application/json' }
    const t = auth.getToken()
    if (t) headers['Authorization'] = `Bearer ${t}`
    const resp = await fetch(url, { method: 'PATCH', headers, body: JSON.stringify(body) })
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
    return resp.json()
  },
  async download(url) {
    const headers = {}
    const t = auth.getToken()
    if (t) headers['Authorization'] = `Bearer ${t}`
    const resp = await fetch(url, { headers })
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`)
    const blob = await resp.blob()
    return blob
  }
}

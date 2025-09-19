import { describe, it, expect, beforeEach, vi } from 'vitest'
import { auth } from '../services/auth'

describe('auth service', () => {
  beforeEach(() => {
    localStorage.clear()
    auth.logout()
    global.fetch = undefined
  })

  it('logs in and stores token and roles', async () => {
    global.fetch = vi.fn(async () => ({
      ok: true,
      json: async () => ({ token: 't123', username: 'admin', roles: ['ROLE_ADMIN'] })
    }))
    await auth.login('admin', 'admin123')
    expect(auth.isAuthenticated()).toBe(true)
    expect(auth.getToken()).toBe('t123')
    expect(auth.getRoles()).toContain('ADMIN')
    expect(localStorage.getItem('auth_token')).toBe('t123')
  })

  it('fails login with error message', async () => {
    global.fetch = vi.fn(async () => ({
      ok: false,
      json: async () => ({ message: 'Invalid username or password' })
    }))
    await expect(auth.login('x', 'y')).rejects.toThrow('Invalid username or password')
    expect(auth.isAuthenticated()).toBe(false)
  })
})


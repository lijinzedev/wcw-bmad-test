import { describe, it, expect, beforeEach, vi } from 'vitest'
import router from '../router'

describe('router guard', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('redirects unauthenticated user to login', async () => {
    vi.doMock('../services/auth', () => ({ auth: { isAuthenticated: () => false, getRoles: () => [] } }))
    // re-import router after mock to apply
    const r = (await import('../router')).default
    await r.push('/')
    await r.isReady()
    expect(r.currentRoute.value.name).toBe('login')
  })

  it('allows admin to access admin route', async () => {
    vi.doMock('../services/auth', () => ({ auth: { isAuthenticated: () => true, getRoles: () => ['ADMIN'] } }))
    const r = (await import('../router')).default
    await r.push('/admin')
    await r.isReady()
    expect(r.currentRoute.value.name).toBe('admin')
  })
})


import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('government router guard', () => {
  beforeEach(() => {
    vi.resetModules()
  })
  it('redirects non-government to login when accessing /gov', async () => {
    vi.doMock('../services/auth', () => ({ auth: { isAuthenticated: () => true, getRoles: () => ['USER'] } }))
    const r = (await import('../router')).default
    await r.push('/gov')
    await r.isReady()
    // Without GOVERNMENT role, guard should send to home
    expect(r.currentRoute.value.name).not.toBe('gov-dashboard')
  })

  it('allows GOVERNMENT role to access /gov', async () => {
    vi.resetModules()
    vi.doMock('../services/auth', () => ({ auth: { isAuthenticated: () => true, getRoles: () => ['GOVERNMENT'] } }))
    const r = (await import('../router')).default
    await r.push('/gov')
    await r.isReady()
    expect(r.currentRoute.value.name).toBe('gov-dashboard')
  })
})

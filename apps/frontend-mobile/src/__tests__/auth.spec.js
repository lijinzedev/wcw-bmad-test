import { describe, beforeEach, it, expect } from 'vitest'
import { getToken, setAuth, getRoles, clearAuth } from '../utils/auth.js'

const storage = new Map()

global.uni = {
  getStorageSync(key) {
    return storage.get(key)
  },
  setStorageSync(key, value) {
    storage.set(key, value)
  },
  removeStorageSync(key) {
    storage.delete(key)
  },
  reLaunch: () => {}
}

describe('auth utils', () => {
  beforeEach(() => {
    storage.clear()
  })

  it('stores and loads token with roles', () => {
    setAuth('token-1', ['ADMIN'])
    expect(getToken()).toBe('token-1')
    expect(getRoles()).toEqual(['ADMIN'])
  })

  it('clears auth data', () => {
    setAuth('token-2', ['USER'])
    clearAuth()
    expect(getToken()).toBe('')
    expect(getRoles()).toEqual([])
  })
})

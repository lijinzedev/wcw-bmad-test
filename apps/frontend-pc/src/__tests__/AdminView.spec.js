import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminView from '../views/AdminView.vue'

vi.mock('../services/auth', () => ({
  api: { get: vi.fn(async () => ({ ok: 'protected' })) },
  auth: { getRoles: () => [], isAuthenticated: () => true }
}))

describe('AdminView', () => {
  it('renders admin links', () => {
    const wrapper = mount(AdminView)
    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('角色管理')
  })
})

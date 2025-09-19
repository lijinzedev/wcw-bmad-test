import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LoginView from '../views/LoginView.vue'
import * as authMod from '../services/auth'

describe('LoginView', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('submits credentials and redirects on success', async () => {
    vi.spyOn(authMod, 'auth', 'get').mockReturnValue({
      login: vi.fn().mockResolvedValue(undefined)
    })
    const replace = vi.fn()
    const wrapper = mount(LoginView, {
      global: { mocks: { $router: { replace }, $route: { query: {} } } }
    })

    await wrapper.find('input[type="text"], input:not([type])').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(replace).toHaveBeenCalledWith('/')
  })

  it('shows link to government login page', async () => {
    const wrapper = mount(LoginView, {
      global: { mocks: { $router: { replace: vi.fn() }, $route: { query: {} } } }
    })
    const link = wrapper.find('a[href="#/gov-login"], a[href="/gov-login"], a')
    expect(wrapper.text()).toContain('政府监管入口')
  })
})

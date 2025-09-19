import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'

let GovernmentLoginView

const govLoginMock = vi.fn()

vi.mock('../services/government.js', () => ({
  govLogin: (...args) => govLoginMock(...args)
}))

beforeEach(async () => {
  vi.restoreAllMocks()
  localStorage.clear()
  govLoginMock.mockReset()
  govLoginMock.mockResolvedValue({ token: 't', roles: ['GOVERNMENT'] })
  GovernmentLoginView = (await import('../views/government/GovernmentLoginView.vue')).default
})

describe('GovernmentLoginView', () => {
  it('submits credentials and redirects to /gov', async () => {
    const replace = vi.fn()
    const wrapper = mount(GovernmentLoginView, {
      global: { mocks: { $router: { replace }, $route: { query: {} } } }
    })
    await wrapper.find('input[type="text"], input:not([type])').setValue('gov')
    await wrapper.find('input[type="password"]').setValue('gov123')
    await wrapper.find('form').trigger('submit.prevent')
    expect(govLoginMock).toHaveBeenCalledWith('gov', 'gov123')
    expect(replace).toHaveBeenCalledWith('/gov')
  })
})


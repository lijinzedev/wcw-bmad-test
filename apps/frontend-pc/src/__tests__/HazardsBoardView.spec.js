import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import HazardsBoardView from '../views/hazard/HazardsBoardView.vue'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

const fetchHazardsMock = vi.fn()

vi.mock('../services/hazard.js', () => ({
  fetchHazards: (...args) => fetchHazardsMock(...args)
}))

describe('HazardsBoardView', () => {
  beforeEach(() => {
    pushMock.mockReset()
    fetchHazardsMock.mockReset()
    fetchHazardsMock.mockResolvedValue({
      data: [
        {
          hazardId: 'haz-1',
          description: '皮带巷积水',
          status: '待指派',
          level: '重大',
          rectifierId: 'rect-1',
          rectificationDeadline: '2030-01-01',
          reportedAt: '2030-01-01T00:00:00Z',
          updatedAt: '2030-01-02T12:00:00Z'
        }
      ],
      total: 1,
      totalPages: 1
    })
  })

  it('loads hazards and renders table', async () => {
    const wrapper = mount(HazardsBoardView, {
      global: {
        config: {
          compilerOptions: { isCustomElement: () => false }
        }
      }
    })
    await flushPromises()
    expect(fetchHazardsMock).toHaveBeenCalled()
    expect(wrapper.text()).toContain('皮带巷积水')
  })

  it('navigates to detail when clicking button', async () => {
    const wrapper = mount(HazardsBoardView)
    await flushPromises()
    await wrapper.find('table button').trigger('click')
    expect(pushMock).toHaveBeenCalledWith({ name: 'hazard-detail', params: { id: 'haz-1' } })
  })
})

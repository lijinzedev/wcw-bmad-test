import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

let GovernmentDashboardView
const fetchGovRisksMock = vi.fn()
const fetchGovHazardsMock = vi.fn()
const createGovHazardMock = vi.fn()

vi.mock('../services/government.js', () => ({
  fetchGovRisks: (...args) => fetchGovRisksMock(...args),
  fetchGovHazards: (...args) => fetchGovHazardsMock(...args),
  createGovHazard: (...args) => createGovHazardMock(...args)
}))

beforeEach(async () => {
  vi.restoreAllMocks()
  localStorage.clear()
  // Silence window.alert in jsdom
  // eslint-disable-next-line no-undef
  global.alert = vi.fn()
  fetchGovRisksMock.mockReset()
  fetchGovHazardsMock.mockReset()
  createGovHazardMock.mockReset()
  fetchGovRisksMock.mockResolvedValue({ data: [{ riskId: 'r1', description: '顶板管理', location: '一采区', level: '一般' }], total: 1, totalPages: 1 })
  fetchGovHazardsMock.mockResolvedValue({ data: [{ hazardId: 'h1', description: '通风不畅', level: '一般', status: '待指派', reportedAt: '2030-01-01T00:00:00Z' }], total: 1, totalPages: 1 })
  createGovHazardMock.mockResolvedValue({ hazardId: 'h2' })
  GovernmentDashboardView = (await import('../views/government/GovernmentDashboardView.vue')).default
})

describe('GovernmentDashboardView', () => {
  it('loads risks and hazards', async () => {
    const wrapper = mount(GovernmentDashboardView)
    await flushPromises()
    expect(fetchGovRisksMock).toHaveBeenCalled()
    expect(fetchGovHazardsMock).toHaveBeenCalled()
    expect(wrapper.text()).toContain('顶板管理')
    expect(wrapper.text()).toContain('通风不畅')
  })

  it('submits new government hazard', async () => {
    const wrapper = mount(GovernmentDashboardView)
    await flushPromises()
    await wrapper.find('textarea').setValue('执法发现问题')
    await wrapper.find('form').trigger('submit.prevent')
    expect(createGovHazardMock).toHaveBeenCalled()
  })
})

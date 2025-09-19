import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AccidentDashboardView from '../views/analysis/AccidentDashboardView.vue'
import { setAccidents, setAccidentTrends, setAccidentTopRisks } from '../stores/analysisStore.js'

const routerPushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerPushMock })
}))

const fetchAccidentsMock = vi.fn()
const fetchAccidentTrendsMock = vi.fn()
const fetchAccidentTopRisksMock = vi.fn()

vi.mock('../services/analysis.js', () => ({
  fetchAccidents: (...args) => fetchAccidentsMock(...args),
  fetchAccidentTrends: (...args) => fetchAccidentTrendsMock(...args),
  fetchAccidentTopRisks: (...args) => fetchAccidentTopRisksMock(...args)
}))

describe('AccidentDashboardView', () => {
  beforeEach(() => {
    routerPushMock.mockReset()
    setAccidents([])
    setAccidentTrends([])
    setAccidentTopRisks([])
    fetchAccidentsMock.mockResolvedValue({ data: [{
      accidentId: 'a-1',
      occurredAt: new Date().toISOString(),
      title: '事故A',
      location: '井口',
      accidentType: 'FALL',
      fatalityCount: 1,
      injuryCount: 0,
      status: '调查中'
    }], total: 1, totalPages: 1 })
    fetchAccidentTrendsMock.mockResolvedValue([
      { bucket: new Date().toISOString(), total: 1, fatalities: 1, injuries: 0 }
    ])
    fetchAccidentTopRisksMock.mockResolvedValue([
      { riskId: 'risk-1', riskDescription: '风险1', category: '通风', occurrences: 2 }
    ])
  })

  it('loads accidents and analytics, and navigates to create', async () => {
    const wrapper = mount(AccidentDashboardView)
    await flushPromises()

    expect(fetchAccidentsMock).toHaveBeenCalled()
    expect(fetchAccidentTrendsMock).toHaveBeenCalled()
    expect(fetchAccidentTopRisksMock).toHaveBeenCalled()

    const rows = wrapper.findAll('tbody tr')
    expect(rows.length).toBeGreaterThan(0)

    await wrapper.find('.actions button').trigger('click')
    expect(routerPushMock).toHaveBeenCalledWith({ name: 'accident-create' })
  })
})

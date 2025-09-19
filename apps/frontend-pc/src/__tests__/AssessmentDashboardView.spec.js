import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AssessmentDashboardView from '../views/analysis/AssessmentDashboardView.vue'
import { setCycles, clearCache } from '../stores/analysisStore.js'

const fetchCyclesMock = vi.fn()
const fetchResultsMock = vi.fn()
const recalculateMock = vi.fn()

vi.mock('../services/analysis.js', () => ({
  fetchCycles: (...args) => fetchCyclesMock(...args),
  fetchResults: (...args) => fetchResultsMock(...args),
  recalculateCycle: (...args) => recalculateMock(...args)
}))

describe('AssessmentDashboardView', () => {
  beforeEach(() => {
    setCycles([])
    clearCache()
    fetchCyclesMock.mockResolvedValue({
      data: [
        { cycleId: 'cycle-1', name: '周期1', level: 'MINE' }
      ],
      total: 1,
      totalPages: 1
    })
    fetchResultsMock.mockResolvedValue([
      {
        resultId: 'result-1',
        organizationId: 'org-1',
        organizationName: '一矿',
        rankOrder: 1,
        score: 92,
        details: [
          { indicatorId: 'ind-1', indicatorName: '隐患', rawValue: 2, weightedScore: 40, maxScore: 40 }
        ]
      }
    ])
    recalculateMock.mockResolvedValue({ organizationsEvaluated: 1 })
  })

  it('loads cycles and results on mount', async () => {
    const wrapper = mount(AssessmentDashboardView)
    await flushPromises()
    expect(fetchCyclesMock).toHaveBeenCalled()
    expect(fetchResultsMock).toHaveBeenCalledWith('cycle-1')
    expect(wrapper.text()).toContain('一矿')
    expect(wrapper.text()).toContain('92.00')
  })

  it('recalculates when action triggered', async () => {
    const wrapper = mount(AssessmentDashboardView)
    await flushPromises()
    await wrapper.findAll('button')[0].trigger('click')
    await flushPromises()
    expect(recalculateMock).toHaveBeenCalledWith('cycle-1')
  })
})

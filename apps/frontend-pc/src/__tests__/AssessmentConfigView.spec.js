import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AssessmentConfigView from '../views/analysis/AssessmentConfigView.vue'
import { setCycles, clearCache } from '../stores/analysisStore.js'

const fetchCyclesMock = vi.fn()
const fetchCycleMock = vi.fn()
const createCycleMock = vi.fn()
const updateCycleMock = vi.fn()

vi.mock('../services/analysis.js', () => ({
  fetchCycles: (...args) => fetchCyclesMock(...args),
  fetchCycle: (...args) => fetchCycleMock(...args),
  createCycle: (...args) => createCycleMock(...args),
  updateCycle: (...args) => updateCycleMock(...args)
}))

describe('AssessmentConfigView', () => {
  beforeEach(() => {
    setCycles([])
    clearCache()
    fetchCyclesMock.mockResolvedValue({ data: [], total: 0, totalPages: 1 })
    createCycleMock.mockResolvedValue({ cycleId: 'cycle-1' })
    updateCycleMock.mockResolvedValue({})
    fetchCycleMock.mockResolvedValue({
      cycleId: 'cycle-1',
      name: '周期1',
      level: 'MINE',
      status: '草稿',
      startAt: new Date().toISOString(),
      endAt: new Date().toISOString(),
      notes: '说明',
      indicators: [
        {
          indicatorId: 'ind-1',
          code: 'HAZARD_OPEN_TOTAL',
          displayName: '隐患',
          weight: 60,
          thresholdValue: 5,
          higherBetter: false,
          description: ''
        }
      ]
    })
  })

  it('creates new cycle', async () => {
    const wrapper = mount(AssessmentConfigView)
    await flushPromises()
    wrapper.vm.addIndicator()
    wrapper.vm.form.name = '新周期'
    wrapper.vm.form.level = 'MINE'
    wrapper.vm.form.indicators[0].code = 'HAZARD_OPEN_TOTAL'
    wrapper.vm.form.indicators[0].displayName = '隐患'
    wrapper.vm.form.indicators[0].weight = 100
    await wrapper.vm.save()
    expect(createCycleMock).toHaveBeenCalled()
    const payload = createCycleMock.mock.calls[0][0]
    expect(payload.name).toBe('新周期')
    expect(payload.indicators).toHaveLength(1)
  })

  it('loads and updates existing cycle', async () => {
    fetchCyclesMock.mockResolvedValueOnce({
      data: [{ cycleId: 'cycle-1', name: '周期1', level: 'MINE' }],
      total: 1,
      totalPages: 1
    })
    const wrapper = mount(AssessmentConfigView)
    await flushPromises()
    await wrapper.vm.editCycle({ cycleId: 'cycle-1' })
    await flushPromises()
    wrapper.vm.form.indicators[0].weight = 100
    await wrapper.vm.save()
    expect(updateCycleMock).toHaveBeenCalledWith('cycle-1', expect.objectContaining({ name: '周期1' }))
  })
})

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import InspectionDetailView from '../views/inspection/InspectionDetailView.vue'

const fetchPlanMock = vi.fn()
const addRecordMock = vi.fn()
const fetchReportMock = vi.fn()

vi.mock('../services/inspection.js', () => ({
  fetchPlan: (...args) => fetchPlanMock(...args),
  addRecord: (...args) => addRecordMock(...args),
  fetchReport: (...args) => fetchReportMock(...args)
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: 'plan-1' } }),
  useRouter: () => ({ push: vi.fn(), back: vi.fn() })
}))

describe('InspectionDetailView', () => {
  beforeEach(() => {
    fetchPlanMock.mockResolvedValue({
      planId: 'plan-1',
      title: '季度检查',
      level: 'COMPANY',
      mineName: '一矿',
      status: '计划中',
      startAt: '2030-01-01T00:00:00Z',
      endAt: '2030-01-05T00:00:00Z',
      records: []
    })
    addRecordMock.mockResolvedValue({})
    fetchReportMock.mockResolvedValue('report')
  })

  it('loads plan and submits record', async () => {
    const wrapper = mount(InspectionDetailView)
    await flushPromises()
    expect(fetchPlanMock).toHaveBeenCalledWith('plan-1')
    await wrapper.find('input').setValue('掘进面')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(addRecordMock).toHaveBeenCalled()
  })
})

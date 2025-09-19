import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import InspectionBoardView from '../views/inspection/InspectionBoardView.vue'

const fetchPlansMock = vi.fn()
const exportPlansMock = vi.fn()
const pushMock = vi.fn()

vi.mock('../services/inspection.js', () => ({
  fetchPlans: (...args) => fetchPlansMock(...args),
  exportPlans: (...args) => exportPlansMock(...args)
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

describe('InspectionBoardView', () => {
  beforeEach(() => {
    fetchPlansMock.mockResolvedValue({
      data: [
        { planId: 'plan-1', title: '季度检查', level: 'COMPANY', mineName: '一矿', status: '计划中', startAt: '2030-01-01T00:00:00Z', endAt: '2030-01-02T00:00:00Z' }
      ],
      total: 1,
      totalPages: 1
    })
    exportPlansMock.mockResolvedValue(new Blob(['csv']))
    pushMock.mockReset()
  })

  it('loads plans and renders row', async () => {
    const wrapper = mount(InspectionBoardView)
    await flushPromises()
    expect(fetchPlansMock).toHaveBeenCalled()
    expect(wrapper.text()).toContain('季度检查')
  })

  it('navigates to create form', async () => {
    const wrapper = mount(InspectionBoardView)
    await flushPromises()
    const createBtn = wrapper.findAll('button').find(btn => btn.text() === '新建计划')
    await createBtn.trigger('click')
    expect(pushMock).toHaveBeenCalledWith({ name: 'inspection-create' })
  })
})

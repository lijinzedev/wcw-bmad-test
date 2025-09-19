import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import InspectionFormView from '../views/inspection/InspectionFormView.vue'

const fetchPlanMock = vi.fn()
const createPlanMock = vi.fn()
const updatePlanMock = vi.fn()
const pushMock = vi.fn()
const backMock = vi.fn()

vi.mock('../services/inspection.js', () => ({
  fetchPlan: (...args) => fetchPlanMock(...args),
  createPlan: (...args) => createPlanMock(...args),
  updatePlan: (...args) => updatePlanMock(...args)
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: {} }),
  useRouter: () => ({ push: pushMock, back: backMock })
}))

describe('InspectionFormView', () => {
  beforeEach(() => {
    createPlanMock.mockReset().mockResolvedValue({})
    updatePlanMock.mockReset().mockResolvedValue({})
    fetchPlanMock.mockReset()
    pushMock.mockReset()
  })

  it('submits create request', async () => {
    const wrapper = mount(InspectionFormView)
    await flushPromises()
    await wrapper.find('input').setValue('季度检查')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(createPlanMock).toHaveBeenCalled()
    expect(pushMock).toHaveBeenCalledWith({ name: 'inspection-board' })
  })
})

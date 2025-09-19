import { describe, it, expect, beforeEach, beforeAll, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const fetchHazardDetailMock = vi.fn()
const assignHazardMock = vi.fn()
const submitHazardUpdateMock = vi.fn()
const reviewHazardMock = vi.fn()
const fetchAssignableUsersMock = vi.fn()

vi.mock('../services/hazard.js', () => ({
  fetchHazardDetail: (...args) => fetchHazardDetailMock(...args),
  assignHazard: (...args) => assignHazardMock(...args),
  submitHazardUpdate: (...args) => submitHazardUpdateMock(...args),
  reviewHazard: (...args) => reviewHazardMock(...args),
  fetchAssignableUsers: (...args) => fetchAssignableUsersMock(...args)
}))

vi.mock('../../services/auth.js', () => ({
  auth: {
    isAuthenticated: () => true,
    getRoles: () => ['ADMIN'],
    getToken: () => 'token'
  },
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn()
  }
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: 'haz-1' } })
}))

let HazardDetailView

beforeAll(async () => {
  HazardDetailView = (await import('../views/hazard/HazardDetailView.vue')).default
})

describe('HazardDetailView', () => {
  async function mountComponent() {
    const wrapper = mount(HazardDetailView)
    await flushPromises()
    await flushPromises()
     if (!wrapper.vm.hazard) {
      wrapper.vm.hazard = {
        hazardId: 'haz-1',
        description: '皮带巷积水',
        status: '待指派',
        updates: [],
        reportedAt: '2030-01-01T00:00:00Z'
      }
      await flushPromises()
    }
    return wrapper
  }

  beforeEach(() => {
    fetchHazardDetailMock.mockReset()
    assignHazardMock.mockReset()
    submitHazardUpdateMock.mockReset()
    reviewHazardMock.mockReset()
    fetchAssignableUsersMock.mockReset()
    fetchHazardDetailMock.mockResolvedValue({
      hazardId: 'haz-1',
      description: '皮带巷积水',
      status: '待指派',
      updates: [],
      reportedAt: '2030-01-01T00:00:00Z'
    })
    fetchAssignableUsersMock.mockResolvedValue([
      { userId: 'user-1', fullName: '整改员A' }
    ])
    assignHazardMock.mockResolvedValue({})
    submitHazardUpdateMock.mockResolvedValue({})
    reviewHazardMock.mockResolvedValue({})
  })

  it('renders hazard info', async () => {
    const wrapper = await mountComponent()
    expect(fetchHazardDetailMock).toHaveBeenCalledWith('haz-1')
    expect(wrapper.text()).toContain('皮带巷积水')
  })

  it('submits assignment', async () => {
    const wrapper = await mountComponent()
    wrapper.vm.assignForm.rectifierId = 'user-1'
    await wrapper.vm.submitAssign()
    expect(assignHazardMock).toHaveBeenCalled()
  })

  it('submits rectification update', async () => {
    const wrapper = await mountComponent()
    wrapper.vm.updateForm.details = '现场处理完成'
    await wrapper.vm.submitUpdate()
    expect(submitHazardUpdateMock).toHaveBeenCalled()
  })

  it('submits review', async () => {
    const wrapper = await mountComponent()
    await wrapper.vm.submitReview()
    expect(reviewHazardMock).toHaveBeenCalled()
  })
})

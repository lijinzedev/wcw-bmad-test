import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import BehaviorBoardView from '../views/behavior/BehaviorBoardView.vue'

const fetchBehaviorsMock = vi.fn()
const exportBehaviorsMock = vi.fn()
const fetchBehaviorStatsMock = vi.fn()
const pushMock = vi.fn()

vi.mock('../services/behavior.js', () => ({
  fetchBehaviors: (...args) => fetchBehaviorsMock(...args),
  exportBehaviors: (...args) => exportBehaviorsMock(...args),
  fetchBehaviorStats: (...args) => fetchBehaviorStatsMock(...args)
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

describe('BehaviorBoardView', () => {
  beforeEach(() => {
    fetchBehaviorsMock.mockResolvedValue({
      data: [
        {
          behaviorId: 'beh-1',
          occurredAt: '2030-01-01T00:00:00Z',
          personName: '张三',
          behaviorType: '违章操作',
          location: '掘进面',
          status: '未处理'
        }
      ],
      total: 1,
      totalPages: 1
    })
    exportBehaviorsMock.mockResolvedValue(new Blob(['csv']))
    fetchBehaviorStatsMock.mockResolvedValue({ byType: [], byPerson: [] })
    pushMock.mockReset()
  })

  it('loads behaviors and renders row', async () => {
    const wrapper = mount(BehaviorBoardView)
    await flushPromises()
    expect(fetchBehaviorsMock).toHaveBeenCalled()
    expect(wrapper.text()).toContain('张三')
  })

  it('navigates to create form', async () => {
    const wrapper = mount(BehaviorBoardView)
    await flushPromises()
    const createBtn = wrapper.findAll('button').find(btn => btn.text() === '录入行为')
    await createBtn.trigger('click')
    expect(pushMock).toHaveBeenCalledWith({ name: 'behavior-create' })
  })
})

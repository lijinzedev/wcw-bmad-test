import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import BehaviorFormView from '../views/behavior/BehaviorFormView.vue'

const createBehaviorMock = vi.fn()
const updateBehaviorMock = vi.fn()
const fetchBehaviorDetailMock = vi.fn()
const recordBehaviorActionMock = vi.fn()
const pushMock = vi.fn()

vi.mock('../services/behavior.js', () => ({
  createBehavior: (...args) => createBehaviorMock(...args),
  updateBehavior: (...args) => updateBehaviorMock(...args),
  fetchBehaviorDetail: (...args) => fetchBehaviorDetailMock(...args),
  recordBehaviorAction: (...args) => recordBehaviorActionMock(...args)
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: {} }),
  useRouter: () => ({ push: pushMock, back: vi.fn() })
}))

describe('BehaviorFormView', () => {
  beforeEach(() => {
    createBehaviorMock.mockReset().mockResolvedValue({})
    updateBehaviorMock.mockReset().mockResolvedValue({})
    fetchBehaviorDetailMock.mockReset()
    recordBehaviorActionMock.mockReset()
    pushMock.mockReset()
  })

  it('submits new behavior', async () => {
    const wrapper = mount(BehaviorFormView)
    await flushPromises()
    await wrapper.find('input[type="datetime-local"]').setValue('2030-01-01T12:00')
    await wrapper.findAll('input')[1].setValue('掘进面')
    await wrapper.findAll('input')[2].setValue('张三')
    await wrapper.findAll('input')[3].setValue('违章操作')
    await wrapper.find('textarea').setValue('描述')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(createBehaviorMock).toHaveBeenCalled()
    expect(pushMock).toHaveBeenCalledWith({ name: 'behavior-board' })
  })
})

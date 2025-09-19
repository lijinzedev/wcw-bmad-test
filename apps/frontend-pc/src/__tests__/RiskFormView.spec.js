import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import RiskFormView from '../views/risk/RiskFormView.vue'

let currentRoute
const pushMock = vi.fn()
const backMock = vi.fn()
const postMock = vi.fn()
const putMock = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => currentRoute,
  useRouter: () => ({
    push: pushMock,
    back: backMock
  })
}))

vi.mock('../services/auth', () => ({
  api: {
    post: (...args) => postMock(...args),
    put: (...args) => putMock(...args)
  }
}))

const originalFetch = global.fetch

describe('RiskFormView', () => {
  beforeEach(() => {
    currentRoute = { params: {}, query: {} }
    pushMock.mockReset()
    backMock.mockReset()
    postMock.mockReset()
    putMock.mockReset()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    global.fetch = originalFetch
  })

  it('submits new risk and navigates to list', async () => {
    postMock.mockResolvedValue({})

    const wrapper = mount(RiskFormView)
    await flushPromises()

    const inputs = wrapper.findAll('input')
    await inputs.at(0).setValue('皮带巷积水')
    await inputs.at(1).setValue('通风')
    await inputs.at(2).setValue('西翼')
    const select = wrapper.find('select')
    await select.setValue('重大')
    await wrapper.find('textarea').setValue('安排专人抽排积水')

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(postMock).toHaveBeenCalledWith('/api/v1/risks', {
      description: '皮带巷积水',
      category: '通风',
      location: '西翼',
      level: '重大',
      controlMeasures: '安排专人抽排积水'
    })
    expect(pushMock).toHaveBeenCalledWith({ name: 'risks' })
  })

  it('loads existing risk and updates it via put', async () => {
    currentRoute = { params: { id: 'risk-1' }, query: {} }
    const existing = {
      description: '瓦斯超限',
      category: '通风',
      location: '采掘线',
      level: '重大',
      controlMeasures: '加强通风'
    }
    global.fetch = vi.fn(() => Promise.resolve({
      ok: true,
      json: async () => existing,
      headers: { get: () => null }
    }))

    putMock.mockResolvedValue({})

    const wrapper = mount(RiskFormView)
    await flushPromises()

    expect(global.fetch).toHaveBeenCalledWith('/api/v1/risks/risk-1', expect.any(Object))

    await wrapper.find('input').setValue('瓦斯超限处理')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(putMock).toHaveBeenCalledWith('/api/v1/risks/risk-1', {
      description: '瓦斯超限处理',
      category: '通风',
      location: '采掘线',
      level: '重大',
      controlMeasures: '加强通风'
    })
    expect(pushMock).toHaveBeenLastCalledWith({ name: 'risks' })
  })

  it('shows error message when save fails', async () => {
    postMock.mockRejectedValue(new Error('保存失败'))

    const wrapper = mount(RiskFormView)
    await flushPromises()

    await wrapper.find('input').setValue('冒顶失稳')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('保存失败')
  })
})

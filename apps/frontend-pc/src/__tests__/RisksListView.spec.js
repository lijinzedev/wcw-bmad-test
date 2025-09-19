import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import RisksListView from '../views/risk/RisksListView.vue'

const pushMock = vi.fn()
const downloadMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

vi.mock('../services/auth', () => ({
  api: {
    download: (...args) => downloadMock(...args)
  }
}))

const originalFetch = global.fetch

function buildFetchResponse(data, extra = {}) {
  const { ok = true, status = 200, headers = {} } = extra
  return Promise.resolve({
    ok,
    status,
    json: async () => data,
    headers: {
      get: (key) => {
        const normalized = key?.toLowerCase()
        if (normalized === 'x-total-count') return headers['X-Total-Count'] ?? headers['x-total-count'] ?? null
        if (normalized === 'x-total-pages') return headers['X-Total-Pages'] ?? headers['x-total-pages'] ?? null
        return headers[key] ?? null
      }
    }
  })
}

describe('RisksListView', () => {
  beforeEach(() => {
    pushMock.mockReset()
    downloadMock.mockReset()
    localStorage.clear()
    global.fetch = vi.fn((url) => buildFetchResponse([], {
      headers: {
        'X-Total-Count': '0',
        'X-Total-Pages': '1'
      }
    }))
  })

  afterEach(() => {
    global.fetch = originalFetch
  })

  it('loads risks and applies filters when querying', async () => {
    const fixtures = [
      { riskId: 'r1', description: '皮带巷积水', category: '通风', location: '西翼', level: '重大', responsibleOrgId: 'org-001' },
      { riskId: 'r2', description: '设备老化', category: '机电', location: '采区', level: '一般', responsibleOrgId: 'org-002' }
    ]
    const calls = []
    global.fetch = vi.fn((url) => {
      calls.push(url)
      return buildFetchResponse(fixtures, {
        headers: {
          'X-Total-Count': String(fixtures.length),
          'X-Total-Pages': '1'
        }
      })
    })

    const wrapper = mount(RisksListView)
    await flushPromises()

    expect(global.fetch).toHaveBeenCalledWith('/api/v1/risks?page=0&size=10', expect.any(Object))
    expect(wrapper.findAll('tbody tr')).toHaveLength(2)
    expect(wrapper.text()).toContain('皮带巷积水')

    await wrapper.find('input[placeholder="关键词搜索(描述/地点)"]').setValue('皮带')
    await wrapper.find('input[placeholder="专业"]').setValue('通风')
    await wrapper.find('input[placeholder="区域/地点"]').setValue('西翼')
    await wrapper.find('select').setValue('重大')

    const queryBtn = wrapper.findAll('button').find((btn) => btn.text() === '查询')
    expect(queryBtn).toBeDefined()
    await queryBtn.trigger('click')
    await flushPromises()

    const lastUrl = calls.at(-1)
    expect(lastUrl).toContain('q=%E7%9A%AE%E5%B8%A6')
    expect(lastUrl).toContain('category=%E9%80%9A%E9%A3%8E')
    expect(lastUrl).toContain('location=%E8%A5%BF%E7%BF%BC')
    expect(lastUrl).toContain('level=%E9%87%8D%E5%A4%A7')
  })

  it('invokes export download with active filters', async () => {
    const fixtures = [
      { riskId: 'r1', description: '瓦斯超限', category: '通风', location: '采掘线', level: '重大', responsibleOrgId: 'org-10001' }
    ]
    global.fetch = vi.fn(() => buildFetchResponse(fixtures, {
      headers: {
        'X-Total-Count': '1',
        'X-Total-Pages': '1'
      }
    }))

    const blob = new Blob(['risk'])
    downloadMock.mockResolvedValue(blob)

    const originalCreateObjectURL = global.URL.createObjectURL
    const originalRevokeObjectURL = global.URL.revokeObjectURL
    const originalCreateElement = document.createElement
    const createElementSpy = vi.spyOn(document, 'createElement')

    const clickMock = vi.fn()
    global.URL.createObjectURL = vi.fn(() => 'blob:url')
    global.URL.revokeObjectURL = vi.fn()
    createElementSpy.mockImplementation((tag) => {
      if (tag === 'a') {
        return {
          href: '',
          download: '',
          click: clickMock
        }
      }
      return originalCreateElement.call(document, tag)
    })

    const wrapper = mount(RisksListView)
    await flushPromises()

    await wrapper.find('input[placeholder="关键词搜索(描述/地点)"]').setValue('瓦斯')
    await wrapper.find('select').setValue('重大')

    const exportBtn = wrapper.findAll('button').find((btn) => btn.text() === '导出Excel')
    expect(exportBtn).toBeDefined()

    try {
      await exportBtn.trigger('click')
      await flushPromises()

      expect(downloadMock).toHaveBeenCalledWith('/api/v1/risks/export?q=%E7%93%A6%E6%96%AF&level=%E9%87%8D%E5%A4%A7')
      expect(global.URL.createObjectURL).toHaveBeenCalledWith(blob)
      expect(clickMock).toHaveBeenCalled()
    } finally {
      createElementSpy.mockRestore()
      global.URL.createObjectURL = originalCreateObjectURL
      global.URL.revokeObjectURL = originalRevokeObjectURL
    }
  })

  it('shows error message when loading fails', async () => {
    global.fetch = vi.fn(async () => ({
      ok: false,
      status: 500,
      json: async () => ([]),
      headers: { get: () => null }
    }))

    const wrapper = mount(RisksListView)
    await flushPromises()

    expect(wrapper.text()).toContain('加载失败 500')
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AlertAnalyticsView from '../views/analysis/AlertAnalyticsView.vue'
import * as echarts from 'echarts'

const pushMock = vi.fn()
const fetchAlertTrendMock = vi.fn()
const fetchAlertDistributionMock = vi.fn()
const fetchAlertTopMetricsMock = vi.fn()
const exportAlertAnalyticsCsvMock = vi.fn()
const chartInstances = []

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

vi.mock('../services/analytics.js', () => ({
  fetchAlertTrend: (...args) => fetchAlertTrendMock(...args),
  fetchAlertDistribution: (...args) => fetchAlertDistributionMock(...args),
  fetchAlertTopMetrics: (...args) => fetchAlertTopMetricsMock(...args),
  exportAlertAnalyticsCsv: (...args) => exportAlertAnalyticsCsvMock(...args)
}))

vi.mock('echarts', () => ({
  init: vi.fn(() => {
    const handlers = {}
    const instance = {
      setOption: vi.fn(),
      on: vi.fn((event, handler) => {
        handlers[event] = handler
      }),
      dispose: vi.fn(),
      __handlers: handlers
    }
    chartInstances.push(instance)
    return instance
  })
}))

describe('AlertAnalyticsView', () => {
  const defaultBuckets = [
    {
      bucketStart: '2024-01-01T00:00:00Z',
      bucketEnd: '2024-01-02T00:00:00Z',
      total: 3,
      severityCounts: { HIGH: 2, LOW: 1 }
    }
  ]
  const defaultMetrics = [
    { metricCode: 'GAS', total: 5 },
    { metricCode: 'TEMP', total: 2 }
  ]

  beforeEach(() => {
    pushMock.mockReset()
    fetchAlertTrendMock.mockReset()
    fetchAlertDistributionMock.mockReset()
    fetchAlertTopMetricsMock.mockReset()
    exportAlertAnalyticsCsvMock.mockReset()
    echarts.init.mockClear()
    chartInstances.length = 0

    const bucketsClone = defaultBuckets.map(item => ({
      bucketStart: item.bucketStart,
      bucketEnd: item.bucketEnd,
      total: item.total,
      severityCounts: { ...item.severityCounts }
    }))

    fetchAlertTrendMock.mockResolvedValue(bucketsClone)
    fetchAlertDistributionMock.mockResolvedValue(bucketsClone)
    fetchAlertTopMetricsMock.mockResolvedValue(defaultMetrics.map(item => ({ ...item })))
    exportAlertAnalyticsCsvMock.mockResolvedValue(new Blob(['metric,total\n']))
  })

  it('加载后请求分析数据并初始化图表', async () => {
    const wrapper = mount(AlertAnalyticsView)
    await flushPromises()

    expect(fetchAlertTrendMock).toHaveBeenCalled()
    expect(fetchAlertDistributionMock).toHaveBeenCalled()
    expect(fetchAlertTopMetricsMock).toHaveBeenCalled()
    expect(echarts.init).toHaveBeenCalledTimes(3)
    expect(chartInstances).toHaveLength(3)
    expect(wrapper.text()).toContain('预警分析看板')
  })

  it('点击趋势图数据点后跳转预警列表', async () => {
    mount(AlertAnalyticsView)
    await flushPromises()

    const trendChart = chartInstances[0]
    expect(trendChart).toBeTruthy()
    const clickHandler = trendChart.__handlers.click
    expect(clickHandler).toBeTypeOf('function')

    clickHandler({ dataIndex: 0 })
    expect(pushMock).toHaveBeenCalledWith({
      name: 'alerts-board',
      query: {
        from: defaultBuckets[0].bucketStart,
        to: defaultBuckets[0].bucketEnd
      }
    })
  })

  it('点击导出按钮会触发下载', async () => {
    const blob = new Blob(['metric,total\n'])
    exportAlertAnalyticsCsvMock.mockResolvedValueOnce(blob)

    const originalCreateObjectURL = global.URL?.createObjectURL
    const originalRevokeObjectURL = global.URL?.revokeObjectURL
    const originalCreateElement = document.createElement
    const clickMock = vi.fn()

    if (!global.URL) {
      global.URL = {}
    }
    global.URL.createObjectURL = vi.fn(() => 'blob:alert-analytics')
    global.URL.revokeObjectURL = vi.fn()

    const createElementSpy = vi.spyOn(document, 'createElement').mockImplementation((tagName, options) => {
      const element = originalCreateElement.call(document, tagName, options)
      if (tagName === 'a') {
        element.click = clickMock
      }
      return element
    })

    try {
      const wrapper = mount(AlertAnalyticsView)
      await flushPromises()

      const exportBtn = wrapper.findAll('button').find(btn => btn.text() === '导出 CSV')
      expect(exportBtn).toBeDefined()
      await exportBtn.trigger('click')
      await flushPromises()

      expect(exportAlertAnalyticsCsvMock).toHaveBeenCalledWith(expect.objectContaining({ bucket: 'day' }))
      expect(global.URL.createObjectURL).toHaveBeenCalledWith(blob)
      expect(clickMock).toHaveBeenCalled()
    } finally {
      createElementSpy.mockRestore()
      if (originalCreateObjectURL) {
        global.URL.createObjectURL = originalCreateObjectURL
      } else {
        delete global.URL.createObjectURL
      }
      if (originalRevokeObjectURL) {
        global.URL.revokeObjectURL = originalRevokeObjectURL
      } else {
        delete global.URL.revokeObjectURL
      }
    }
  })
})

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import BenchmarkCompareView from '../views/analysis/BenchmarkCompareView.vue'
import { clearCache } from '../stores/analysisStore.js'

const fetchBenchmarkTemplates = vi.fn()
const fetchBenchmarkTemplate = vi.fn()
const createBenchmarkTemplate = vi.fn()
const updateBenchmarkTemplate = vi.fn()
const compareBenchmark = vi.fn()
const fetchBenchmarkLatest = vi.fn()
const fetchOrganizations = vi.fn()

vi.mock('../services/analysis.js', () => ({
  fetchBenchmarkTemplates: (...args) => fetchBenchmarkTemplates(...args),
  fetchBenchmarkTemplate: (...args) => fetchBenchmarkTemplate(...args),
  createBenchmarkTemplate: (...args) => createBenchmarkTemplate(...args),
  updateBenchmarkTemplate: (...args) => updateBenchmarkTemplate(...args),
  compareBenchmark: (...args) => compareBenchmark(...args),
  fetchBenchmarkLatest: (...args) => fetchBenchmarkLatest(...args)
}))

vi.mock('../services/discipline.js', () => ({
  fetchOrganizations: (...args) => fetchOrganizations(...args)
}))

describe('BenchmarkCompareView', () => {
  beforeEach(() => {
    clearCache()
    fetchBenchmarkTemplates.mockReset()
    fetchBenchmarkTemplate.mockReset()
    createBenchmarkTemplate.mockReset()
    updateBenchmarkTemplate.mockReset()
    compareBenchmark.mockReset()
    fetchBenchmarkLatest.mockReset()
    fetchOrganizations.mockReset()

    fetchBenchmarkTemplates.mockResolvedValue({ data: [], total: 0, totalPages: 1 })
    fetchBenchmarkTemplate.mockResolvedValue({
      templateId: 'tpl-1',
      name: '季度模板',
      visibility: 'GROUP',
      organizationLevel: 'MINE',
      defaultOrganizationIds: ['org-1'],
      metrics: [
        { code: 'HAZARD_OPEN_TOTAL', displayName: '在办隐患', higherBetter: false, aggregation: 'SUM', weight: 1 }
      ]
    })
    createBenchmarkTemplate.mockResolvedValue({
      templateId: 'tpl-1',
      name: '季度模板',
      visibility: 'GROUP',
      organizationLevel: 'MINE',
      defaultOrganizationIds: ['org-1'],
      metrics: []
    })
    updateBenchmarkTemplate.mockResolvedValue({
      templateId: 'tpl-1',
      name: '季度模板',
      visibility: 'GROUP',
      organizationLevel: 'MINE',
      defaultOrganizationIds: ['org-1'],
      metrics: []
    })
    compareBenchmark.mockResolvedValue({
      rows: [
        {
          organizationId: 'org-1',
          organizationName: '示例矿井',
          rankOrder: 1,
          score: 3.2,
          metrics: [{ code: 'HAZARD_OPEN_TOTAL', value: 2 }]
        }
      ],
      categories: ['示例矿井'],
      series: [{ code: 'HAZARD_OPEN_TOTAL', name: '在办隐患', data: [2] }]
    })
    fetchBenchmarkLatest.mockResolvedValue({
      rows: [],
      categories: [],
      series: []
    })
    fetchOrganizations.mockResolvedValue([
      { organizationId: 'org-1', name: '示例矿井', type: 'MINE' },
      { organizationId: 'org-2', name: '示例公司', type: 'COMPANY' }
    ])
  })

  it('adds metric and runs compare request', async () => {
    const wrapper = mount(BenchmarkCompareView)
    await flushPromises()

    wrapper.vm.selectedOrganizations = ['org-1']
    wrapper.vm.selectedMetrics = [
      { code: 'HAZARD_OPEN_TOTAL', displayName: '在办隐患', higherBetter: false, aggregation: 'SUM', weight: 1 }
    ]
    await wrapper.vm.runCompare()

    expect(compareBenchmark).toHaveBeenCalled()
    const payload = compareBenchmark.mock.calls[0][0]
    expect(payload.organizationIds).toContain('org-1')
    expect(payload.metrics[0].code).toBe('HAZARD_OPEN_TOTAL')
  })

  it('saves template when form valid', async () => {
    const wrapper = mount(BenchmarkCompareView)
    await flushPromises()

    wrapper.vm.templateForm.name = '测试模板'
    wrapper.vm.selectedMetrics = [
      { code: 'HAZARD_OVERDUE_TOTAL', displayName: '超期隐患', higherBetter: false, aggregation: 'SUM', weight: 1 }
    ]

    await wrapper.vm.saveTemplate()
    expect(createBenchmarkTemplate).toHaveBeenCalled()
    const payload = createBenchmarkTemplate.mock.calls[0][0]
    expect(payload.metrics).toHaveLength(1)
    expect(payload.metrics[0].code).toBe('HAZARD_OVERDUE_TOTAL')
  })

  it('shows empty hint when no results', async () => {
    const wrapper = mount(BenchmarkCompareView)
    await flushPromises()
    const empty = wrapper.find('.empty')
    expect(empty.exists()).toBe(true)
  })
})

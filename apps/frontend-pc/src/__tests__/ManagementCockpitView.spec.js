import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ManagementCockpitView from '../views/analysis/ManagementCockpitView.vue'
import { clearCache } from '../stores/analysisStore.js'

const routerPushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerPushMock })
}))

const fetchDashboardMock = vi.fn()
const fetchDashboardByScopeMock = vi.fn()

const clone = (value) => JSON.parse(JSON.stringify(value))

vi.mock('../services/analysis.js', () => ({
  fetchDashboard: (...args) => fetchDashboardMock(...args),
  fetchDashboardByScope: (...args) => fetchDashboardByScopeMock(...args)
}))

const baseDashboard = {
  scope: 'GROUP',
  generatedAt: '2025-09-18T08:00:00Z',
  cacheExpiresAt: '2025-09-18T08:05:00Z',
  summary: {
    metrics: [
      { code: 'RISK_TOTAL', label: '风险总数', value: 12, unit: '项', precision: 0, extras: {} },
      { code: 'HAZARD_OPEN_TOTAL', label: '在办隐患', value: 5, unit: '条', precision: 0, extras: {} }
    ]
  },
  modules: [
    {
      code: 'risk-hazard',
      title: '风险与隐患概览',
      description: '集团层级风险与隐患集中度',
      metrics: [
        { code: 'RISK_TOTAL', label: '风险总数', value: 12, unit: '项', precision: 0, extras: {} },
        { code: 'HAZARD_MAJOR_RATIO', label: '重大隐患占比', value: 40, unit: '%', precision: 1, extras: { majorTotal: 2 } }
      ],
      series: [
        {
          code: 'HAZARD_OPEN_SERIES',
          name: '在办隐患 TOP',
          description: '集团范围数据',
          type: 'bar',
          categories: ['一号矿井', '二号矿井'],
          data: [3, 2],
          drillRouteName: 'hazards-board',
          drillParams: { status: 'open' }
        }
      ]
    }
  ],
  scopes: [
    { code: 'GROUP', label: '集团', description: '', active: true },
    { code: 'COMPANY', label: '公司', description: '', active: false },
    { code: 'MINE', label: '矿井', description: '', active: false }
  ]
}

const mineDashboard = {
  ...baseDashboard,
  scope: 'MINE',
  summary: {
    metrics: [
      { code: 'RISK_TOTAL', label: '风险总数', value: 4, unit: '项', precision: 0, extras: {} }
    ]
  },
  modules: [
    {
      code: 'risk-hazard',
      title: '矿井风险隐患',
      description: '',
      metrics: [
        { code: 'RISK_TOTAL', label: '风险总数', value: 4, unit: '项', precision: 0, extras: {} }
      ],
      series: [
        {
          code: 'ACCIDENT_SERIES',
          name: '近30天事故',
          description: '矿井数据',
          type: 'bar',
          categories: ['一号矿井'],
          data: [1],
          drillRouteName: 'accident-dashboard',
          drillParams: {}
        }
      ]
    }
  ],
  scopes: [
    { code: 'GROUP', label: '集团', description: '', active: false },
    { code: 'COMPANY', label: '公司', description: '', active: false },
    { code: 'MINE', label: '矿井', description: '', active: true }
  ]
}

describe('ManagementCockpitView', () => {
  beforeEach(() => {
    clearCache()
    routerPushMock.mockReset()
    fetchDashboardMock.mockReset()
    fetchDashboardByScopeMock.mockReset()
    fetchDashboardMock.mockResolvedValue(clone(baseDashboard))
    fetchDashboardByScopeMock.mockResolvedValue(clone(baseDashboard))
  })

  it('renders summary metrics and modules', async () => {
    const wrapper = mount(ManagementCockpitView)
    await flushPromises()

    expect(fetchDashboardMock).toHaveBeenCalled()
    expect(wrapper.text()).toContain('风险总数')
    expect(wrapper.text()).toContain('在办隐患')
    const rows = wrapper.findAll('tbody tr')
    expect(rows.length).toBeGreaterThan(0)
  })

  it('refreshes current scope on demand', async () => {
    const wrapper = mount(ManagementCockpitView)
    await flushPromises()

    fetchDashboardByScopeMock.mockResolvedValueOnce(clone(baseDashboard))
    await wrapper.find('.controls button').trigger('click')
    await flushPromises()

    expect(fetchDashboardByScopeMock).toHaveBeenCalledWith('GROUP', { refresh: true })
  })

  it('loads different scope and updates view', async () => {
    const wrapper = mount(ManagementCockpitView)
    await flushPromises()

    fetchDashboardByScopeMock.mockResolvedValueOnce(clone(mineDashboard))
    const select = wrapper.find('.controls select')
    await select.setValue('MINE')
    await select.trigger('change')
    await flushPromises()

    expect(fetchDashboardByScopeMock).toHaveBeenLastCalledWith('MINE', {})
    expect(wrapper.text()).toContain('矿井风险隐患')
  })
})

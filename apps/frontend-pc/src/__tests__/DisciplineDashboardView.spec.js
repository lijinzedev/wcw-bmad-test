import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import DisciplineDashboardView from '../views/discipline/DisciplineDashboardView.vue'

const fetchFlagsMock = vi.fn()
const createFlagMock = vi.fn()
const resolveFlagMock = vi.fn()
const evaluateRulesMock = vi.fn()
const fetchOrganizationsMock = vi.fn()

vi.mock('../services/discipline.js', () => ({
  fetchFlags: (...args) => fetchFlagsMock(...args),
  createFlag: (...args) => createFlagMock(...args),
  resolveFlag: (...args) => resolveFlagMock(...args),
  evaluateRules: (...args) => evaluateRulesMock(...args),
  fetchOrganizations: (...args) => fetchOrganizationsMock(...args)
}))

describe('DisciplineDashboardView', () => {
  beforeEach(() => {
    fetchFlagsMock.mockResolvedValue({
      data: [
        { flagId: 'flag-1', organizationId: 'org-1', organizationName: '一矿', severity: 'YELLOW', status: 'ACTIVE', deadline: '2030-01-01', reason: '多次超时整改' }
      ],
      total: 1,
      totalPages: 1
    })
    fetchOrganizationsMock.mockResolvedValue([
      { organizationId: 'org-1', name: '一矿', type: 'MINE' }
    ])
    createFlagMock.mockResolvedValue({ flagId: 'flag-2', organizationId: 'org-1', organizationName: '一矿', severity: 'RED', status: 'ACTIVE', reason: '测试', deadline: null })
    resolveFlagMock.mockResolvedValue({ flagId: 'flag-1', organizationId: 'org-1', organizationName: '一矿', severity: 'YELLOW', status: 'RESOLVED', reason: '多次超时整改' })
    evaluateRulesMock.mockResolvedValue([{ ruleId: 'rule-1', ruleName: '测试规则', triggeredCount: 1 }])
  })

  it('loads flags and organizations on mount', async () => {
    const wrapper = mount(DisciplineDashboardView)
    await flushPromises()
    expect(fetchFlagsMock).toHaveBeenCalled()
    expect(fetchOrganizationsMock).toHaveBeenCalledWith('MINE')
    expect(wrapper.text()).toContain('一矿')
  })

  it('runs evaluation and refreshes flags', async () => {
    const wrapper = mount(DisciplineDashboardView)
    await flushPromises()
    await wrapper.findAll('button').find(btn => btn.text().includes('评估')).trigger('click')
    await flushPromises()
    expect(evaluateRulesMock).toHaveBeenCalled()
    expect(fetchFlagsMock.mock.calls.length).toBeGreaterThanOrEqual(2)
  })

  it('submits manual flag', async () => {
    const wrapper = mount(DisciplineDashboardView)
    await flushPromises()
    const selects = wrapper.findAll('select')
    await selects[0].setValue('org-1')
    await wrapper.find('textarea').setValue('人工测试')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(createFlagMock).toHaveBeenCalledWith({ organizationId: 'org-1', severity: 'YELLOW', reason: '人工测试' })
    expect(wrapper.vm.flags[0].flagId).toBe('flag-2')
  })
})

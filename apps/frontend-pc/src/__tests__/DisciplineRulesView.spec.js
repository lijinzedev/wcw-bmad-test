import { describe, beforeEach, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import DisciplineRulesView from '../views/discipline/DisciplineRulesView.vue'

const fetchRulesMock = vi.fn()
const createRuleMock = vi.fn()
const updateRuleMock = vi.fn()

vi.mock('../services/discipline.js', () => ({
  fetchRules: (...args) => fetchRulesMock(...args),
  createRule: (...args) => createRuleMock(...args),
  updateRule: (...args) => updateRuleMock(...args)
}))

describe('DisciplineRulesView', () => {
  beforeEach(() => {
    fetchRulesMock.mockResolvedValue({
      data: [
        {
          ruleId: 'rule-1',
          name: '重大隐患黄牌',
          organizationLevel: 'MINE',
          metricType: 'HAZARD_MAJOR_COUNT',
          thresholdWindowDays: 30,
          thresholdValue: 2,
          severity: 'YELLOW',
          notificationChannels: ['SMS'],
          notes: '',
          active: true
        }
      ],
      total: 1,
      totalPages: 1
    })
    createRuleMock.mockResolvedValue({ ruleId: 'rule-new' })
    updateRuleMock.mockResolvedValue({ ruleId: 'rule-1' })
  })

  it('loads rules on mount', async () => {
    const wrapper = mount(DisciplineRulesView)
    await flushPromises()
    expect(fetchRulesMock).toHaveBeenCalled()
    expect(wrapper.text()).toContain('重大隐患黄牌')
  })

  it('submits new rule', async () => {
    const wrapper = mount(DisciplineRulesView)
    await flushPromises()
    await wrapper.find('input[placeholder="例如：重大隐患黄牌"]').setValue('新规则')
    const numberInputs = wrapper.findAll('input[type="number"]')
    await numberInputs[0].setValue('15')
    await numberInputs[1].setValue('5')
    await wrapper.find('textarea').setValue('说明')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(createRuleMock).toHaveBeenCalledWith(expect.objectContaining({ name: '新规则', thresholdWindowDays: 15, thresholdValue: 5 }))
  })

  it('enters edit mode and updates', async () => {
    const wrapper = mount(DisciplineRulesView)
    await flushPromises()
    await wrapper.findAll('button').find(btn => btn.text() === '编辑').trigger('click')
    await flushPromises()
    expect(wrapper.vm.editingRuleId).toBe('rule-1')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    expect(updateRuleMock).toHaveBeenCalledWith('rule-1', expect.objectContaining({ name: '重大隐患黄牌' }))
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import SlaSettingsView from '../views/monitoring/SlaSettingsView.vue'
import { clearSlaStore } from '../stores/slaStore.js'

const fetchSlaRules = vi.fn()
const createSlaRule = vi.fn()
const updateSlaRule = vi.fn()
const deleteSlaRule = vi.fn()

vi.mock('../services/sla.js', () => ({
  fetchSlaRules: (...a) => fetchSlaRules(...a),
  createSlaRule: (...a) => createSlaRule(...a),
  updateSlaRule: (...a) => updateSlaRule(...a),
  deleteSlaRule: (...a) => deleteSlaRule(...a)
}))

describe('SlaSettingsView', () => {
  beforeEach(() => {
    clearSlaStore?.()
    fetchSlaRules.mockReset(); createSlaRule.mockReset(); updateSlaRule.mockReset(); deleteSlaRule.mockReset()
    fetchSlaRules.mockResolvedValue({ data: [], total: 0 })
    createSlaRule.mockResolvedValue({})
  })

  it('loads rules on mount', async () => {
    mount(SlaSettingsView)
    await flushPromises()
    expect(fetchSlaRules).toHaveBeenCalled()
  })

  it('creates a rule', async () => {
    const wrapper = mount(SlaSettingsView)
    await flushPromises()
    await wrapper.find('form.form input').setValue('GAS')
    await wrapper.find('form.form button').trigger('submit')
    await flushPromises()
    expect(createSlaRule).toHaveBeenCalled()
  })
})


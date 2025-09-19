import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import NotificationSettingsView from '../views/notification/NotificationSettingsView.vue'
import { clearNotificationStore } from '../stores/notificationStore.js'

const fetchRules = vi.fn()
const createRule = vi.fn()
const updateRule = vi.fn()
const deleteRule = vi.fn()
const testSend = vi.fn()
const fetchLogs = vi.fn()

vi.mock('../services/notification.js', () => ({
  fetchRules: (...a) => fetchRules(...a),
  createRule: (...a) => createRule(...a),
  updateRule: (...a) => updateRule(...a),
  deleteRule: (...a) => deleteRule(...a),
  testSend: (...a) => testSend(...a),
  fetchLogs: (...a) => fetchLogs(...a)
}))

describe('NotificationSettingsView', () => {
  beforeEach(() => {
    clearNotificationStore?.()
    fetchRules.mockReset(); createRule.mockReset(); updateRule.mockReset(); deleteRule.mockReset(); testSend.mockReset(); fetchLogs.mockReset()
    fetchRules.mockResolvedValue({ data: [], total: 0 })
    fetchLogs.mockResolvedValue({ data: [], total: 0 })
    createRule.mockResolvedValue({})
  })

  it('loads rules and logs on mount', async () => {
    mount(NotificationSettingsView)
    await flushPromises()
    expect(fetchRules).toHaveBeenCalled()
    expect(fetchLogs).toHaveBeenCalled()
  })

  it('creates a new rule', async () => {
    const wrapper = mount(NotificationSettingsView)
    await flushPromises()
    await wrapper.find('form.form input').setValue('GAS')
    await wrapper.find('form.form button').trigger('submit')
    await flushPromises()
    expect(createRule).toHaveBeenCalled()
  })
})


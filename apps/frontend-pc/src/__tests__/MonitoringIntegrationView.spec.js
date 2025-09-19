import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import MonitoringIntegrationView from '../views/monitoring/MonitoringIntegrationView.vue'
import { clearMonitoringStore } from '../stores/monitoringStore.js'

const fetchThresholds = vi.fn()
const createThreshold = vi.fn()
const updateThreshold = vi.fn()
const deleteThreshold = vi.fn()
const fetchAlerts = vi.fn()
const triggerPoll = vi.fn()

vi.mock('../services/monitoring.js', () => ({
  fetchThresholds: (...args) => fetchThresholds(...args),
  createThreshold: (...args) => createThreshold(...args),
  updateThreshold: (...args) => updateThreshold(...args),
  deleteThreshold: (...args) => deleteThreshold(...args),
  fetchAlerts: (...args) => fetchAlerts(...args),
  triggerPoll: (...args) => triggerPoll(...args)
}))

describe('MonitoringIntegrationView', () => {
  beforeEach(() => {
    clearMonitoringStore()
    fetchThresholds.mockReset()
    createThreshold.mockReset()
    updateThreshold.mockReset()
    deleteThreshold.mockReset()
    fetchAlerts.mockReset()
    triggerPoll.mockReset()

    fetchThresholds.mockResolvedValue({ data: [{
      thresholdId: 't1',
      metricCode: 'GAS',
      metricName: '瓦斯',
      comparisonOperator: '>',
      thresholdValue: 80,
      unit: 'kPa',
      severity: 'HIGH',
      locationPattern: '主井',
      enabled: true
    }], total: 1 })
    fetchAlerts.mockResolvedValue({ data: [], total: 0 })
    createThreshold.mockResolvedValue({})
    updateThreshold.mockResolvedValue({})
    triggerPoll.mockResolvedValue({ status: 'SUCCESS', message: 'Fetched 1 events' })
  })

  it('loads thresholds on mount', async () => {
    mount(MonitoringIntegrationView)
    await flushPromises()
    expect(fetchThresholds).toHaveBeenCalled()
  })

  it('submits threshold form to create rule', async () => {
    const wrapper = mount(MonitoringIntegrationView)
    await flushPromises()
    await wrapper.find('form.form input').setValue('TEMP')
    await wrapper.find('form.form select').setValue('>')
    await wrapper.findAll('form.form input')[1].setValue('温度')
    await wrapper.find('form.form button').trigger('submit')
    await flushPromises()
    expect(createThreshold).toHaveBeenCalled()
  })
})


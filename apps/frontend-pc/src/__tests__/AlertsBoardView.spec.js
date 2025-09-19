import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AlertsBoardView from '../views/monitoring/AlertsBoardView.vue'

const fetchAlerts = vi.fn()
const ackAlert = vi.fn()
const assignAlert = vi.fn()
const escalateAlert = vi.fn()

vi.mock('../services/monitoring.js', () => ({
  fetchAlerts: (...a) => fetchAlerts(...a),
  ackAlert: (...a) => ackAlert(...a),
  assignAlert: (...a) => assignAlert(...a),
  escalateAlert: (...a) => escalateAlert(...a)
}))

describe('AlertsBoardView', () => {
  beforeEach(() => {
    fetchAlerts.mockReset(); ackAlert.mockReset(); assignAlert.mockReset(); escalateAlert.mockReset()
    fetchAlerts.mockResolvedValue({ data: [{ alertId: 'a1', occurredAt: new Date().toISOString(), metricCode: 'GAS', measuredValue: 100, unit: 'kPa', location: '主井', severity: 'HIGH', acknowledged: false }] })
    ackAlert.mockResolvedValue({ alertId: 'a1', acknowledged: true })
  })

  it('loads alerts on mount', async () => {
    mount(AlertsBoardView)
    await flushPromises()
    expect(fetchAlerts).toHaveBeenCalled()
  })

  it('acknowledges an alert', async () => {
    const wrapper = mount(AlertsBoardView)
    await flushPromises()
    await wrapper.find('tbody button').trigger('click')
    await flushPromises()
    expect(ackAlert).toHaveBeenCalled()
  })
})


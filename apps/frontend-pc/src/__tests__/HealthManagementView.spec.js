import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import HealthManagementView from '../views/health/HealthManagementView.vue'
import { clearHealthStore } from '../stores/healthStore.js'

const fetchHazardFactors = vi.fn()
const createHazardFactor = vi.fn()
const deleteHazardFactor = vi.fn()
const fetchExposures = vi.fn()
const createExposure = vi.fn()
const deleteExposure = vi.fn()
const fetchChecks = vi.fn()
const createCheck = vi.fn()
const deleteCheck = vi.fn()
const fetchCases = vi.fn()
const createCase = vi.fn()
const deleteCase = vi.fn()
const fetchFollowUps = vi.fn()

vi.mock('../services/health.js', () => ({
  fetchHazardFactors: (...args) => fetchHazardFactors(...args),
  createHazardFactor: (...args) => createHazardFactor(...args),
  deleteHazardFactor: (...args) => deleteHazardFactor(...args),
  fetchExposures: (...args) => fetchExposures(...args),
  createExposure: (...args) => createExposure(...args),
  deleteExposure: (...args) => deleteExposure(...args),
  fetchChecks: (...args) => fetchChecks(...args),
  createCheck: (...args) => createCheck(...args),
  deleteCheck: (...args) => deleteCheck(...args),
  fetchCases: (...args) => fetchCases(...args),
  createCase: (...args) => createCase(...args),
  deleteCase: (...args) => deleteCase(...args),
  fetchFollowUps: (...args) => fetchFollowUps(...args)
}))

describe('HealthManagementView', () => {
  beforeEach(() => {
    clearHealthStore()
    fetchHazardFactors.mockReset()
    createHazardFactor.mockReset()
    deleteHazardFactor.mockReset()
    fetchExposures.mockReset()
    createExposure.mockReset()
    deleteExposure.mockReset()
    fetchChecks.mockReset()
    createCheck.mockReset()
    deleteCheck.mockReset()
    fetchCases.mockReset()
    createCase.mockReset()
    deleteCase.mockReset()
    fetchFollowUps.mockReset()

    fetchHazardFactors.mockResolvedValue({ data: [{ factorId: 'f1', name: '粉尘' }], total: 1 })
    fetchExposures.mockResolvedValue({ data: [], total: 0 })
    fetchChecks.mockResolvedValue({ data: [], total: 0 })
    fetchCases.mockResolvedValue({ data: [], total: 0 })
    fetchFollowUps.mockResolvedValue([])
    createHazardFactor.mockResolvedValue({})
    createExposure.mockResolvedValue({})
    createCheck.mockResolvedValue({})
    createCase.mockResolvedValue({})
  })

  it('loads initial data and displays hazard factors', async () => {
    const wrapper = mount(HealthManagementView)
    await flushPromises()
    expect(fetchHazardFactors).toHaveBeenCalled()
    const rows = wrapper.findAll('tbody tr')
    expect(rows[0].text()).toContain('粉尘')
  })

  it('submits hazard factor form', async () => {
    const wrapper = mount(HealthManagementView)
    await flushPromises()

    await wrapper.find('form.form input').setValue('高温')
    await wrapper.find('form.form').trigger('submit')
    await flushPromises()

    expect(createHazardFactor).toHaveBeenCalled()
  })
})

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AccidentFormView from '../views/analysis/AccidentFormView.vue'

const routeParams = { value: {} }
const routerPushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: routeParams.value }),
  useRouter: () => ({ push: routerPushMock })
}))

const fetchAccidentMock = vi.fn()
const createAccidentMock = vi.fn()
const updateAccidentMock = vi.fn()
const presignMock = vi.fn()

vi.mock('../services/analysis.js', () => ({
  fetchAccident: (...args) => fetchAccidentMock(...args),
  createAccident: (...args) => createAccidentMock(...args),
  updateAccident: (...args) => updateAccidentMock(...args),
  presignAccidentAttachment: (...args) => presignMock(...args)
}))

vi.mock('../services/auth.js', () => ({
  auth: {
    getToken: () => 'token'
  }
}))

describe('AccidentFormView', () => {
  beforeEach(() => {
    routeParams.value = {}
    routerPushMock.mockReset()
    fetchAccidentMock.mockReset()
    createAccidentMock.mockReset()
    updateAccidentMock.mockReset()
    presignMock.mockReset()
    createAccidentMock.mockResolvedValue({})
    updateAccidentMock.mockResolvedValue({})
    fetchAccidentMock.mockResolvedValue({
      accidentId: 'accident-1',
      title: '已存在事故',
      occurredAt: new Date().toISOString(),
      relatedRisks: [{ id: 'risk-1', name: 'Risk 1' }],
      relatedHazards: [{ id: 'hazard-1', name: 'Hazard 1' }],
      attachments: []
    })
    presignMock.mockResolvedValue({
      key: 'presigned-key',
      uploadUrl: 'https://upload.example',
      downloadUrl: 'https://download.example',
      contentType: 'application/pdf',
      expiresInSeconds: 3600
    })

    global.fetch = vi.fn((url) => {
      if (typeof url === 'string' && url.startsWith('/api/v1/risks')) {
        return Promise.resolve({ ok: true, json: () => Promise.resolve([{ riskId: 'risk-1', description: '风险1' }]) })
      }
      if (typeof url === 'string' && url.startsWith('/api/v1/hazards')) {
        return Promise.resolve({ ok: true, json: () => Promise.resolve([{ hazardId: 'hazard-1', description: '隐患1' }]) })
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}), text: () => Promise.resolve('') })
    })
  })

  it('creates accident with selected references and attachment', async () => {
    const wrapper = mount(AccidentFormView)
    await flushPromises()

    wrapper.vm.toggleRisk('risk-1')
    wrapper.vm.toggleHazard('hazard-1')
    const file = new File(['PDF'], 'report.pdf', { type: 'application/pdf' })
    await wrapper.vm.handleFileChange({ target: { files: [file], value: '' } })

    wrapper.vm.form.title = '测试事故'
    wrapper.vm.form.status = '调查中'

    await wrapper.vm.save()
    expect(createAccidentMock).toHaveBeenCalled()
    const payload = createAccidentMock.mock.calls[0][0]
    expect(payload.title).toBe('测试事故')
    expect(payload.relatedRiskIds).toContain('risk-1')
    expect(payload.relatedHazardIds).toContain('hazard-1')
    expect(payload.attachments).toHaveLength(1)
  })

  it('loads existing accident and updates', async () => {
    routeParams.value = { id: 'accident-1' }
    const wrapper = mount(AccidentFormView)
    await flushPromises()

    expect(fetchAccidentMock).toHaveBeenCalledWith('accident-1')
    wrapper.vm.form.status = '已归档'
    await wrapper.vm.save()
    expect(updateAccidentMock).toHaveBeenCalledWith('accident-1', expect.objectContaining({ status: '已归档' }))
  })
})

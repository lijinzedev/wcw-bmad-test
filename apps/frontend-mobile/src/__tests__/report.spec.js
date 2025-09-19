import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ReportPage from '../pages/report/report.vue'

const uploadHazardMock = vi.fn().mockResolvedValue({ hazardId: 'h1' })
const searchRisksMock = vi.fn()

vi.mock('../services/api.js', () => ({
  uploadHazard: (...args) => uploadHazardMock(...args),
  searchRisks: (...args) => searchRisksMock(...args)
}))

const customTags = ['scroll-view', 'view', 'text', 'button', 'image', 'picker', 'textarea']

global.uni = {
  showToast: vi.fn(),
  chooseMedia: vi.fn().mockImplementation(({ success }) => success({ tempFiles: [{ tempFilePath: '/tmp/file.png' }] })),
  previewImage: vi.fn(),
  navigateTo: vi.fn(),
  redirectTo: vi.fn(),
  showActionSheet: vi.fn(),
  showModal: vi.fn(),
  reLaunch: vi.fn(),
  getStorageSync: vi.fn(() => 'token')
}

describe('ReportPage', () => {
  beforeEach(() => {
    uploadHazardMock.mockClear()
    searchRisksMock.mockReset()
    global.uni.showToast.mockClear()
  })

  it('validates description before submit', async () => {
    const wrapper = mount(ReportPage, {
      global: {
        config: {
          compilerOptions: {
            isCustomElement: (tag) => customTags.includes(tag)
          }
        }
      }
    })

    await wrapper.find('.submit').trigger('click')
    expect(global.uni.showToast).not.toHaveBeenCalled()
    expect(uploadHazardMock).not.toHaveBeenCalled()
  })

  it('submits hazard when form is valid', async () => {
    const wrapper = mount(ReportPage, {
      global: {
        config: {
          compilerOptions: {
            isCustomElement: (tag) => customTags.includes(tag)
          }
        }
      }
    })

    await wrapper.find('textarea').setValue('皮带巷有积水')
    await wrapper.vm.submit()

    expect(uploadHazardMock).toHaveBeenCalledTimes(1)
    const payload = uploadHazardMock.mock.calls[0][0]
    expect(payload.description).toBe('皮带巷有积水')
  })
})

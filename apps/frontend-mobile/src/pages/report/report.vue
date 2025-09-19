<template>
  <scroll-view class="report-page" scroll-y>
    <view class="section">
      <text class="section-title">隐患描述 *</text>
      <textarea class="textarea" v-model="form.description" placeholder="请用简洁语言描述隐患..." maxlength="500" />
      <view class="voice-row">
        <button class="voice-btn" size="mini" type="default" :loading="voiceLoading" @click="handleVoiceInput">语音转文字</button>
        <text class="voice-tip">语音识别结果将附加到描述</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">隐患等级</text>
      <picker mode="selector" :range="levels" @change="onLevelChange">
        <view class="picker">{{ form.level || '请选择等级（可选）' }}</view>
      </picker>
    </view>

    <view class="section">
      <text class="section-title">关联风险点</text>
      <input class="input" v-model="form.riskKeyword" placeholder="输入关键字搜索已有风险点（可选）" />
      <view class="risk-actions">
        <button size="mini" type="primary" :loading="riskLoading" @click="searchRisk">查询风险点</button>
        <button size="mini" type="warn" v-if="selectedRisk" @click="clearRisk">清除关联</button>
      </view>
      <view v-if="selectedRisk" class="risk-chip">
        <text class="risk-name">{{ selectedRisk.description }}</text>
        <text class="risk-meta">等级: {{ selectedRisk.level || '未标注' }}</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">位置</text>
      <input class="input" v-model="form.location" placeholder="请输入地点（可选）" />
    </view>

    <view class="section">
      <text class="section-title">现场照片 / 视频</text>
      <view class="attachments">
        <view class="attachment" v-for="(item, index) in attachments" :key="item">
          <image class="thumb" :src="item" mode="aspectFill" @click="previewAttachment(index)" />
          <text class="remove" @click="removeAttachment(index)">删除</text>
        </view>
        <view class="attachment add" @click="chooseMedia">
          <text>+</text>
        </view>
      </view>
      <text class="hint">最多可选择 3 张照片或视频</text>
    </view>

    <view class="section">
      <button class="submit" type="primary" :loading="loading" @click="submit">提交上报</button>
      <button class="secondary" type="default" @click="goMyReports">查看我的上报</button>
      <text v-if="error" class="error">{{ error }}</text>
    </view>
  </scroll-view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { ensureAuth } from '../../utils/auth.js'
import { uploadHazard, searchRisks } from '../../services/api.js'

const levels = ['重大', '较大', '一般', '低']
const form = reactive({
  description: '',
  level: '',
  location: '',
  riskKeyword: ''
})

const attachments = ref([])
const loading = ref(false)
const voiceLoading = ref(false)
const error = ref('')
const riskLoading = ref(false)
const selectedRisk = ref(null)

onShow(() => {
  if (!ensureAuth()) return
})

function onLevelChange(e) {
  const index = e.detail.value
  form.level = levels[index]
}

function chooseMedia() {
  if (attachments.value.length >= 3) {
    uni.showToast({ title: '最多上传3个附件', icon: 'none' })
    return
  }
  uni.chooseMedia({
    count: 3 - attachments.value.length,
    mediaType: ['image', 'video'],
    sourceType: ['camera', 'album'],
    success(res) {
      const files = res.tempFiles || []
      files.forEach(f => attachments.value.push(f.tempFilePath))
    }
  })
}

function previewAttachment(index) {
  uni.previewImage({
    current: attachments.value[index],
    urls: attachments.value
  })
}

function removeAttachment(index) {
  attachments.value.splice(index, 1)
}

async function handleVoiceInput() {
  voiceLoading.value = true
  try {
    if (typeof window !== 'undefined' && 'webkitSpeechRecognition' in window) {
      const recognition = new window.webkitSpeechRecognition()
      recognition.lang = 'zh-CN'
      recognition.onresult = (event) => {
        const text = event.results[0][0].transcript
        form.description = `${form.description} ${text}`.trim()
        uni.showToast({ title: '识别完成', icon: 'success' })
      }
      recognition.onerror = () => {
        uni.showToast({ title: '语音识别失败', icon: 'none' })
      }
      recognition.start()
    } else {
      uni.showModal({
        title: '提示',
        content: '设备未集成语音识别，将保存录音供人工录入。',
        showCancel: false
      })
    }
  } catch (e) {
    uni.showToast({ title: '暂不支持语音识别', icon: 'none' })
  } finally {
    voiceLoading.value = false
  }
}

async function submit() {
  if (!form.description.trim()) {
    error.value = '请填写隐患描述'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const payload = {
      description: form.description.trim(),
      level: form.level,
      location: form.location
    }
    if (selectedRisk.value) {
      payload.riskId = selectedRisk.value.riskId
    }
    const res = await uploadHazard(payload, attachments.value)
    uni.showToast({ title: '上报成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/my/reports' })
    }, 500)
    form.description = ''
    form.level = ''
    form.location = ''
    form.riskKeyword = ''
    attachments.value = []
    selectedRisk.value = null
  } catch (e) {
    error.value = e.message || '上报失败'
  } finally {
    loading.value = false
  }
}

function goMyReports() {
  if (!ensureAuth()) return
  uni.navigateTo({ url: '/pages/my/reports' })
}

async function searchRisk() {
  if (!form.riskKeyword.trim()) {
    uni.showToast({ title: '请输入关键字', icon: 'none' })
    return
  }
  riskLoading.value = true
  try {
    const list = await searchRisks(form.riskKeyword.trim())
    if (!Array.isArray(list) || list.length === 0) {
      uni.showToast({ title: '未找到匹配风险点', icon: 'none' })
      return
    }
    const itemNames = list.map(item => `${item.description} (${item.level || '未标注'})`)
    uni.showActionSheet({
      itemList: itemNames,
      success(res) {
        selectedRisk.value = list[res.tapIndex]
        uni.showToast({ title: '已关联风险点', icon: 'success', duration: 1000 })
      }
    })
  } catch (e) {
    uni.showToast({ title: '查询失败，请稍后再试', icon: 'none' })
  } finally {
    riskLoading.value = false
  }
}

function clearRisk() {
  selectedRisk.value = null
}
</script>

<style scoped>
.report-page {
  padding: 32rpx;
  box-sizing: border-box;
  min-height: 100vh;
}
.section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(15, 101, 202, 0.08);
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}
.textarea {
  width: 100%;
  margin-top: 20rpx;
  min-height: 180rpx;
  background: #f8f9fb;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
}
.voice-row {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  gap: 16rpx;
}
.voice-btn {
  border-radius: 16rpx;
}
.voice-tip {
  font-size: 24rpx;
  color: #909399;
}
.picker, .input {
  margin-top: 20rpx;
  padding: 24rpx;
  font-size: 28rpx;
  background: #f8f9fb;
  border-radius: 16rpx;
}
.risk-actions {
  margin-top: 16rpx;
  display: flex;
  gap: 16rpx;
}
.risk-chip {
  margin-top: 16rpx;
  padding: 20rpx;
  background: #eef5ff;
  border-radius: 16rpx;
  color: #0f65ca;
}
.risk-name {
  font-size: 28rpx;
  font-weight: 600;
}
.risk-meta {
  font-size: 24rpx;
  display: block;
}
.attachments {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.attachment {
  width: 180rpx;
  height: 180rpx;
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f2f3f7;
  display: flex;
  align-items: center;
  justify-content: center;
}
.attachment.add {
  border: 2rpx dashed #b0c4de;
  color: #7a7e83;
  font-size: 64rpx;
}
.thumb {
  width: 100%;
  height: 100%;
}
.remove {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}
.hint {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #a0a4ad;
}
.submit {
  width: 100%;
  background: #0f65ca;
  color: #fff;
  border-radius: 16rpx;
}
.secondary {
  width: 100%;
  margin-top: 16rpx;
  border-radius: 16rpx;
}
.error {
  display: block;
  margin-top: 18rpx;
  color: #ff4d4f;
  text-align: center;
  font-size: 24rpx;
}
</style>

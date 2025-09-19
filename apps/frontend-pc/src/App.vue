<template>
  <div id="app">
    <router-view/>
    <div class="toast-container" aria-live="polite" aria-atomic="true">
      <div class="toast" v-for="t in store.toasts" :key="t.id">
        <strong class="toast__title">{{ t.title }}</strong>
        <div class="toast__msg">{{ t.message }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted, onBeforeUnmount } from 'vue'
import { useNotifyStore, pushToast, incAlertsBadge } from './stores/notifyStore.js'
import { useSettingsStore } from './stores/settingsStore.js'
import { playBeep } from './services/sound.js'

export default {
  name: 'App',
  setup() {
    const store = useNotifyStore()
    const settings = useSettingsStore()
    const handler = (ev) => {
      try {
        const payload = ev.detail || {}
        const type = payload.type || 'EVENT'
        const data = payload.data || {}
        if (type === 'NEW_ALERT') {
          incAlertsBadge(1)
          pushToast('新的预警', `指标${data.metricCode || ''} 等级${data.severity || ''}`)
        } else if (type === 'ASSIGNED') {
          incAlertsBadge(1)
          pushToast('预警指派', `已指派至 ${data.assigneeName || data.assigneeId || ''}`)
        } else if (type === 'ACK_CHANGED') {
          pushToast('预警确认更新', data.acknowledged ? '已确认' : '已取消确认')
        } else if (type === 'ESCALATED') {
          pushToast('预警升级', `已升级为隐患 ${data.hazardId}`)
        }
        if (settings.soundEnabled && (type === 'NEW_ALERT' || type === 'ASSIGNED' || type === 'ESCALATED')) {
          playBeep()
        }
      } catch (e) {}
    }
    onMounted(() => window.addEventListener('monitoring-event', handler))
    onBeforeUnmount(() => window.removeEventListener('monitoring-event', handler))
    return { store }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
.toast-container { position: fixed; right: 16px; bottom: 16px; display: flex; flex-direction: column; gap: 8px; z-index: 9999; }
.toast { background: rgba(17,24,39,0.92); color: #fff; padding: 10px 12px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.25); text-align: left; min-width: 220px; }
.toast__title { font-weight: 600; }
.toast__msg { font-size: 13px; opacity: 0.9; margin-top: 2px; }
</style>

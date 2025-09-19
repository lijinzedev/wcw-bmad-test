<template>
  <div class="admin">
    <h1>管理后台</h1>
    <p>仅管理员可见的页面。</p>
    <div class="nav">
      <router-link to="/admin/users">用户管理</router-link>
      <router-link to="/admin/roles">角色管理</router-link>
      <router-link to="/hazards">隐患闭环</router-link>
      <router-link to="/behaviors">不安全行为</router-link>
      <router-link to="/inspections">监督检查</router-link>
      <router-link to="/discipline">红黄牌督办</router-link>
      <router-link to="/discipline/rules">红黄牌规则</router-link>
      <router-link to="/analysis/assessments">安全绩效考核</router-link>
      <router-link to="/analysis/assessments/config">考核配置</router-link>
      <router-link to="/analysis/accidents">事故分析</router-link>
      <router-link to="/analysis/accidents/new">事故填报</router-link>
      <router-link to="/analysis/benchmarks">跨单位对标</router-link>
      <router-link to="/analysis/dashboard">管理驾驶舱</router-link>
      <router-link to="/health">职业健康管理</router-link>
      <router-link to="/monitoring">监控系统对接</router-link>
      <router-link to="/alerts">预警看板 <span v-if="notify.alertsBadge" class="badge">{{ notify.alertsBadge }}</span></router-link>
      <router-link to="/admin/sla">预警SLA配置</router-link>
      <router-link to="/analysis/sla">SLA报表</router-link>
      <router-link to="/admin/notifications">通知联动设置</router-link>
      <router-link to="/admin/kb">知识库管理</router-link>
      <router-link v-if="isGov" to="/gov">政府监管门户</router-link>
    </div>
    <div class="panel">
      <h3>系统设置</h3>
      <label><input type="checkbox" :checked="settings.realtimeEnabled" @change="toggleRealtime($event)"/> 启用实时推送（SSE）</label>
      <label style="margin-left:16px"><input type="checkbox" :checked="settings.soundEnabled" @change="toggleSound($event)"/> 声音提示</label>
      <button style="margin-left:8px" @click="testBeep">测试音</button>
      <hr/>
      <button @click="loadProtected">校验受保护接口</button>
      <pre v-if="result">{{ result }}</pre>
      <div v-if="error" class="error">{{ error }}</div>
    </div>
  </div>
</template>

<script>
import { api, auth } from '../services/auth'
import { useSettingsStore, setRealtimeEnabled as setRealtimePref, setSoundEnabled } from '../stores/settingsStore'
import { setEnabled as setRealtime } from '../services/eventStream'
import { playBeep } from '../services/sound'

export default {
  name: 'AdminView',
  data() {
    return { result: '', error: '', settings: useSettingsStore() }
  },
  computed: {
    notify() {
      try { return require('../stores/notifyStore.js').useNotifyStore() } catch (e) { return { alertsBadge: 0 } }
    },
    isGov() { return (auth.getRoles() || []).includes('GOVERNMENT') }
  },
  methods: {
    toggleRealtime(e) {
      const on = e?.target?.checked ?? !this.settings.realtimeEnabled
      setRealtimePref(on)
      setRealtime(on)
    },
    toggleSound(e) {
      const on = e?.target?.checked ?? !this.settings.soundEnabled
      setSoundEnabled(on)
    },
    testBeep() { try { playBeep() } catch (e) {} },
    async loadProtected() {
      this.error = ''
      this.result = ''
      try {
        const data = await api.get('/api/v1/protected')
        this.result = JSON.stringify(data, null, 2)
      } catch (e) {
        this.error = e.message
      }
    }
  }
}
</script>

<style scoped>
.nav { display: flex; gap: 12px; margin: 8px 0; }
.badge { background: #ef4444; color: #fff; border-radius: 10px; padding: 1px 6px; font-size: 12px; margin-left: 6px; }
.panel { margin-top: 16px; text-align: left }
.error { color: #c00; }
</style>

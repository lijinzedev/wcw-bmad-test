<template>
  <div class="gov">
    <h1>政府监管门户</h1>
    <div class="nav">
      <router-link to="/">返回首页</router-link>
    </div>

    <section class="panel">
      <h2>监管范围内风险清单</h2>
      <div class="controls">
        <input v-model.trim="filters.q" placeholder="搜索..." @keyup.enter="loadRisks" />
        <button @click="loadRisks">查询</button>
      </div>
      <div v-if="riskError" class="error">{{ riskError }}</div>
      <table v-if="risks.length">
        <thead><tr><th>描述</th><th>位置</th><th>等级</th></tr></thead>
        <tbody>
          <tr v-for="r in risks" :key="r.riskId">
            <td>{{ r.description }}</td>
            <td>{{ r.location }}</td>
            <td>{{ r.level }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无数据</div>
    </section>

    <section class="panel">
      <h2>督办隐患跟踪</h2>
      <div class="controls">
        <select v-model="hazardFilters.status">
          <option value="">全部状态</option>
          <option>待指派</option>
          <option>整改中</option>
          <option>待验收</option>
          <option>已关闭</option>
        </select>
        <button @click="loadHazards">刷新</button>
      </div>
      <div v-if="hazardError" class="error">{{ hazardError }}</div>
      <table v-if="hazards.length">
        <thead><tr><th>描述</th><th>等级</th><th>状态</th><th>上报时间</th></tr></thead>
        <tbody>
          <tr v-for="h in hazards" :key="h.hazardId">
            <td>{{ h.description }}</td>
            <td>{{ h.level }}</td>
            <td>{{ h.status }}</td>
            <td>{{ (h.reportedAt || '').toString().replace('T',' ').substring(0,19) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无数据</div>
    </section>

    <section class="panel">
      <h2>录入执法隐患</h2>
      <form @submit.prevent="submitGovHazard">
        <div>
          <label>描述</label>
          <textarea v-model="form.description" required rows="3" />
        </div>
        <div>
          <label>等级</label>
          <input v-model="form.level" placeholder="一般/重大" />
        </div>
        <div>
          <label>位置</label>
          <input v-model="form.location" />
        </div>
        <div>
          <label>附件</label>
          <input type="file" multiple @change="onFiles" />
        </div>
        <div v-if="createError" class="error">{{ createError }}</div>
        <button type="submit" :disabled="creating">{{ creating ? '提交中...' : '提交' }}</button>
      </form>
    </section>
  </div>
</template>

<script>
import { fetchGovRisks, fetchGovHazards, createGovHazard } from '../../services/government'

export default {
  name: 'GovernmentDashboardView',
  data() {
    return {
      filters: { q: '' },
      risks: [], riskError: '',
      hazardFilters: { status: '' },
      hazards: [], hazardError: '',
      form: { description: '', level: '', location: '' },
      files: [], creating: false, createError: ''
    }
  },
  created() {
    this.loadRisks(); this.loadHazards()
  },
  methods: {
    async loadRisks() {
      this.riskError = ''
      try {
        const { data } = await fetchGovRisks({ q: this.filters.q })
        this.risks = data
      } catch (e) {
        this.riskError = e.message
      }
    },
    async loadHazards() {
      this.hazardError = ''
      try {
        const { data } = await fetchGovHazards({ status: this.hazardFilters.status })
        this.hazards = data
      } catch (e) {
        this.hazardError = e.message
      }
    },
    onFiles(ev) {
      this.files = Array.from(ev.target.files || [])
    },
    async submitGovHazard() {
      this.createError = ''
      this.creating = true
      try {
        const payload = { ...this.form }
        await createGovHazard(payload, this.files)
        this.form = { description: '', level: '', location: '' }
        this.files = []
        await this.loadHazards()
        alert('已提交政府督办隐患')
      } catch (e) {
        this.createError = e.message || '提交失败'
      } finally {
        this.creating = false
      }
    }
  }
}
</script>

<style scoped>
.gov { max-width: 900px; margin: 24px auto; text-align: left; }
.panel { margin: 16px 0; padding: 12px; border: 1px solid #ddd; border-radius: 6px; }
.controls { display: flex; gap: 8px; margin-bottom: 8px; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #eee; padding: 6px 8px; }
.empty { color: #666; }
.error { color: #c00; margin: 6px 0; }
textarea, input[type="text"], input[type="file"], select { width: 100%; padding: 6px; }
button { padding: 8px 12px; }
</style>


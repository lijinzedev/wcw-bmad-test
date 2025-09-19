<template>
  <div class="assessment-config">
    <h1>考核配置管理</h1>

    <div class="layout">
      <section class="list">
        <h2>已配置周期</h2>
        <button class="link" @click="resetForm">+ 新建周期</button>
        <ul>
          <li v-for="cycle in store.cycles" :key="cycle.cycleId" @click="editCycle(cycle)" :class="{ active: form.cycleId === cycle.cycleId }">
            <strong>{{ cycle.name }}</strong>
            <div class="meta">{{ cycle.level }} · 最近计算 {{ cycle.lastCalculatedAt ? formatDate(cycle.lastCalculatedAt) : '未计算' }}</div>
          </li>
        </ul>
      </section>

      <section class="editor">
        <h2>{{ form.cycleId ? '编辑周期' : '新建周期' }}</h2>
        <form @submit.prevent="save">
          <div class="grid">
            <label>
              名称
              <input v-model="form.name" required />
            </label>
            <label>
              考核层级
              <select v-model="form.level" required>
                <option value="">请选择</option>
                <option value="MINE">矿级</option>
                <option value="COMPANY">公司级</option>
                <option value="GROUP">集团级</option>
              </select>
            </label>
            <label>
              状态
              <input v-model="form.status" placeholder="例如：草稿 / 生效" />
            </label>
          </div>
          <div class="grid">
            <label>
              开始时间
              <input type="datetime-local" v-model="form.startAt" />
            </label>
            <label>
              结束时间
              <input type="datetime-local" v-model="form.endAt" />
            </label>
          </div>
          <label>
            备注
            <textarea v-model="form.notes" rows="3"></textarea>
          </label>

          <div class="indicator-header">
            <h3>指标配置 (总权重：{{ totalWeight.toFixed(2) }})</h3>
            <button type="button" @click="addIndicator">+ 新增指标</button>
          </div>

          <div class="indicator-list">
            <div v-for="(indicator, index) in form.indicators" :key="indicator.uid" class="indicator-item">
              <div class="indicator-grid">
                <label>
                  指标编码
                  <input v-model="indicator.code" required placeholder="如 HAZARD_OPEN_TOTAL" />
                </label>
                <label>
                  展示名称
                  <input v-model="indicator.displayName" placeholder="展示名称" />
                </label>
                <label>
                  权重
                  <input type="number" step="0.1" min="0" v-model.number="indicator.weight" required />
                </label>
                <label>
                  阈值
                  <input type="number" step="0.1" v-model.number="indicator.thresholdValue" placeholder="可选" />
                </label>
                <label>
                  趋势
                  <select v-model="indicator.higherBetter">
                    <option :value="true">越大越好</option>
                    <option :value="false">越小越好</option>
                  </select>
                </label>
              </div>
              <label>
                描述
                <textarea v-model="indicator.description" rows="2"></textarea>
              </label>
              <button type="button" class="remove" @click="removeIndicator(index)">删除指标</button>
            </div>
          </div>

          <div class="actions">
            <button type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存配置' }}</button>
            <button type="button" @click="resetForm">重置</button>
          </div>
          <div v-if="error" class="error">{{ error }}</div>
          <div v-if="message" class="message">{{ message }}</div>
        </form>
      </section>
    </div>
  </div>
</template>

<script>
import { fetchCycles, fetchCycle, createCycle, updateCycle } from '../../services/analysis.js'
import { useAnalysisStore, setCycles, clearCache } from '../../stores/analysisStore.js'

function isoToLocal(iso) {
  if (!iso) return ''
  const date = new Date(iso)
  const tz = date.getTimezoneOffset()
  const local = new Date(date.getTime() - tz * 60000)
  return local.toISOString().slice(0, 16)
}

function localToIso(value) {
  if (!value) return null
  const date = new Date(value)
  return date.toISOString()
}

export default {
  name: 'AssessmentConfigView',
  data() {
    return {
      store: useAnalysisStore(),
      form: this.emptyForm(),
      saving: false,
      error: '',
      message: ''
    }
  },
  computed: {
    totalWeight() {
      return this.form.indicators.reduce((sum, item) => sum + Number(item.weight || 0), 0)
    }
  },
  created() {
    this.loadCycles()
  },
  methods: {
    emptyForm() {
      return {
        cycleId: '',
        name: '',
        level: '',
        status: '',
        startAt: '',
        endAt: '',
        notes: '',
        indicators: []
      }
    },
    async loadCycles() {
      try {
        const { data } = await fetchCycles({ size: 50 })
        setCycles(data)
      } catch (e) {
        this.error = e.message || '加载考核周期失败'
      }
    },
    async editCycle(cycle) {
      try {
        const data = await fetchCycle(cycle.cycleId)
        this.form = {
          cycleId: data.cycleId,
          name: data.name,
          level: data.level,
          status: data.status || '',
          startAt: isoToLocal(data.startAt),
          endAt: isoToLocal(data.endAt),
          notes: data.notes || '',
          indicators: (data.indicators || []).map(item => ({
            uid: this.makeUid(),
            indicatorId: item.indicatorId,
            code: item.code,
            displayName: item.displayName,
            weight: item.weight,
            thresholdValue: item.thresholdValue,
            higherBetter: item.higherBetter,
            description: item.description || ''
          }))
        }
        this.message = ''
        this.error = ''
      } catch (e) {
        this.error = e.message || '加载考核详情失败'
      }
    },
    addIndicator() {
      this.form.indicators.push({
        uid: this.makeUid(),
        code: '',
        displayName: '',
        weight: 0,
        thresholdValue: null,
        higherBetter: true,
        description: ''
      })
    },
    removeIndicator(index) {
      this.form.indicators.splice(index, 1)
    },
    resetForm() {
      this.form = this.emptyForm()
      this.message = ''
      this.error = ''
    },
    async save() {
      if (!this.form.indicators.length) {
        this.error = '请至少配置一个指标'
        return
      }
      if (Math.abs(this.totalWeight - 100) > 0.5) {
        this.error = '指标权重之和必须为100'
        return
      }
      this.saving = true
      this.error = ''
      this.message = ''
      const payload = {
        name: this.form.name,
        level: this.form.level,
        status: this.form.status,
        startAt: localToIso(this.form.startAt),
        endAt: localToIso(this.form.endAt),
        notes: this.form.notes,
        indicators: this.form.indicators.map(item => ({
          indicatorId: item.indicatorId,
          code: item.code,
          displayName: item.displayName,
          weight: Number(item.weight),
          thresholdValue: item.thresholdValue === null || item.thresholdValue === undefined || item.thresholdValue === '' ? null : Number(item.thresholdValue),
          higherBetter: item.higherBetter,
          description: item.description
        }))
      }
      try {
        if (this.form.cycleId) {
          await updateCycle(this.form.cycleId, payload)
          this.message = '配置已更新'
        } else {
          const res = await createCycle(payload)
          this.form.cycleId = res.cycleId
          this.message = '配置已创建'
        }
        await this.loadCycles()
        clearCache()
      } catch (e) {
        this.error = e.message || '保存失败'
      } finally {
        this.saving = false
      }
    },
    formatDate(value) {
      if (!value) return ''
      const d = new Date(value)
      return d.toLocaleString()
    },
    makeUid() {
      return `ind-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
    }
  }
}
</script>

<style scoped>
.assessment-config { max-width: 1040px; margin: 0 auto; text-align: left; }
.layout { display: flex; gap: 24px; }
.list { width: 280px; border-right: 1px solid #e5e7eb; padding-right: 16px; }
.list ul { list-style: none; padding: 0; margin: 0; }
.list li { padding: 10px 12px; border-radius: 6px; cursor: pointer; margin-bottom: 8px; border: 1px solid transparent; }
.list li:hover { border-color: #93c5fd; }
.list li.active { border-color: #2563eb; background: #eff6ff; }
.list .meta { font-size: 12px; color: #6b7280; margin-top: 4px; }
.list .link { background: none; border: none; color: #2563eb; padding: 0; cursor: pointer; margin-bottom: 8px; }
.editor { flex: 1; }
.grid { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); margin-bottom: 12px; }
label { display: grid; gap: 6px; font-size: 14px; }
input, select, textarea { padding: 6px; border: 1px solid #d1d5db; border-radius: 4px; }
textarea { resize: vertical; }
.indicator-header { display: flex; align-items: center; justify-content: space-between; margin: 16px 0 8px; }
.indicator-header button { padding: 6px 10px; }
.indicator-list { display: grid; gap: 12px; }
.indicator-item { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; }
.indicator-grid { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); }
.remove { margin-top: 8px; padding: 6px 10px; background: #fee2e2; border: 1px solid #fca5a5; border-radius: 4px; cursor: pointer; }
.actions { display: flex; gap: 12px; margin-top: 16px; }
button { cursor: pointer; }
.error { color: #dc2626; margin-top: 12px; }
.message { color: #0f766e; margin-top: 12px; }
@media (max-width: 900px) {
  .layout { flex-direction: column; }
  .list { width: 100%; border-right: none; border-bottom: 1px solid #e5e7eb; padding-bottom: 16px; }
}
</style>

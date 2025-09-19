<template>
  <div class="rules">
    <h1>红黄牌规则配置</h1>

    <section class="panel">
      <h2>{{ editingRuleId ? '编辑规则' : '新建规则' }}</h2>
      <form class="form" @submit.prevent="submitRule">
        <label>
          规则名称
          <input v-model.trim="ruleForm.name" required placeholder="例如：重大隐患黄牌" />
        </label>
        <label>
          适用层级
          <select v-model="ruleForm.organizationLevel" required>
            <option value="MINE">矿级</option>
            <option value="COMPANY">公司级</option>
            <option value="GROUP">集团级</option>
          </select>
        </label>
        <label>
          指标类型
          <select v-model="ruleForm.metricType" required>
            <option value="HAZARD_MAJOR_COUNT">重大隐患数量</option>
            <option value="HAZARD_TOTAL_OPEN">未闭环隐患</option>
          </select>
        </label>
        <div class="grid">
          <label>
            统计周期(天)
            <input type="number" min="1" v-model.number="ruleForm.thresholdWindowDays" required />
          </label>
          <label>
            阈值
            <input type="number" min="1" v-model.number="ruleForm.thresholdValue" required />
          </label>
        </div>
        <label>
          严重级别
          <select v-model="ruleForm.severity" required>
            <option value="YELLOW">黄牌</option>
            <option value="RED">红牌</option>
          </select>
        </label>
        <label>
          通知渠道 (逗号分隔)
          <input v-model="ruleForm.notificationChannels" placeholder="SMS,APP" />
        </label>
        <label>
          备注
          <textarea v-model="ruleForm.notes" rows="3" placeholder="补充说明"></textarea>
        </label>
        <label class="checkbox">
          <input type="checkbox" v-model="ruleForm.active" /> 启用规则
        </label>
        <div v-if="ruleError" class="error">{{ ruleError }}</div>
        <div class="buttons">
          <button type="submit" :disabled="submitting">{{ submitting ? '保存中...' : editingRuleId ? '保存修改' : '创建规则' }}</button>
          <button type="button" v-if="editingRuleId" @click="resetForm">取消编辑</button>
        </div>
      </form>
    </section>

    <section class="panel">
      <h2>规则列表</h2>
      <div v-if="loadError" class="error">{{ loadError }}</div>
      <table v-if="rules.length">
        <thead>
          <tr>
            <th>名称</th>
            <th>层级</th>
            <th>指标</th>
            <th>周期</th>
            <th>阈值</th>
            <th>级别</th>
            <th>状态</th>
            <th>通知渠道</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="rule in rules" :key="rule.ruleId">
            <td>{{ rule.name }}</td>
            <td>{{ levelLabel(rule.organizationLevel) }}</td>
            <td>{{ metricLabel(rule.metricType) }}</td>
            <td>{{ rule.thresholdWindowDays }}</td>
            <td>{{ rule.thresholdValue }}</td>
            <td>{{ severityLabel(rule.severity) }}</td>
            <td>{{ rule.active ? '启用' : '停用' }}</td>
            <td>{{ (rule.notificationChannels || []).join(', ') }}</td>
            <td>
              <button @click="editRule(rule)">编辑</button>
              <button @click="toggleActive(rule)" :disabled="submitting">
                {{ rule.active ? '停用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无规则，请先创建。</div>
    </section>
  </div>
</template>

<script>
import { fetchRules, createRule, updateRule } from '../../services/discipline.js'

export default {
  name: 'DisciplineRulesView',
  data() {
    return {
      rules: [],
      loadError: '',
      ruleForm: this.getDefaultForm(),
      editingRuleId: '',
      ruleError: '',
      submitting: false
    }
  },
  created() {
    this.loadRules()
  },
  methods: {
    getDefaultForm() {
      return {
        name: '',
        organizationLevel: 'MINE',
        metricType: 'HAZARD_MAJOR_COUNT',
        thresholdWindowDays: 30,
        thresholdValue: 3,
        severity: 'YELLOW',
        notificationChannels: '',
        notes: '',
        active: true
      }
    },
    async loadRules() {
      this.loadError = ''
      try {
        const { data } = await fetchRules({ page: 0, size: 50 })
        this.rules = data
      } catch (e) {
        this.loadError = e.message || '加载规则失败'
      }
    },
    levelLabel(level) {
      return level === 'GROUP' ? '集团级' : level === 'COMPANY' ? '公司级' : '矿级'
    },
    metricLabel(metric) {
      if (metric === 'HAZARD_MAJOR_COUNT') return '重大隐患数量'
      if (metric === 'HAZARD_TOTAL_OPEN') return '未闭环隐患'
      return metric
    },
    severityLabel(severity) {
      return severity === 'RED' ? '红牌' : '黄牌'
    },
    async submitRule() {
      this.ruleError = ''
      this.submitting = true
      try {
        const payload = {
          name: this.ruleForm.name,
          organizationLevel: this.ruleForm.organizationLevel,
          metricType: this.ruleForm.metricType,
          thresholdWindowDays: Number(this.ruleForm.thresholdWindowDays),
          thresholdValue: Number(this.ruleForm.thresholdValue),
          severity: this.ruleForm.severity,
          notes: this.ruleForm.notes,
          active: this.ruleForm.active,
          notificationChannels: this.parseChannels(this.ruleForm.notificationChannels)
        }
        if (this.editingRuleId) {
          await updateRule(this.editingRuleId, payload)
        } else {
          await createRule(payload)
        }
        await this.loadRules()
        this.resetForm()
      } catch (e) {
        this.ruleError = e.message || '保存失败'
      } finally {
        this.submitting = false
      }
    },
    parseChannels(value) {
      if (!value) return []
      return value.split(',').map(s => s.trim()).filter(Boolean)
    },
    editRule(rule) {
      this.editingRuleId = rule.ruleId
      this.ruleForm = {
        name: rule.name,
        organizationLevel: rule.organizationLevel,
        metricType: rule.metricType,
        thresholdWindowDays: rule.thresholdWindowDays,
        thresholdValue: rule.thresholdValue,
        severity: rule.severity,
        notificationChannels: (rule.notificationChannels || []).join(', '),
        notes: rule.notes || '',
        active: rule.active
      }
    },
    resetForm() {
      this.editingRuleId = ''
      this.ruleForm = this.getDefaultForm()
      this.ruleError = ''
    },
    async toggleActive(rule) {
      if (this.submitting) return
      this.submitting = true
      try {
        await updateRule(rule.ruleId, {
          name: rule.name,
          organizationLevel: rule.organizationLevel,
          metricType: rule.metricType,
          thresholdWindowDays: rule.thresholdWindowDays,
          thresholdValue: rule.thresholdValue,
          severity: rule.severity,
          notes: rule.notes,
          active: !rule.active,
          notificationChannels: rule.notificationChannels
        })
        await this.loadRules()
      } catch (e) {
        this.ruleError = e.message || '更新失败'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.rules { max-width: 960px; margin: 24px auto; text-align: left; }
.panel { border: 1px solid #ddd; border-radius: 8px; padding: 16px; margin-bottom: 24px; background: #fff; }
.form { display: grid; gap: 12px; }
.grid { display: grid; gap: 10px; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); }
.checkbox { display: flex; align-items: center; gap: 8px; }
input, select, textarea { width: 100%; padding: 6px; box-sizing: border-box; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #eee; padding: 8px; }
.buttons { display: flex; gap: 12px; }
button { padding: 8px 14px; }
.error { color: #c00; }
.empty { color: #777; padding: 10px 0; }
</style>


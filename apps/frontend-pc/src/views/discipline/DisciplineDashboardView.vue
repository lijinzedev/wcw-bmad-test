<template>
  <div class="discipline">
    <h1>红黄牌督办看板</h1>

    <section class="panel">
      <div class="panel-header">
        <h2>当前挂牌单位</h2>
        <div class="actions">
          <button @click="loadFlags" :disabled="loadingFlags">{{ loadingFlags ? '刷新中...' : '刷新' }}</button>
          <button @click="runEvaluate" :disabled="evaluating">{{ evaluating ? '评估中...' : '评估规则' }}</button>
        </div>
      </div>
      <div v-if="evaluateMessage" class="info">{{ evaluateMessage }}</div>
      <div v-if="flagError" class="error">{{ flagError }}</div>
      <table v-if="flags.length">
        <thead>
          <tr>
            <th>单位</th>
            <th>严重级别</th>
            <th>状态</th>
            <th>截止</th>
            <th>原因</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="flag in flags" :key="flag.flagId" :class="flag.severity.toLowerCase()">
            <td>{{ flag.organizationName || flag.organizationId }}</td>
            <td>{{ severityLabel(flag.severity) }}</td>
            <td>{{ statusLabel(flag.status) }}</td>
            <td>{{ deadlineLabel(flag.deadline) }}</td>
            <td>{{ flag.reason }}</td>
            <td>
              <button v-if="flag.status === 'ACTIVE'" @click="resolve(flag)" :disabled="resolvingId === flag.flagId">摘牌</button>
              <span v-else>已摘牌</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无挂牌单位</div>
    </section>

    <section class="panel">
      <h2>手动挂牌</h2>
      <form class="form" @submit.prevent="submitFlag">
        <label>
          督办单位
          <select v-model="flagForm.organizationId" required>
            <option value="" disabled>请选择单位</option>
            <option v-for="org in organizations" :key="org.organizationId" :value="org.organizationId">
              {{ org.name }} ({{ levelLabel(org.type) }})
            </option>
          </select>
        </label>
        <label>
          严重级别
          <select v-model="flagForm.severity" required>
            <option value="YELLOW">黄牌</option>
            <option value="RED">红牌</option>
          </select>
        </label>
        <label>
          摘牌期限
          <input type="date" v-model="flagForm.deadline" />
        </label>
        <label>
          督办原因
          <textarea v-model="flagForm.reason" rows="3" required placeholder="请输入挂牌原因"></textarea>
        </label>
        <div v-if="createError" class="error">{{ createError }}</div>
        <button type="submit" :disabled="creatingFlag">{{ creatingFlag ? '创建中...' : '创建挂牌' }}</button>
      </form>
    </section>
  </div>
</template>

<script>
import { fetchFlags, createFlag, resolveFlag, evaluateRules, fetchOrganizations } from '../../services/discipline.js'

export default {
  name: 'DisciplineDashboardView',
  data() {
    return {
      flags: [],
      loadingFlags: false,
      flagError: '',
      evaluating: false,
      evaluateMessage: '',
      resolvingId: '',
      organizations: [],
      flagForm: {
        organizationId: '',
        severity: 'YELLOW',
        reason: '',
        deadline: ''
      },
      creatingFlag: false,
      createError: ''
    }
  },
  created() {
    this.loadFlags()
    this.loadOrganizations()
  },
  methods: {
    async loadFlags() {
      this.loadingFlags = true
      this.flagError = ''
      try {
        const { data } = await fetchFlags({ status: 'ACTIVE' })
        this.flags = data
      } catch (e) {
        this.flagError = e.message || '加载挂牌信息失败'
      } finally {
        this.loadingFlags = false
      }
    },
    async loadOrganizations() {
      try {
        const res = await fetchOrganizations('MINE')
        this.organizations = res
      } catch (e) {
        console.warn('加载组织失败', e)
      }
    },
    severityLabel(severity) {
      return severity === 'RED' ? '红牌' : '黄牌'
    },
    statusLabel(status) {
      return status === 'RESOLVED' ? '已摘牌' : '督办中'
    },
    deadlineLabel(deadline) {
      if (!deadline) return '未设置'
      return deadline
    },
    levelLabel(level) {
      return level === 'COMPANY' ? '公司级' : level === 'GROUP' ? '集团级' : '矿级'
    },
    async runEvaluate() {
      this.evaluating = true
      this.evaluateMessage = ''
      try {
        const results = await evaluateRules()
        if (Array.isArray(results) && results.length) {
          this.evaluateMessage = results.map(r => `${r.ruleName} 触发 ${r.triggeredCount} 次`).join('；')
        } else {
          this.evaluateMessage = '已评估，暂无触发'
        }
        await this.loadFlags()
      } catch (e) {
        this.evaluateMessage = e.message || '评估失败'
      } finally {
        this.evaluating = false
      }
    },
    async submitFlag() {
      this.createError = ''
      this.creatingFlag = true
      try {
        const payload = { ...this.flagForm }
        if (!payload.deadline) delete payload.deadline
        const created = await createFlag(payload)
        this.flags.unshift(created)
        this.flagForm = { organizationId: '', severity: 'YELLOW', reason: '', deadline: '' }
      } catch (e) {
        this.createError = e.message || '创建失败'
      } finally {
        this.creatingFlag = false
      }
    },
    async resolve(flag) {
      this.resolvingId = flag.flagId
      try {
        const updated = await resolveFlag(flag.flagId, { resolutionNote: '管理员手动摘牌' })
        this.flags = this.flags.map(f => (f.flagId === updated.flagId ? updated : f))
      } catch (e) {
        this.flagError = e.message || '摘牌失败'
      } finally {
        this.resolvingId = ''
      }
    }
  }
}
</script>

<style scoped>
.discipline { max-width: 960px; margin: 24px auto; text-align: left; }
.panel { border: 1px solid #ddd; border-radius: 8px; padding: 16px; margin-bottom: 24px; background: #fff; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.actions { display: flex; gap: 10px; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #eee; padding: 8px; }
tr.red { background: #ffecec; }
tr.yellow { background: #fff6d9; }
.empty { color: #777; padding: 12px 0; }
.error { color: #c00; margin-top: 8px; }
.info { color: #0366d6; margin-bottom: 8px; }
.form { display: grid; gap: 12px; }
textarea, select, input[type="date"] { width: 100%; padding: 6px; }
button { padding: 8px 14px; }
</style>


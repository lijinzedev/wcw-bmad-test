<template>
  <div class="health">
    <header class="health__header">
      <h1>职业健康档案管理</h1>
      <p class="hint">维护职业病危害因素、接害人员、健康体检与病例信息，并查看需复查人员。</p>
    </header>

    <nav class="tabs">
      <button v-for="tab in tabs" :key="tab.value" :class="{ active: currentTab === tab.value }" @click="currentTab = tab.value">
        {{ tab.label }}
      </button>
    </nav>

    <section v-if="currentTab === 'factors'">
      <header class="section-header">
        <h2>危害因素清单 ({{ store.totals.factors }})</h2>
        <div class="actions">
          <input v-model="factorKeyword" placeholder="搜索名称或分类" @input="loadFactors" />
        </div>
      </header>
      <table class="data-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>分类</th>
            <th>限值</th>
            <th>创建人</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="factor in store.factors" :key="factor.factorId">
            <td>{{ factor.name }}</td>
            <td>{{ factor.category || '-' }}</td>
            <td>{{ factor.limitValue != null ? `${factor.limitValue} ${factor.limitUnit || ''}` : '-' }}</td>
            <td>{{ factor.createdByName || '-' }}</td>
            <td><button @click="removeFactor(factor.factorId)">删除</button></td>
          </tr>
          <tr v-if="!store.factors.length">
            <td colspan="5" class="empty">暂无危害因素记录</td>
          </tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveFactor">
        <h3>新增危害因素</h3>
        <div class="form-row">
          <label>名称<input v-model="factorForm.name" required /></label>
          <label>分类<input v-model="factorForm.category" /></label>
          <label>限值<input type="number" step="0.01" v-model.number="factorForm.limitValue" /></label>
          <label>单位<input v-model="factorForm.limitUnit" /></label>
        </div>
        <div class="form-row">
          <label>评估方法<input v-model="factorForm.assessmentMethod" /></label>
        </div>
        <label>描述<textarea v-model="factorForm.description" rows="3"></textarea></label>
        <button type="submit">保存</button>
      </form>
    </section>

    <section v-else-if="currentTab === 'exposures'">
      <header class="section-header">
        <h2>接害人员档案 ({{ store.totals.exposures }})</h2>
        <div class="actions">
          <input v-model="exposureKeyword" placeholder="搜索人员/岗位" @input="loadExposures" />
        </div>
      </header>
      <table class="data-table">
        <thead>
          <tr>
            <th>人员</th>
            <th>岗位</th>
            <th>危害因素</th>
            <th>开始日期</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="exposure in store.exposures" :key="exposure.exposureId">
            <td>{{ exposure.employeeName }}</td>
            <td>{{ exposure.positionTitle || '-' }}</td>
            <td>{{ exposure.factorName || '-' }}</td>
            <td>{{ exposure.startDate || '-' }}</td>
            <td><button @click="removeExposure(exposure.exposureId)">删除</button></td>
          </tr>
          <tr v-if="!store.exposures.length">
            <td colspan="5" class="empty">暂无接害人员档案</td>
          </tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveExposure">
        <h3>新增接害人员</h3>
        <div class="form-row">
          <label>危害因素<select v-model="exposureForm.factorId">
            <option value="">请选择</option>
            <option v-for="factor in store.factors" :key="factor.factorId" :value="factor.factorId">{{ factor.name }}</option>
          </select></label>
          <label>接害人员<input v-model="exposureForm.employeeName" required /></label>
          <label>岗位<input v-model="exposureForm.positionTitle" /></label>
        </div>
        <div class="form-row">
          <label>开始日期<input type="date" v-model="exposureForm.startDate" /></label>
          <label>结束日期<input type="date" v-model="exposureForm.endDate" /></label>
        </div>
        <label>备注<textarea v-model="exposureForm.notes" rows="2"></textarea></label>
        <button type="submit">保存</button>
      </form>
    </section>

    <section v-else-if="currentTab === 'checks'">
      <header class="section-header">
        <h2>健康体检记录 ({{ store.totals.checks }})</h2>
        <div class="actions">
          <label><input type="checkbox" v-model="onlyFollowUps" @change="loadChecks" /> 仅显示需复查</label>
        </div>
      </header>
      <table class="data-table">
        <thead>
          <tr>
            <th>人员</th>
            <th>类型</th>
            <th>体检日期</th>
            <th>结论</th>
            <th>下次体检</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in store.checks" :key="record.checkId">
            <td>{{ record.employeeName }}</td>
            <td>{{ record.checkType }}</td>
            <td>{{ record.checkDate }}</td>
            <td>{{ record.medicalConclusion || '-' }}</td>
            <td>{{ record.nextCheckDate || '-' }}</td>
            <td><button @click="removeCheck(record.checkId)">删除</button></td>
          </tr>
          <tr v-if="!store.checks.length">
            <td colspan="6" class="empty">暂无体检记录</td>
          </tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveCheck">
        <h3>新增体检记录</h3>
        <div class="form-row">
          <label>接害档案<select v-model="checkForm.exposureId" required>
            <option value="">请选择</option>
            <option v-for="exposure in store.exposures" :key="exposure.exposureId" :value="exposure.exposureId">{{ exposure.employeeName }} - {{ exposure.factorName }}</option>
          </select></label>
          <label>体检类别<input v-model="checkForm.checkType" /></label>
          <label>体检日期<input type="date" v-model="checkForm.checkDate" /></label>
        </div>
        <div class="form-row">
          <label>结论<input v-model="checkForm.medicalConclusion" /></label>
          <label>医生<input v-model="checkForm.doctorName" /></label>
        </div>
        <label>复查说明<input v-model="checkForm.followUpReason" /></label>
        <label>下次体检<input type="date" v-model="checkForm.nextCheckDate" /></label>
        <label><input type="checkbox" v-model="checkForm.followUpNeeded" /> 需要复查</label>
        <button type="submit">保存</button>
      </form>

      <aside class="followups">
        <h3>需要复查/调岗人员</h3>
        <ul>
          <li v-for="item in store.followUps" :key="item.employeeId + (item.exposureId || '')">
            {{ item.employeeName }} - 下次体检：{{ item.nextCheckDate || '待定' }} ({{ item.followUpReason }})
          </li>
          <li v-if="!store.followUps.length" class="empty">暂无待复查提醒</li>
        </ul>
      </aside>
    </section>

    <section v-else>
      <header class="section-header">
        <h2>职业病病例档案 ({{ store.totals.cases }})</h2>
      </header>
      <table class="data-table">
        <thead>
          <tr>
            <th>人员</th>
            <th>诊断</th>
            <th>诊断日期</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in store.cases" :key="item.caseId">
            <td>{{ item.employeeName }}</td>
            <td>{{ item.diagnosis }}</td>
            <td>{{ item.diagnosisDate || '-' }}</td>
            <td>{{ item.status || '-' }}</td>
            <td><button @click="removeCase(item.caseId)">删除</button></td>
          </tr>
          <tr v-if="!store.cases.length">
            <td colspan="5" class="empty">暂无病例档案</td>
          </tr>
        </tbody>
      </table>

      <form class="form" @submit.prevent="saveCase">
        <h3>新增病例</h3>
        <div class="form-row">
          <label>人员<input v-model="caseForm.employeeName" required /></label>
          <label>诊断<input v-model="caseForm.diagnosis" required /></label>
          <label>诊断日期<input type="date" v-model="caseForm.diagnosisDate" /></label>
        </div>
        <div class="form-row">
          <label>状态<input v-model="caseForm.status" /></label>
        </div>
        <label>备注<textarea v-model="caseForm.notes" rows="3"></textarea></label>
        <button type="submit">保存</button>
      </form>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="message" class="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  fetchHazardFactors,
  createHazardFactor,
  deleteHazardFactor,
  fetchExposures,
  createExposure,
  deleteExposure,
  fetchChecks,
  createCheck,
  deleteCheck,
  fetchCases,
  createCase,
  deleteCase,
  fetchFollowUps
} from '../../services/health.js'
import {
  useHealthStore,
  setFactors,
  setExposures,
  setChecks,
  setCases,
  setFollowUps
} from '../../stores/healthStore.js'

const clone = value => JSON.parse(JSON.stringify(value ?? {}))

const store = useHealthStore()
const tabs = [
  { label: '危害因素', value: 'factors' },
  { label: '接害档案', value: 'exposures' },
  { label: '健康体检', value: 'checks' },
  { label: '病例档案', value: 'cases' }
]
const currentTab = ref('factors')
const error = ref('')
const message = ref('')

const factorKeyword = ref('')
const exposureKeyword = ref('')
const onlyFollowUps = ref(false)

const factorForm = reactive({
  name: '',
  category: '',
  description: '',
  assessmentMethod: '',
  limitValue: null,
  limitUnit: ''
})

const exposureForm = reactive({
  factorId: '',
  employeeName: '',
  positionTitle: '',
  startDate: '',
  endDate: '',
  notes: ''
})

const checkForm = reactive({
  exposureId: '',
  checkType: '',
  checkDate: '',
  medicalConclusion: '',
  doctorName: '',
  followUpNeeded: false,
  followUpReason: '',
  nextCheckDate: ''
})

const caseForm = reactive({
  employeeName: '',
  diagnosis: '',
  diagnosisDate: '',
  status: '',
  notes: ''
})

const factorOptions = computed(() => store.factors.map(f => ({ id: f.factorId, name: f.name })))

async function loadFactors() {
  try {
    const { data, total } = await fetchHazardFactors({ keyword: factorKeyword.value })
    setFactors(data, total)
  } catch (e) {
    error.value = e.message
  }
}

async function loadExposures() {
  try {
    const { data, total } = await fetchExposures({ keyword: exposureKeyword.value })
    setExposures(data, total)
  } catch (e) {
    error.value = e.message
  }
}

async function loadChecks() {
  try {
    const { data, total } = await fetchChecks({ onlyFollowUps: onlyFollowUps.value })
    setChecks(data, total)
    const followUps = await fetchFollowUps()
    setFollowUps(followUps)
  } catch (e) {
    error.value = e.message
  }
}

async function loadCases() {
  try {
    const { data, total } = await fetchCases()
    setCases(data, total)
  } catch (e) {
    error.value = e.message
  }
}

async function saveFactor() {
  try {
    await createHazardFactor(clone(factorForm))
    message.value = '危害因素已保存'
    Object.assign(factorForm, { name: '', category: '', description: '', assessmentMethod: '', limitValue: null, limitUnit: '' })
    await loadFactors()
  } catch (e) {
    error.value = e.message
  }
}

async function removeFactor(id) {
  if (!confirm('确认删除该危害因素吗？')) return
  try {
    await deleteHazardFactor(id)
    await loadFactors()
  } catch (e) {
    error.value = e.message
  }
}

async function saveExposure() {
  try {
    const payload = {
      ...clone(exposureForm),
      factorId: exposureForm.factorId || null
    }
    await createExposure(payload)
    message.value = '接害档案已保存'
    Object.assign(exposureForm, { factorId: '', employeeName: '', positionTitle: '', startDate: '', endDate: '', notes: '' })
    await loadExposures()
  } catch (e) {
    error.value = e.message
  }
}

async function removeExposure(id) {
  if (!confirm('确认删除该接害档案吗？')) return
  try {
    await deleteExposure(id)
    await loadExposures()
  } catch (e) {
    error.value = e.message
  }
}

async function saveCheck() {
  try {
    const payload = {
      ...clone(checkForm),
      followUpNeeded: !!checkForm.followUpNeeded
    }
    await createCheck(payload)
    message.value = '体检记录已保存'
    Object.assign(checkForm, { exposureId: '', checkType: '', checkDate: '', medicalConclusion: '', doctorName: '', followUpNeeded: false, followUpReason: '', nextCheckDate: '' })
    await loadChecks()
  } catch (e) {
    error.value = e.message
  }
}

async function removeCheck(id) {
  if (!confirm('确认删除该体检记录吗？')) return
  try {
    await deleteCheck(id)
    await loadChecks()
  } catch (e) {
    error.value = e.message
  }
}

async function saveCase() {
  try {
    await createCase(clone(caseForm))
    message.value = '病例档案已保存'
    Object.assign(caseForm, { employeeName: '', diagnosis: '', diagnosisDate: '', status: '', notes: '' })
    await loadCases()
  } catch (e) {
    error.value = e.message
  }
}

async function removeCase(id) {
  if (!confirm('确认删除该病例档案吗？')) return
  try {
    await deleteCase(id)
    await loadCases()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(async () => {
  await loadFactors()
  await loadExposures()
})

watch(currentTab, async value => {
  if (value === 'checks') {
    await loadChecks()
  } else if (value === 'cases') {
    await loadCases()
  }
})
</script>

<style scoped>
.health { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.health__header { margin-bottom: 16px; }
.hint { color: #64748b; margin-top: 4px; }
.tabs { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.tabs button { padding: 8px 16px; background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 20px; cursor: pointer; }
.tabs button.active { background: #2563eb; color: #fff; }
.section-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 12px; flex-wrap: wrap; }
.actions input { padding: 6px 10px; }
.data-table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
.data-table th, .data-table td { border: 1px solid #e5e7eb; padding: 8px; }
.data-table th { background: #f8fafc; }
.empty { color: #94a3b8; text-align: center; }
.form { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-top: 16px; display: flex; flex-direction: column; gap: 12px; }
.form-row { display: flex; gap: 12px; flex-wrap: wrap; }
.form label { display: flex; flex-direction: column; gap: 4px; flex: 1 1 200px; }
.form input, .form select, .form textarea { padding: 6px 8px; border: 1px solid #cbd5f5; border-radius: 4px; }
.form button { align-self: flex-start; padding: 8px 16px; }
.followups { margin-top: 16px; background: #f1f5f9; border-radius: 8px; padding: 12px; }
.followups ul { margin: 0; padding-left: 16px; }
.error { color: #dc2626; margin-top: 12px; }
.message { color: #0f766e; margin-top: 12px; }
@media (max-width: 768px) {
  .form-row { flex-direction: column; }
}
</style>

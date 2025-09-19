<template>
  <div class="kb">
    <h1>知识库管理</h1>
    <section class="card">
      <header class="section-header">
        <input v-model="keyword" placeholder="搜索标题/标签" @input="load" />
        <label><input type="checkbox" v-model="onlyEnabled" @change="load" /> 仅启用</label>
      </header>
      <table class="data-table">
        <thead><tr><th>标题</th><th>启用</th><th>更新时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="a in store.articles" :key="a.articleId">
            <td>{{ a.title }}</td>
            <td>{{ a.enabled ? '是' : '否' }}</td>
            <td>{{ a.updatedAt || '-' }}</td>
            <td>
              <button @click="editArticle(a)">编辑</button>
              <button @click="removeArticle(a.articleId)">删除</button>
            </td>
          </tr>
          <tr v-if="!store.articles.length"><td colspan="4" class="empty">暂无文章</td></tr>
        </tbody>
      </table>
      <form class="form" @submit.prevent="saveArticle">
        <h3>{{ form.articleId ? '编辑文章' : '新增文章' }}</h3>
        <label>标题<input v-model="form.title" required /></label>
        <label>标签（逗号分隔）<input v-model="tagsInput" /></label>
        <label>内容<textarea v-model="form.content" rows="5"/></label>
        <label><input type="checkbox" v-model="form.enabled" /> 启用</label>
        <div class="form-actions"><button type="submit">保存</button><button type="button" @click="resetForm">重置</button></div>
      </form>
    </section>

    <section class="card">
      <header class="section-header">
        <h2>映射规则（按指标/级别/地点→文章）</h2>
      </header>
      <table class="data-table">
        <thead><tr><th>指标</th><th>级别</th><th>地点匹配</th><th>文章ID</th><th>优先级</th><th>启用</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="m in store.mappings" :key="m.ruleId">
            <td>{{ m.metricCode }}</td>
            <td>{{ m.severity || '-' }}</td>
            <td>{{ m.locationPattern || '-' }}</td>
            <td>{{ m.articleId }}</td>
            <td>{{ m.priority ?? 0 }}</td>
            <td>{{ m.enabled ? '是' : '否' }}</td>
            <td>
              <button @click="editMapping(m)">编辑</button>
              <button @click="removeMapping(m.ruleId)">删除</button>
            </td>
          </tr>
          <tr v-if="!store.mappings.length"><td colspan="7" class="empty">暂无映射</td></tr>
        </tbody>
      </table>
      <form class="form" @submit.prevent="saveMapping">
        <h3>{{ mapForm.ruleId ? '编辑映射' : '新增映射' }}</h3>
        <div class="row">
          <label>指标<input v-model="mapForm.metricCode" required /></label>
          <label>级别<input v-model="mapForm.severity" /></label>
          <label>地点匹配<input v-model="mapForm.locationPattern" /></label>
        </div>
        <div class="row">
          <label>文章ID<input v-model="mapForm.articleId" required /></label>
          <label>优先级<input type="number" v-model.number="mapForm.priority" /></label>
          <label><input type="checkbox" v-model="mapForm.enabled" /> 启用</label>
        </div>
        <div class="form-actions"><button type="submit">保存</button><button type="button" @click="resetMap">重置</button></div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useKbStore, setArticles, setMappings } from '../../stores/kbStore.js'
import { fetchArticles, createArticle, updateArticle, deleteArticle, fetchMappings, createMapping, updateMapping, deleteMapping } from '../../services/kb.js'

const store = useKbStore()
const keyword = ref('')
const onlyEnabled = ref(true)
const form = ref({ articleId: null, title:'', content:'', enabled:true, tags:[] })
const tagsInput = ref('')
const mapForm = ref({ ruleId:null, metricCode:'', severity:'', locationPattern:'', articleId:'', priority:0, enabled:true })

async function load() {
  const { data: arts, total: at } = await fetchArticles({ keyword: keyword.value, enabled: onlyEnabled.value })
  setArticles(arts, at)
  const { data: maps, total: mt } = await fetchMappings({ enabled: onlyEnabled.value })
  setMappings(maps, mt)
}

function editArticle(a) { form.value = { ...a, articleId: a.articleId, tags: a.tags || [] }; tagsInput.value = (a.tags||[]).join(',') }
function resetForm() { form.value = { articleId:null, title:'', content:'', enabled:true, tags:[] }; tagsInput.value = '' }
async function saveArticle() {
  const payload = { ...form.value, tags: String(tagsInput.value||'').split(',').map(s=>s.trim()).filter(Boolean) }
  if (payload.articleId) {
    await updateArticle(payload.articleId, payload)
  } else {
    await createArticle(payload)
  }
  resetForm(); await load()
}
async function removeArticle(id) { if (!confirm('确定删除该文章吗？')) return; await deleteArticle(id); await load() }

function editMapping(m) { mapForm.value = { ...m } }
function resetMap() { mapForm.value = { ruleId:null, metricCode:'', severity:'', locationPattern:'', articleId:'', priority:0, enabled:true } }
async function saveMapping() { const p = { ...mapForm.value }; if (p.ruleId) await updateMapping(p.ruleId,p); else await createMapping(p); resetMap(); await load() }

load()
</script>

<style scoped>
.kb { max-width: 1100px; margin: 0 auto; padding: 16px; text-align: left; }
.card { margin-bottom: 24px; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; background: #fff; }
.section-header { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { border: 1px solid #e5e7eb; padding: 8px; }
.empty { color: #94a3b8; text-align: center; }
.form { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
.row { display: flex; gap: 8px; }
textarea { width: 100%; min-height: 120px; }
</style>

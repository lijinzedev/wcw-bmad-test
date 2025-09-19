<template>
  <div class="roles">
    <h2>角色管理</h2>
    <div class="flex">
      <div class="list">
        <h3>角色列表</h3>
        <table>
          <thead>
          <tr><th>角色名</th><th>权限(JSON)</th><th>操作</th></tr>
          </thead>
          <tbody>
          <tr v-for="r in roles" :key="r.roleId">
            <td>{{ r.roleName }}</td>
            <td><code>{{ r.permissions }}</code></td>
            <td>
              <button @click="selectRole(r)">编辑</button>
              <button class="danger" @click="removeRole(r)">删除</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
      <div class="form">
        <h3>{{ editing ? '编辑角色' : '创建角色' }}</h3>
        <form @submit.prevent="save">
          <div>
            <label>角色名</label>
            <input v-model.trim="form.roleName" required />
          </div>
          <div>
            <label>权限(JSON)</label>
            <textarea v-model="form.permissions" placeholder="[]"></textarea>
          </div>
          <div v-if="error" class="error">{{ error }}</div>
          <div class="row">
            <button type="submit">{{ editing ? '保存' : '创建' }}</button>
            <button type="button" v-if="editing" @click="reset">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/auth'

export default {
  name: 'AdminRolesView',
  data() {
    return { roles: [], editing: false, selectedId: null, error: '', form: { roleName: '', permissions: '[]' } }
  },
  async created() {
    await this.refresh()
  },
  methods: {
    async refresh() {
      this.error = ''
      try { this.roles = await api.get('/api/v1/admin/roles') } catch (e) { this.error = e.message }
    },
    selectRole(r) {
      this.editing = true
      this.selectedId = r.roleId
      this.form.roleName = r.roleName
      this.form.permissions = r.permissions || '[]'
    },
    reset() {
      this.editing = false
      this.selectedId = null
      this.form = { roleName: '', permissions: '[]' }
    },
    async save() {
      this.error = ''
      try {
        if (this.editing) {
          await api.post(`/api/v1/admin/roles/${this.selectedId}?_method=PUT`, this.form)
        } else {
          await api.post('/api/v1/admin/roles', this.form)
        }
        await this.refresh(); this.reset()
      } catch (e) { this.error = e.message }
    },
    async removeRole(r) {
      if (!confirm(`确定删除角色 ${r.roleName}?`)) return
      try {
        await fetch(`/api/v1/admin/roles/${r.roleId}`, { method: 'DELETE', headers: this._authHeaders() })
        await this.refresh()
      } catch (e) { this.error = e.message }
    },
    _authHeaders() {
      const token = localStorage.getItem('auth_token')
      return token ? { 'Authorization': `Bearer ${token}` } : {}
    }
  }
}
</script>

<style scoped>
.flex { display: flex; gap: 24px; align-items: flex-start; }
.list { flex: 2; }
.form { flex: 1; border-left: 1px solid #ddd; padding-left: 16px; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #eee; padding: 6px 8px; text-align: left; }
.row { display: flex; gap: 8px; margin-top: 8px; }
.danger { color: #c00; }
.error { color: #c00; margin-top: 8px; }
textarea { width: 100%; min-height: 120px; }
</style>


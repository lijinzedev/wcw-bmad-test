<template>
  <div class="users">
    <h2>用户管理</h2>
    <div class="flex">
      <div class="list">
        <h3>用户列表</h3>
        <table>
          <thead>
          <tr><th>用户名</th><th>姓名</th><th>启用</th><th>角色</th><th>操作</th></tr>
          </thead>
          <tbody>
          <tr v-for="u in users" :key="u.userId">
            <td>{{ u.username }}</td>
            <td>{{ u.fullName }}</td>
            <td>
              <input type="checkbox" :checked="u.enabled" @change="toggleEnabled(u)" />
            </td>
            <td>{{ (u.roles || []).map(r=>r.roleName).join(', ') }}</td>
            <td>
              <button @click="selectUser(u)">编辑</button>
              <button class="danger" @click="removeUser(u)">删除</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
      <div class="form">
        <h3>{{ editing ? '编辑用户' : '创建用户' }}</h3>
        <form @submit.prevent="save">
          <div>
            <label>用户名</label>
            <input v-model.trim="form.username" :disabled="editing" required />
          </div>
          <div>
            <label>密码 <small v-if="editing">(留空则不修改)</small></label>
            <input type="password" v-model="form.password" :required="!editing" />
          </div>
          <div>
            <label>姓名</label>
            <input v-model.trim="form.fullName" />
          </div>
          <div>
            <label>员工编号</label>
            <input v-model.trim="form.employeeId" />
          </div>
          <div>
            <label>组织ID</label>
            <input v-model.trim="form.organizationId" required placeholder="11111111-1111-1111-1111-111111111111" />
          </div>
          <div>
            <label>启用</label>
            <input type="checkbox" v-model="form.enabled" />
          </div>
          <div>
            <label>角色</label>
            <select multiple v-model="form.roleIds">
              <option v-for="r in roles" :value="r.roleId" :key="r.roleId">{{ r.roleName }}</option>
            </select>
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
  name: 'AdminUsersView',
  data() {
    return {
      users: [],
      roles: [],
      editing: false,
      selectedId: null,
      error: '',
      form: {
        username: '', password: '', fullName: '', employeeId: '',
        organizationId: '11111111-1111-1111-1111-111111111111', enabled: true, roleIds: []
      }
    }
  },
  async created() {
    await this.refresh()
  },
  methods: {
    async refresh() {
      this.error = ''
      try {
        this.users = await api.get('/api/v1/admin/users')
        this.roles = await api.get('/api/v1/admin/roles')
      } catch (e) {
        this.error = e.message
      }
    },
    selectUser(u) {
      this.editing = true
      this.selectedId = u.userId
      this.form.username = u.username
      this.form.password = ''
      this.form.fullName = u.fullName || ''
      this.form.employeeId = u.employeeId || ''
      this.form.organizationId = u.organizationId || '11111111-1111-1111-1111-111111111111'
      this.form.enabled = !!u.enabled
      this.form.roleIds = (u.roles || []).map(r => r.roleId)
    },
    reset() {
      this.editing = false
      this.selectedId = null
      this.form = { username: '', password: '', fullName: '', employeeId: '', organizationId: '11111111-1111-1111-1111-111111111111', enabled: true, roleIds: [] }
    },
    async save() {
      this.error = ''
      try {
        if (this.editing) {
          const body = { ...this.form }
          if (!body.password) delete body.password
          await api.post(`/api/v1/admin/users/${this.selectedId}?_method=PUT`, body) // fallback if PUT blocked; backend supports PUT but fetch wrapper is simple
        } else {
          await api.post('/api/v1/admin/users', this.form)
        }
        await this.refresh(); this.reset()
      } catch (e) {
        this.error = e.message
      }
    },
    async toggleEnabled(u) {
      try {
        await api.post(`/api/v1/admin/users/${u.userId}?_method=PUT`, { enabled: !u.enabled })
        await this.refresh()
      } catch (e) {
        this.error = e.message
      }
    },
    async removeUser(u) {
      if (!confirm(`确定删除用户 ${u.username}?`)) return
      try {
        await fetch(`/api/v1/admin/users/${u.userId}`, { method: 'DELETE', headers: this._authHeaders() })
        await this.refresh()
      } catch (e) {
        this.error = e.message
      }
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
select { width: 100%; min-height: 100px; }
</style>


<template>
  <div class="login">
    <h2>登录</h2>
    <form @submit.prevent="onSubmit">
      <div>
        <label>用户名</label>
        <input v-model.trim="username" required />
      </div>
      <div>
        <label>密码</label>
        <input type="password" v-model="password" required minlength="4" />
      </div>
      <div v-if="error" class="error">{{ error }}</div>
      <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
    </form>
  </div>
  <div class="alt">
    <router-link :to="{ name: 'gov-login' }">政府监管入口</router-link>
  </div>
  </template>

<script>
import { auth } from '../services/auth'

export default {
  name: 'LoginView',
  data() {
    return { username: '', password: '', error: '', loading: false }
  },
  methods: {
    async onSubmit() {
      this.error = ''
      this.loading = true
      try {
        await auth.login(this.username, this.password)
        const redirect = this.$route.query.redirect || '/'
        this.$router.replace(redirect)
      } catch (e) {
        this.error = e.message || '登录失败'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login { max-width: 360px; margin: 40px auto; text-align: left; }
label { display: block; margin: 6px 0; }
input { width: 100%; padding: 8px; margin-bottom: 12px; }
.error { color: #c00; margin: 8px 0; }
button { width: 100%; padding: 10px; }
.alt { text-align: center; margin-top: 12px; }
</style>

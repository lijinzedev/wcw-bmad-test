<template>
  <div class="home">
    <h1>煤矿双预防系统</h1>
    <p>欢迎使用煤矿双预防管理系统</p>
    <div class="actions">
      <router-link v-if="!isAuthed" :to="{ name: 'login' }">去登录</router-link>
      <button v-else @click="doLogout">退出登录</button>
    </div>
    <div class="links" v-if="isAuthed">
      <router-link to="/">首页</router-link>
      <router-link v-if="isAdmin" to="/admin">管理后台</router-link>
      <router-link v-if="isGov" to="/gov">政府监管门户</router-link>
    </div>
  </div>
</template>

<script>
import { auth } from '../services/auth'
export default {
  name: 'HomeView',
  computed: {
    isAuthed() { return auth.isAuthenticated() },
    isAdmin() { return auth.getRoles().includes('ADMIN') },
    isGov() { return auth.getRoles().includes('GOVERNMENT') }
  },
  methods: {
    doLogout() {
      auth.logout()
      this.$router.replace({ name: 'login' })
    }
  }
}
</script>

<template>
  <view class="login-page">
    <view class="header">
      <text class="title">煤矿隐患快速上报</text>
      <text class="subtitle">请登录后开始上报现场隐患</text>
    </view>
    <view class="form">
      <input class="input" type="text" v-model="form.username" placeholder="用户名" />
      <input class="input" type="password" v-model="form.password" placeholder="密码" />
      <view class="actions">
        <button class="submit" type="primary" :loading="loading" @click="submit">登录</button>
      </view>
      <text v-if="error" class="error">{{ error }}</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { login } from '../../services/api.js'
import { setAuth, clearAuth, getToken } from '../../utils/auth.js'

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

onShow(() => {
  error.value = ''
  if (getToken()) {
    uni.redirectTo({ url: '/pages/report/report' })
  } else {
    clearAuth()
  }
})

async function submit() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await login(form.username.trim(), form.password)
    setAuth(res.token, res.roles || [])
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/report/report' })
    }, 300)
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx;
  background: linear-gradient(180deg, #0f65ca 0%, #ffffff 55%);
}
.header {
  text-align: center;
  color: #ffffff;
  margin-bottom: 60rpx;
}
.title {
  font-size: 42rpx;
  font-weight: 600;
}
.subtitle {
  margin-top: 12rpx;
  font-size: 26rpx;
  opacity: 0.9;
}
.form {
  width: 100%;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 12rpx 40rpx rgba(15, 101, 202, 0.18);
}
.input {
  width: 100%;
  margin-bottom: 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #f5f7fb;
  font-size: 28rpx;
}
.actions {
  margin-top: 10rpx;
}
.submit {
  width: 100%;
  background: #0f65ca;
  color: #fff;
  border-radius: 16rpx;
}
.error {
  display: block;
  color: #ff4d4f;
  font-size: 24rpx;
  margin-top: 24rpx;
  text-align: center;
}
</style>

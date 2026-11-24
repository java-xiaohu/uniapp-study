<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useToast } from 'wot-design-uni'
import { useAuth } from '@/composables/useAuth'

const toast = useToast()
const formRef = ref()
const canGoBack = ref(false)
const { login } = useAuth()

onLoad(() => {
  canGoBack.value = getCurrentPages().length > 1
})

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入手机号或用户名' },
    { pattern: /^1[3-9]\d{9}$|^[a-zA-Z]\w{3,15}$/, message: '请输入正确的手机号或用户名' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { pattern: /^\S{6,20}$/, message: '密码长度为 6-20 位' },
  ],
}

async function handleLogin() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  uni.showLoading({ title: '登录中', mask: true })
  try {
    await login(form.username, form.password)
    uni.hideLoading()
    toast.success('登录成功')
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index' })
    }, 600)
  } catch (error) {
    uni.hideLoading()
    toast.error((error as Error).message || '登录失败')
  }
}

function goRegister() {
  uni.navigateTo({ url: '/pages/register' })
}

function goBack() {
  uni.navigateBack()
}
</script>

<template>
  <view class="auth-page">
    <view class="hero">
      <view v-if="canGoBack" class="back" @click="goBack">
        <wd-icon name="arrow-left" size="44rpx" />
      </view>
      <view class="hero-title">欢迎回来</view>
      <view class="hero-subtitle">登录后继续探索更多内容</view>
    </view>

    <view class="card">
      <view class="card-title">账号登录</view>

      <wd-form ref="formRef" :model="form" :rules="rules">
        <view class="field">
          <wd-input
            v-model="form.username"
            prop="username"
            placeholder="请输入手机号或用户名"
            prefix-icon="user"
            clearable
            no-border
          />
        </view>
        <view class="field">
          <wd-input
            v-model="form.password"
            prop="password"
            placeholder="请输入密码"
            prefix-icon="lock-on"
            show-password
            clearable
            no-border
          />
        </view>
      </wd-form>

      <view class="actions">
        <text class="link">忘记密码？</text>
      </view>

      <wd-button type="primary" block size="large" @click="handleLogin">登录</wd-button>
      <wd-button class="register-btn" plain block size="large" @click="goRegister">注册</wd-button>
    </view>

    <view class="footer">uni-app · Vue 3 · Wot Design Uni</view>

    <wd-toast />
  </view>
</template>

<style lang="scss" scoped>
@import '../styles/auth.scss';
</style>

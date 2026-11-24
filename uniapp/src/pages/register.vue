<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useToast } from 'wot-design-uni'
import { useAuth } from '@/composables/useAuth'

const toast = useToast()
const formRef = ref()
const { register } = useAuth()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
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
  confirmPassword: [
    { required: true, message: '请再次输入密码' },
    {
      validator: (value: string) => value === form.password,
      message: '两次输入的密码不一致',
    },
  ],
}

async function handleRegister() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  uni.showLoading({ title: '注册中', mask: true })
  try {
    const loggedIn = await register(form.username, form.password)
    uni.hideLoading()
    if (loggedIn) {
      toast.success('注册成功')
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index' })
      }, 600)
    } else {
      toast.success('注册成功，请登录')
      setTimeout(() => {
        if (getCurrentPages().length > 1) {
          uni.navigateBack()
        } else {
          uni.redirectTo({ url: '/pages/login' })
        }
      }, 800)
    }
  } catch (error) {
    uni.hideLoading()
    toast.error((error as Error).message || '注册失败')
  }
}

function goLogin() {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
  } else {
    uni.redirectTo({ url: '/pages/login' })
  }
}

function goBack() {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
  } else {
    uni.redirectTo({ url: '/pages/login' })
  }
}
</script>

<template>
  <view class="auth-page">
    <view class="hero">
      <view class="back" @click="goBack">
        <wd-icon name="arrow-left" size="44rpx" />
      </view>
      <view class="hero-title">创建账号</view>
      <view class="hero-subtitle">注册后即可体验完整功能</view>
    </view>

    <view class="card">
      <view class="card-title">新用户注册</view>

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
            placeholder="请设置密码（6-20 位）"
            prefix-icon="lock-on"
            show-password
            clearable
            no-border
          />
        </view>
        <view class="field">
          <wd-input
            v-model="form.confirmPassword"
            prop="confirmPassword"
            placeholder="请再次输入密码"
            prefix-icon="lock-on"
            show-password
            clearable
            no-border
          />
        </view>
      </wd-form>

      <view class="actions">
        <text class="link">注册即表示同意用户协议</text>
      </view>

      <wd-button type="primary" block size="large" @click="handleRegister">注册</wd-button>

      <view class="switch">
        已有账号？
        <text class="link link--strong" @click="goLogin">去登录</text>
      </view>
    </view>

    <view class="footer">uni-app · Vue 3 · Wot Design Uni</view>

    <wd-toast />
  </view>
</template>

<style lang="scss" scoped>
@import '../styles/auth.scss';
</style>

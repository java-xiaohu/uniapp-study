<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { useAuth } from '@/composables/useAuth'
import { ensureLogin } from '@/utils/routeGuard'

const { state, roles, isLoggedIn, isAdmin, fetchMe, logout } = useAuth()

onShow(() => {
  if (!ensureLogin()) return
  fetchMe().catch(() => {})
})

function goLogin() {
  uni.navigateTo({ url: '/pages/login' })
}

function goRegister() {
  uni.navigateTo({ url: '/pages/register' })
}

function goUsers() {
  uni.navigateTo({ url: '/pages/users' })
}

async function handleLogout() {
  await logout()
  uni.showToast({ title: '已退出登录', icon: 'none' })
}
</script>

<template>
  <view class="page">
    <view class="title">uni-app + Vue 3 + TypeScript</view>
    <view class="subtitle">UI 组件库：Wot UI（wot-design-uni）</view>

    <view v-if="isLoggedIn" class="account">
      <view class="account-name">{{ state.user?.username || '已登录' }}</view>
      <view class="account-roles">
        角色：
        <text v-for="role in roles" :key="role" class="role-tag">{{ role }}</text>
        <text v-if="!roles.length">-</text>
      </view>
    </view>
    <view v-else class="account account--guest">
      <view class="account-name">未登录</view>
      <view class="account-tip">登录后可查看个人信息</view>
    </view>

    <view class="actions">
      <template v-if="isLoggedIn">
        <wd-button v-if="isAdmin" type="primary" block @click="goUsers">
          用户列表（管理员）
        </wd-button>
        <wd-button plain block @click="handleLogout">退出登录</wd-button>
      </template>
      <template v-else>
        <wd-button type="primary" block @click="goLogin">登录</wd-button>
        <wd-button plain block @click="goRegister">注册</wd-button>
      </template>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 48px 24px;
  text-align: center;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #222;
}

.subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}

.account {
  margin-top: 32px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(31, 41, 55, 0.05);
}

.account-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.account-roles {
  margin-top: 8px;
  font-size: 13px;
  color: #999;
}

.account-tip {
  margin-top: 6px;
  font-size: 13px;
  color: #999;
}

.role-tag {
  padding: 2px 8px;
  font-size: 12px;
  color: #ff6b00;
  background: rgba(255, 107, 0, 0.08);
  border-radius: 4px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}
</style>

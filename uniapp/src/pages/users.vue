<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useToast } from 'wot-design-uni'
import { authApi, type UserInfo } from '@/api/auth'
import { getRoleNames, useAuth } from '@/composables/useAuth'
import { ensureLogin } from '@/utils/routeGuard'

const toast = useToast()
const { isAdmin } = useAuth()

const users = ref<UserInfo[]>([])
const loading = ref(true)
const errorMessage = ref('')
const canRetry = ref(true)

async function loadUsers() {
  loading.value = true
  errorMessage.value = ''
  try {
    users.value = await authApi.users()
  } catch (error) {
    errorMessage.value = (error as Error).message || '加载失败'
    toast.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onLoad(() => {
  if (!ensureLogin()) return
  if (!isAdmin.value) {
    loading.value = false
    canRetry.value = false
    errorMessage.value = '当前账号没有管理员权限'
    return
  }
  loadUsers()
})

function getInitial(user: UserInfo): string {
  return (user.username || '?').slice(0, 1).toUpperCase()
}

function goBack() {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
  } else {
    uni.reLaunch({ url: '/pages/index' })
  }
}
</script>

<template>
  <view class="users-page">
    <view class="navbar">
      <view class="back" @click="goBack">
        <wd-icon name="arrow-left" size="40rpx" />
      </view>
      <view class="navbar-title">用户列表</view>
    </view>

    <view v-if="loading" class="state">
      <wd-loading />
      <view class="state-text">加载中...</view>
    </view>

    <view v-else-if="errorMessage" class="state">
      <view class="state-text">{{ errorMessage }}</view>
      <wd-button v-if="canRetry" plain size="small" @click="loadUsers">重新加载</wd-button>
    </view>

    <view v-else class="list">
      <view
        v-for="user in users"
        :key="String(user.id ?? user.username)"
        class="user-item"
      >
        <view class="avatar">{{ getInitial(user) }}</view>
        <view class="info">
          <view class="name">{{ user.username }}</view>
          <view class="roles">
            <text v-for="role in getRoleNames(user)" :key="role" class="role-tag">{{ role }}</text>
          </view>
        </view>
        <view class="uid">ID: {{ user.id ?? '-' }}</view>
      </view>

      <view v-if="!users.length" class="state-text">暂无数据</view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.users-page {
  box-sizing: border-box;
  min-height: 100vh;
  padding: calc(var(--status-bar-height, 0px) + 24rpx) 32rpx 48rpx;
  background: #fcfcfc;
}

.navbar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  margin-bottom: 24rpx;
}

.back {
  position: absolute;
  left: 0;
  padding: 12rpx;
  line-height: 0;
  color: #1f2937;
}

.navbar-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2937;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  padding-top: 160rpx;
}

.state-text {
  padding: 48rpx 0;
  font-size: 28rpx;
  color: #999;
  text-align: center;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 41, 55, 0.04);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80rpx;
  height: 80rpx;
  margin-right: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #ff6b00;
  background: rgba(255, 107, 0, 0.1);
  border-radius: 50%;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 30rpx;
  font-weight: 500;
  color: #1f2937;
}

.roles {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 10rpx;
}

.role-tag {
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  color: #ff6b00;
  background: rgba(255, 107, 0, 0.08);
  border-radius: 8rpx;
}

.uid {
  margin-left: 24rpx;
  font-size: 24rpx;
  color: #c0c4cc;
}
</style>

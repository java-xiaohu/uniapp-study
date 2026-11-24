import type { TokenPair, UserInfo } from '@/api/auth'

const ACCESS_TOKEN_KEY = 'auth_access_token'
const REFRESH_TOKEN_KEY = 'auth_refresh_token'
const USER_KEY = 'auth_user'

export function getAccessToken(): string {
  return uni.getStorageSync(ACCESS_TOKEN_KEY) || ''
}

export function getRefreshToken(): string {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || ''
}

export function saveTokens(tokens: TokenPair): void {
  uni.setStorageSync(ACCESS_TOKEN_KEY, tokens.accessToken)
  uni.setStorageSync(REFRESH_TOKEN_KEY, tokens.refreshToken)
}

export function getStoredUser(): UserInfo | null {
  return uni.getStorageSync(USER_KEY) || null
}

export function setStoredUser(user: UserInfo | null): void {
  if (user) {
    uni.setStorageSync(USER_KEY, user)
  } else {
    uni.removeStorageSync(USER_KEY)
  }
}

export function clearAuthStorage(): void {
  uni.removeStorageSync(ACCESS_TOKEN_KEY)
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
  uni.removeStorageSync(USER_KEY)
}

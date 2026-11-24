import { computed, reactive } from 'vue'
import { authApi, type UserInfo } from '@/api/auth'
import {
  clearAuthStorage,
  getAccessToken,
  getRefreshToken,
  getStoredUser,
  saveTokens,
  setStoredUser,
} from '@/utils/auth'

const state = reactive({
  user: getStoredUser(),
  token: getAccessToken(),
})

function extractRoleName(role: unknown): string {
  if (typeof role === 'string') return role
  if (role && typeof role === 'object') {
    const record = role as Record<string, any>
    return record.name ?? record.code ?? record.authority ?? ''
  }
  return ''
}

export function getRoleNames(user: UserInfo | null): string[] {
  if (!user) return []
  const raw = user.roles ?? user.role ?? []
  const list = Array.isArray(raw) ? raw : [raw]
  return list
    .map(extractRoleName)
    .filter(Boolean)
    .map((role) => role.replace(/^ROLE_/, '').toUpperCase())
}

export function useAuth() {
  const roles = computed(() => getRoleNames(state.user))
  const isLoggedIn = computed(() => !!state.token)
  const isAdmin = computed(() => roles.value.includes('ADMIN'))

  async function fetchMe(): Promise<UserInfo> {
    const user = await authApi.me()
    state.user = user
    setStoredUser(user)
    return user
  }

  async function login(username: string, password: string): Promise<void> {
    const tokens = await authApi.login({ username, password })
    saveTokens(tokens)
    state.token = tokens.accessToken
    await fetchMe()
  }

  async function register(username: string, password: string): Promise<boolean> {
    const result = await authApi.register({ username, password })
    if (result && 'accessToken' in result && result.accessToken && result.refreshToken) {
      saveTokens({ accessToken: result.accessToken, refreshToken: result.refreshToken })
      state.token = result.accessToken
      await fetchMe()
      return true
    }
    return false
  }

  async function logout(): Promise<void> {
    const refreshToken = getRefreshToken()
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken)
      }
    } catch {
      // 后端登出失败也要清理本地登录态
    } finally {
      clearAuthStorage()
      state.user = null
      state.token = ''
    }
  }

  return {
    state,
    roles,
    isLoggedIn,
    isAdmin,
    fetchMe,
    login,
    register,
    logout,
  }
}

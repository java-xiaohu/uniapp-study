import { request } from './request'

export interface TokenPair {
  accessToken: string
  refreshToken: string
}

export interface UserInfo {
  id?: number | string
  username: string
  roles?: Array<string | { name?: string; code?: string }> | string
  role?: string
  [key: string]: any
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
}

export const authApi = {
  login(payload: LoginPayload) {
    return request<TokenPair>({
      url: '/api/auth/login',
      method: 'POST',
      data: payload,
      auth: false,
    })
  },

  register(payload: RegisterPayload) {
    return request<Partial<TokenPair> | UserInfo | null>({
      url: '/api/auth/register',
      method: 'POST',
      data: payload,
      auth: false,
    })
  },

  logout(refreshToken: string) {
    return request<void>({
      url: '/api/auth/logout',
      method: 'POST',
      data: { refreshToken },
    })
  },

  refresh(refreshToken: string) {
    return request<TokenPair>({
      url: '/api/auth/refresh',
      method: 'POST',
      data: { refreshToken },
      auth: false,
    })
  },

  me() {
    return request<UserInfo>({
      url: '/api/auth/me',
    })
  },

  users() {
    return request<UserInfo[]>({
      url: '/api/auth/users',
    })
  },
}

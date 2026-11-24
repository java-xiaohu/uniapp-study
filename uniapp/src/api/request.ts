import { clearAuthStorage, getAccessToken, getRefreshToken, saveTokens } from '@/utils/auth'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

export interface RequestConfig {
  url: string
  method?: HttpMethod
  data?: Record<string, any>
  /** 是否携带访问令牌，默认 true；登录/注册/刷新接口应设为 false */
  auth?: boolean
}

interface RawConfig extends RequestConfig {
  retried?: boolean
}

export class ApiError extends Error {
  statusCode: number

  constructor(message: string, statusCode: number) {
    super(message)
    this.name = 'ApiError'
    this.statusCode = statusCode
  }
}

function parseBody(data: unknown): any {
  if (typeof data !== 'string') {
    return data
  }
  try {
    return JSON.parse(data)
  } catch {
    return data
  }
}

function resolveErrorMessage(statusCode: number, body: any): string {
  if (body && typeof body === 'object') {
    // Spring ProblemDetail 格式：{ title, status, detail }
    const message = body.detail ?? body.message ?? body.error ?? body.msg ?? body.title
    if (typeof message === 'string' && message) {
      return message
    }
  }
  if (statusCode === 401) return '登录状态已失效，请重新登录'
  if (statusCode === 403) return '没有权限访问该资源'
  if (statusCode === 404) return '请求的资源不存在'
  if (statusCode >= 500) return '服务器异常，请稍后重试'
  return `请求失败（${statusCode}）`
}

function rawRequest<T>(config: RawConfig): Promise<{ statusCode: number; data: T }> {
  return new Promise((resolve, reject) => {
    const header: Record<string, string> = { 'Content-Type': 'application/json' }
    if (config.auth !== false) {
      const token = getAccessToken()
      if (token) {
        header.Authorization = `Bearer ${token}`
      }
    }
    uni.request({
      url: `${BASE_URL}${config.url}`,
      method: config.method ?? 'GET',
      data: config.data,
      header,
      success: (res) => resolve({ statusCode: res.statusCode, data: res.data as T }),
      fail: (err) => reject(new ApiError(err.errMsg || '网络请求失败', 0)),
    })
  })
}

let refreshTask: Promise<boolean> | null = null

async function refreshTokens(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false

  try {
    const { statusCode, data } = await rawRequest<any>({
      url: '/api/auth/refresh',
      method: 'POST',
      data: { refreshToken },
      auth: false,
    })
    const body = parseBody(data)
    if (statusCode >= 200 && statusCode < 300 && body?.accessToken) {
      saveTokens({
        accessToken: body.accessToken,
        refreshToken: body.refreshToken ?? refreshToken,
      })
      return true
    }
  } catch {
    // refresh 失败按登录失效处理
  }
  return false
}

function redirectToLogin(): void {
  const pages = getCurrentPages()
  const currentRoute = pages[pages.length - 1]?.route ?? ''
  if (currentRoute === 'pages/login') return
  uni.reLaunch({ url: '/pages/login' })
}

export async function request<T>(config: RawConfig): Promise<T> {
  const { statusCode, data } = await rawRequest<T>(config)
  const body = parseBody(data)

  if (statusCode === 401 && !config.retried && config.auth !== false) {
    refreshTask ??= refreshTokens().finally(() => {
      refreshTask = null
    })
    const refreshed = await refreshTask
    if (refreshed) {
      return request<T>({ ...config, retried: true })
    }
    clearAuthStorage()
    redirectToLogin()
    throw new ApiError('登录状态已失效，请重新登录', 401)
  }

  if (statusCode < 200 || statusCode >= 300) {
    throw new ApiError(resolveErrorMessage(statusCode, body), statusCode)
  }

  return body as T
}

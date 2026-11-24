import { getAccessToken } from '@/utils/auth'

export const LOGIN_PAGE = '/pages/login'

const GUEST_PAGES = [LOGIN_PAGE, '/pages/register']

export function isGuestPage(url: string): boolean {
  const path = url.split('?')[0]
  return GUEST_PAGES.includes(path.startsWith('/') ? path : `/${path}`)
}

export function ensureLogin(): boolean {
  if (getAccessToken()) return true
  uni.reLaunch({ url: LOGIN_PAGE })
  return false
}

export function setupRouteGuard(): void {
  const apis = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab'] as const

  apis.forEach((api) => {
    uni.addInterceptor(api, {
      invoke(args: { url?: string }) {
        if (!args?.url || getAccessToken() || isGuestPage(args.url)) return
        uni.reLaunch({ url: LOGIN_PAGE })
        return false
      },
    })
  })
}

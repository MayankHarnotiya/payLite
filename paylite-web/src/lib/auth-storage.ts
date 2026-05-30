import type { User } from '@/types/api'

const TOKEN_KEY = 'paylite_access_token'
const USER_KEY = 'paylite_user'
const EXPIRES_KEY = 'paylite_expires_at'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function getUser(): User | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as User
  } catch {
    return null
  }
}

export function isSessionValid(): boolean {
  const token = getToken()
  const expires = localStorage.getItem(EXPIRES_KEY)
  if (!token || !expires) return false
  return new Date(expires).getTime() > Date.now()
}

export function saveSession(token: string, user: User, expiresAt: string): void {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  localStorage.setItem(EXPIRES_KEY, expiresAt)
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(EXPIRES_KEY)
}

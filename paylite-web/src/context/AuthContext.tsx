import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { login as loginApi, signup as signupApi } from '@/api/auth'
import {
  clearSession,
  getUser,
  isSessionValid,
  saveSession,
} from '@/lib/auth-storage'
import type { LoginRequest, SignupRequest, User } from '@/types/api'

interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  login: (data: LoginRequest) => Promise<void>
  signup: (data: SignupRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() =>
    isSessionValid() ? getUser() : null,
  )

  const logout = useCallback(() => {
    clearSession()
    setUser(null)
  }, [])

  const login = useCallback(async (data: LoginRequest) => {
    const auth = await loginApi(data)
    saveSession(auth.accessToken, auth.user, auth.expiresAt)
    setUser(auth.user)
  }, [])

  const signup = useCallback(async (data: SignupRequest) => {
    await signupApi(data)
    await login({ email: data.email, password: data.password })
  }, [login])

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user && isSessionValid(),
      login,
      signup,
      logout,
    }),
    [user, login, signup, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

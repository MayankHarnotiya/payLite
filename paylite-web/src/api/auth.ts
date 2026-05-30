import { apiClient } from '@/lib/api-client'
import type { AuthResponse, LoginRequest, SignupRequest, User } from '@/types/api'

export async function signup(data: SignupRequest): Promise<User> {
  const { data: user } = await apiClient.post<User>('/api/auth/signup', data)
  return user
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const { data: auth } = await apiClient.post<AuthResponse>('/api/auth/login', data)
  return auth
}

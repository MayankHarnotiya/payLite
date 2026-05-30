import axios, { type AxiosError } from 'axios'
import { ApiError, type ApiErrorBody } from '@/types/api'
import { clearSession, getToken } from '@/lib/auth-storage'

const baseURL = import.meta.env.VITE_API_URL?.replace(/\/$/, '') ?? ''

export const apiClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30_000,
})

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => {
    if (error.response?.status === 401) {
      clearSession()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login?session=expired'
      }
    }

    if (error.response?.data?.status) {
      throw new ApiError(error.response.data)
    }

    throw new ApiError({
      status: error.response?.status ?? 0,
      error: 'Network Error',
      message: error.message || 'Unable to reach the server',
    })
  },
)

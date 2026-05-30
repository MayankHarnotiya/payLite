import { ApiError } from '@/types/api'

export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong. Please try again.'
}

export function getFieldErrors(error: unknown): Record<string, string> | undefined {
  if (error instanceof ApiError) return error.fieldErrors
  return undefined
}

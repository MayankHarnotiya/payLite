export interface User {
  id: number
  email: string
  fullName: string
  phone: string | null
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresAt: string
  user: User
}

export interface SignupRequest {
  email: string
  password: string
  fullName: string
  phone?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface Wallet {
  balance: number
  currency: string
  lastUpdated: string
}

export interface TransferRequest {
  recipientEmail: string
  amount: number
}

export interface TransferResponse {
  transactionId: number
  senderEmail: string
  recipientEmail: string
  amount: number
  currency: string
  status: string
  newSenderBalance: number
  completedAt: string
}

export type TransactionType = 'SENT' | 'RECEIVED'

export interface TransactionItem {
  transactionId: number
  type: TransactionType
  counterpartyEmail: string
  counterpartyName: string
  amount: number
  currency: string
  status: string
  createdAt: string
}

export interface TransactionHistory {
  content: TransactionItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApiErrorBody {
  timestamp?: string
  status: number
  error: string
  message: string
  path?: string
  fieldErrors?: Record<string, string>
}

export class ApiError extends Error {
  status: number
  error: string
  fieldErrors?: Record<string, string>

  constructor(body: ApiErrorBody) {
    super(body.message || body.error || 'Request failed')
    this.name = 'ApiError'
    this.status = body.status
    this.error = body.error
    this.fieldErrors = body.fieldErrors
  }
}

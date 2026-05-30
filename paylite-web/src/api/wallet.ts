import { apiClient } from '@/lib/api-client'
import type {
  TransactionHistory,
  TransferRequest,
  TransferResponse,
  Wallet,
} from '@/types/api'

export async function getWallet(): Promise<Wallet> {
  const { data } = await apiClient.get<Wallet>('/api/wallets/me')
  return data
}

export async function addMoney(amount: number): Promise<Wallet> {
  const { data } = await apiClient.post<Wallet>('/api/wallets/add-money', { amount })
  return data
}

export async function transfer(
  body: TransferRequest,
  idempotencyKey: string,
): Promise<TransferResponse> {
  const { data } = await apiClient.post<TransferResponse>('/api/wallets/transfer', body, {
    headers: { 'Idempotency-Key': idempotencyKey },
  })
  return data
}

export async function getTransactions(page = 0, size = 10): Promise<TransactionHistory> {
  const { data } = await apiClient.get<TransactionHistory>('/api/wallets/me/transactions', {
    params: { page, size },
  })
  return data
}

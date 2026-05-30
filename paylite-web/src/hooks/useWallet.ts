import { useQuery } from '@tanstack/react-query'
import { getTransactions, getWallet } from '@/api/wallet'

/** Centralized query keys so invalidation stays consistent across the app. */
export const walletKeys = {
  wallet: ['wallet'] as const,
  transactions: (page: number, size: number) =>
    ['transactions', page, size] as const,
}

/**
 * Polls only while the tab is visible. Hidden tabs stop hammering the API;
 * refetchOnWindowFocus (set globally) catches the user back up on return.
 */
function visiblePolling(intervalMs: number) {
  return () => (document.visibilityState === 'visible' ? intervalMs : false)
}

export function useWallet() {
  return useQuery({
    queryKey: walletKeys.wallet,
    queryFn: getWallet,
    refetchInterval: visiblePolling(20_000),
  })
}

export function useTransactions(page: number, size: number, refetchMs?: number) {
  return useQuery({
    queryKey: walletKeys.transactions(page, size),
    queryFn: () => getTransactions(page, size),
    placeholderData: (prev) => prev, // keep last page visible while paginating
    refetchInterval: refetchMs ? visiblePolling(refetchMs) : undefined,
  })
}

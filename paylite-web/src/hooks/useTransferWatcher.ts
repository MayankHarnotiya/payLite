import { useEffect, useRef } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/context/AuthContext'
import { useNotifications } from '@/context/NotificationContext'
import { useTransactions } from '@/hooks/useWallet'
import { formatCurrency } from '@/lib/format'

const POLL_MS = 15_000

function lastSeenKey(userId: number | string): string {
  return `paylite_last_seen_tx_${userId}`
}

export function useTransferWatcher() {
  const { user } = useAuth()
  const { notify } = useNotifications()
  const { data } = useTransactions(0, 10, POLL_MS)

  const lastSeenId = useRef<number | null>(null)

  useEffect(() => {
    lastSeenId.current = null
  }, [user?.id])

  useEffect(() => {
    if (!user || !data) return
    const items = data.content
    if (items.length === 0) return

    const maxId = Math.max(...items.map((t) => t.transactionId))

    if (lastSeenId.current === null) {
      const stored = Number(localStorage.getItem(lastSeenKey(user.id)))
      lastSeenId.current = Number.isFinite(stored) && stored > 0 ? stored : maxId
    }

    const baseline = lastSeenId.current
    const fresh = items
      .filter((t) => t.transactionId > baseline && t.type === 'RECEIVED')
      .sort((a, b) => a.transactionId - b.transactionId)

    for (const txn of fresh) {
      const amount = formatCurrency(txn.amount, txn.currency)
      toast.success(`Received ${amount}`, {
        description: `From ${txn.counterpartyName}`,
      })
      notify({
        kind: 'transfer-in',
        title: `Received ${amount}`,
        body: `From ${txn.counterpartyName} (${txn.counterpartyEmail})`,
        href: '/history',
      })
    }

    if (maxId > baseline) {
      lastSeenId.current = maxId
      localStorage.setItem(lastSeenKey(user.id), String(maxId))
    }
  }, [data, user, notify])
}

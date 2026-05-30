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

/**
 * Frontend "real-time" layer.
 *
 * The backend emits Kafka `TransferCompletedEvent`s, but browsers can't read
 * Kafka directly. Until a WebSocket/SSE bridge exists on the server, we poll
 * the recent-transactions feed while the tab is visible and surface any newly
 * *received* transfer as a toast + a persisted notification.
 *
 * First load establishes a baseline (the newest known id) so we never replay
 * historical transfers as "new" notifications.
 *
 * Mount once inside the authenticated shell.
 */
export function useTransferWatcher() {
  const { user } = useAuth()
  const { notify } = useNotifications()
  const { data } = useTransactions(0, 10, POLL_MS)

  // Highest transactionId we've already accounted for. Persisted per user so a
  // page reload doesn't re-notify for transfers seen in a previous session.
  const lastSeenId = useRef<number | null>(null)

  useEffect(() => {
    lastSeenId.current = null // reset baseline when the user changes
  }, [user?.id])

  useEffect(() => {
    if (!user || !data) return
    const items = data.content
    if (items.length === 0) return

    const maxId = Math.max(...items.map((t) => t.transactionId))

    // Establish baseline on first observation (prefer a value persisted from a
    // previous session so cross-reload transfers still notify once).
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

import { ArrowDownLeft, ArrowUpRight, Inbox } from 'lucide-react'
import { Badge, transactionBadgeVariant } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { TransactionRowSkeleton } from '@/components/ui/Skeleton'
import { formatCurrency, formatRelative } from '@/lib/format'
import type { TransactionItem } from '@/types/api'
import { cn } from '@/lib/utils'

export function TransactionList({
  items,
  loading,
  compact = false,
  emptyHint = 'Add money or send a transfer to see activity here.',
  onSelect,
}: {
  items: TransactionItem[]
  loading?: boolean
  compact?: boolean
  emptyHint?: string
  onSelect?: (txn: TransactionItem) => void
}) {
  if (loading) {
    return (
      <div className="divide-y divide-slate-100 dark:divide-slate-800">
        {Array.from({ length: compact ? 3 : 6 }).map((_, i) => (
          <TransactionRowSkeleton key={i} />
        ))}
      </div>
    )
  }

  if (items.length === 0) {
    return <EmptyState icon={Inbox} title="No transactions yet" description={emptyHint} />
  }

  return (
    <ul className={cn('divide-y divide-slate-100 dark:divide-slate-800', compact && 'text-sm')}>
      {items.map((txn) => {
        const isSent = txn.type === 'SENT'
        const content = (
          <>
            <div
              className={cn(
                'flex h-11 w-11 shrink-0 items-center justify-center rounded-xl',
                isSent
                  ? 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400'
                  : 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400',
              )}
            >
              {isSent ? <ArrowUpRight className="h-5 w-5" /> : <ArrowDownLeft className="h-5 w-5" />}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium text-slate-900 dark:text-white">
                {isSent ? 'Sent to' : 'Received from'} {txn.counterpartyName}
              </p>
              <p className="truncate text-xs text-slate-500">{txn.counterpartyEmail}</p>
            </div>
            <div className="text-right">
              <p
                className={cn(
                  'font-mono font-semibold',
                  isSent ? 'text-orange-600 dark:text-orange-400' : 'text-emerald-600 dark:text-emerald-400',
                )}
              >
                {isSent ? '−' : '+'}
                {formatCurrency(txn.amount, txn.currency)}
              </p>
              <div className="mt-1 flex items-center justify-end gap-2">
                <Badge variant={transactionBadgeVariant(txn.type)}>{txn.type}</Badge>
                <span className="text-xs text-slate-400">{formatRelative(txn.createdAt)}</span>
              </div>
            </div>
          </>
        )

        return (
          <li key={txn.transactionId}>
            {onSelect ? (
              <button
                type="button"
                onClick={() => onSelect(txn)}
                className="flex w-full items-center gap-4 rounded-xl px-2 py-4 text-left transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/50"
              >
                {content}
              </button>
            ) : (
              <div className="flex items-center gap-4 py-4 first:pt-0 last:pb-0">{content}</div>
            )}
          </li>
        )
      })}
    </ul>
  )
}

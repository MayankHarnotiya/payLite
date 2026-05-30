import { ArrowDownLeft, ArrowUpRight, Copy, Send } from 'lucide-react'
import { toast } from 'sonner'
import type { TransactionItem } from '@/types/api'
import { Modal } from '@/components/ui/Modal'
import { Button } from '@/components/ui/Button'
import { Badge, transactionBadgeVariant } from '@/components/ui/Badge'
import { formatCurrency, formatDateTime } from '@/lib/format'
import { cn } from '@/lib/utils'

function Row({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between gap-4 py-2.5">
      <span className="text-sm text-slate-500">{label}</span>
      <span
        className={cn(
          'truncate text-sm font-medium text-slate-900 dark:text-white',
          mono && 'font-mono',
        )}
      >
        {value}
      </span>
    </div>
  )
}

export function TransactionDetailModal({
  txn,
  onClose,
  onSendAgain,
}: {
  txn: TransactionItem | null
  onClose: () => void
  onSendAgain?: (email: string) => void
}) {
  if (!txn) return null
  const isSent = txn.type === 'SENT'

  const copyId = () => {
    navigator.clipboard.writeText(String(txn.transactionId))
    toast.success('Transaction ID copied')
  }

  return (
    <Modal open={!!txn} onClose={onClose} title="Transaction details">
      <div className="flex flex-col items-center gap-3 pb-4 text-center">
        <div
          className={cn(
            'flex h-14 w-14 items-center justify-center rounded-2xl',
            isSent
              ? 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400'
              : 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400',
          )}
        >
          {isSent ? <ArrowUpRight className="h-7 w-7" /> : <ArrowDownLeft className="h-7 w-7" />}
        </div>
        <div>
          <p
            className={cn(
              'font-mono text-3xl font-bold',
              isSent ? 'text-orange-600 dark:text-orange-400' : 'text-emerald-600 dark:text-emerald-400',
            )}
          >
            {isSent ? '−' : '+'}
            {formatCurrency(txn.amount, txn.currency)}
          </p>
          <Badge variant={transactionBadgeVariant(txn.type)} className="mt-2">
            {txn.type}
          </Badge>
        </div>
      </div>

      <div className="divide-y divide-slate-100 border-t border-slate-100 dark:divide-slate-800 dark:border-slate-800">
        <Row label={isSent ? 'Sent to' : 'Received from'} value={txn.counterpartyName} />
        <Row label="Email" value={txn.counterpartyEmail} />
        <Row label="Status" value={txn.status} />
        <Row label="Date" value={formatDateTime(txn.createdAt)} />
        <Row label="Reference" value={`#${txn.transactionId}`} mono />
      </div>

      <div className="mt-5 flex gap-3">
        <Button variant="secondary" className="flex-1" onClick={copyId}>
          <Copy className="h-4 w-4" />
          Copy ID
        </Button>
        {onSendAgain && (
          <Button
            className="flex-1"
            onClick={() => {
              onSendAgain(txn.counterpartyEmail)
              onClose()
            }}
          >
            <Send className="h-4 w-4" />
            {isSent ? 'Send again' : 'Send back'}
          </Button>
        )}
      </div>
    </Modal>
  )
}

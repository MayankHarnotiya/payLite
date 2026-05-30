import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'
import type { TransactionType } from '@/types/api'

export function Badge({
  children,
  variant = 'default',
  className,
}: {
  children: ReactNode
  variant?: 'default' | 'success' | 'danger' | 'sent' | 'received'
  className?: string
}) {
  const styles = {
    default: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
    success: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300',
    danger: 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-300',
    sent: 'bg-orange-100 text-orange-800 dark:bg-orange-900/40 dark:text-orange-300',
    received: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300',
  }

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wide',
        styles[variant],
        className,
      )}
    >
      {children}
    </span>
  )
}

export function transactionBadgeVariant(type: TransactionType): 'sent' | 'received' {
  return type === 'SENT' ? 'sent' : 'received'
}

import type { HTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

export function Skeleton({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-lg bg-slate-200/80 dark:bg-slate-700/50',
        className,
      )}
      {...props}
    />
  )
}

export function TransactionRowSkeleton() {
  return (
    <div className="flex items-center gap-4 py-4">
      <Skeleton className="h-11 w-11 shrink-0 rounded-xl" />
      <div className="min-w-0 flex-1 space-y-2">
        <Skeleton className="h-4 w-40 max-w-[60%]" />
        <Skeleton className="h-3 w-28 max-w-[40%]" />
      </div>
      <div className="space-y-2 text-right">
        <Skeleton className="ml-auto h-4 w-20" />
        <Skeleton className="ml-auto h-3 w-12" />
      </div>
    </div>
  )
}

import type { LucideIcon } from 'lucide-react'
import { Card } from '@/components/ui/Card'
import { Skeleton } from '@/components/ui/Skeleton'
import { cn } from '@/lib/utils'

const tones = {
  emerald: 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400',
  orange: 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400',
  brand: 'bg-brand-100 text-brand-700 dark:bg-brand-900/40 dark:text-brand-300',
  slate: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
} as const

export function StatCard({
  icon: Icon,
  label,
  value,
  hint,
  tone = 'slate',
  loading,
}: {
  icon: LucideIcon
  label: string
  value: string
  hint?: string
  tone?: keyof typeof tones
  loading?: boolean
}) {
  return (
    <Card padding="md" className="flex items-center gap-4">
      <div className={cn('flex h-11 w-11 shrink-0 items-center justify-center rounded-xl', tones[tone])}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="min-w-0">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</p>
        {loading ? (
          <Skeleton className="mt-1 h-6 w-24" />
        ) : (
          <p className="truncate text-lg font-bold text-slate-900 dark:text-white">{value}</p>
        )}
        {hint && !loading && <p className="truncate text-xs text-slate-400">{hint}</p>}
      </div>
    </Card>
  )
}

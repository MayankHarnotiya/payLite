import { cn } from '@/lib/utils'

export function GradientText({
  children,
  className,
}: {
  children: React.ReactNode
  className?: string
}) {
  return (
    <span
      className={cn(
        'bg-gradient-to-r from-emerald-300 via-teal-200 to-cyan-300 bg-clip-text text-transparent',
        className,
      )}
    >
      {children}
    </span>
  )
}

export function PageHeader({
  title,
  subtitle,
  className,
}: {
  title: string
  subtitle?: string
  className?: string
}) {
  return (
    <div className={cn('space-y-1', className)}>
      <h1 className="font-display text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl dark:text-white">
        {title}
      </h1>
      {subtitle && <p className="text-slate-500 dark:text-slate-400">{subtitle}</p>}
    </div>
  )
}

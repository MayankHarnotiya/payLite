import { AlertCircle, CheckCircle2, Info } from 'lucide-react'
import { cn } from '@/lib/utils'

type AlertVariant = 'error' | 'success' | 'info'

const config: Record<AlertVariant, { icon: typeof AlertCircle; className: string }> = {
  error: {
    icon: AlertCircle,
    className: 'border-red-200 bg-red-50 text-red-900 dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-200',
  },
  success: {
    icon: CheckCircle2,
    className:
      'border-emerald-200 bg-emerald-50 text-emerald-900 dark:border-emerald-900/50 dark:bg-emerald-950/40 dark:text-emerald-200',
  },
  info: {
    icon: Info,
    className:
      'border-blue-200 bg-blue-50 text-blue-900 dark:border-blue-900/50 dark:bg-blue-950/40 dark:text-blue-200',
  },
}

export function Alert({
  variant = 'info',
  title,
  children,
  className,
}: {
  variant?: AlertVariant
  title?: string
  children: React.ReactNode
  className?: string
}) {
  const { icon: Icon, className: variantClass } = config[variant]

  return (
    <div className={cn('flex gap-3 rounded-xl border p-4 text-sm', variantClass, className)} role="alert">
      <Icon className="mt-0.5 h-5 w-5 shrink-0" aria-hidden />
      <div>
        {title && <p className="font-semibold">{title}</p>}
        <div className={title ? 'mt-1 opacity-90' : ''}>{children}</div>
      </div>
    </div>
  )
}

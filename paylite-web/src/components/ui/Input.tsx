import { forwardRef, type InputHTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  hint?: string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, error, hint, id, ...props }, ref) => {
    const inputId = id ?? props.name

    return (
      <div className="space-y-1.5">
        {label && (
          <label htmlFor={inputId} className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={cn(
            'w-full rounded-xl border bg-white px-4 py-2.5 text-slate-900 shadow-sm transition-colors',
            'placeholder:text-slate-400 focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20',
            'dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500',
            error && 'border-red-500 focus:border-red-500 focus:ring-red-500/20',
            className,
          )}
          aria-invalid={!!error}
          aria-describedby={error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined}
          {...props}
        />
        {hint && !error && (
          <p id={`${inputId}-hint`} className="text-xs text-slate-500 dark:text-slate-400">
            {hint}
          </p>
        )}
        {error && (
          <p id={`${inputId}-error`} className="text-xs text-red-600 dark:text-red-400" role="alert">
            {error}
          </p>
        )}
      </div>
    )
  },
)
Input.displayName = 'Input'

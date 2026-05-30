import type { HTMLAttributes, ReactNode } from 'react'
import { cn } from '@/lib/utils'

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
  padding?: 'none' | 'md' | 'lg'
}

export function Card({ children, className, padding = 'lg', ...props }: CardProps) {
  return (
    <div
      className={cn(
        'glass rounded-2xl',
        padding === 'md' && 'p-5',
        padding === 'lg' && 'p-6 sm:p-8',
        className,
      )}
      {...props}
    >
      {children}
    </div>
  )
}

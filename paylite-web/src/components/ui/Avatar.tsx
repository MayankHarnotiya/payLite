import { cn } from '@/lib/utils'

const palette = [
  'bg-brand-100 text-brand-700 dark:bg-brand-900/40 dark:text-brand-300',
  'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300',
  'bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-300',
  'bg-purple-100 text-purple-700 dark:bg-purple-900/40 dark:text-purple-300',
  'bg-rose-100 text-rose-700 dark:bg-rose-900/40 dark:text-rose-300',
  'bg-teal-100 text-teal-700 dark:bg-teal-900/40 dark:text-teal-300',
]

function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

export function Avatar({
  name,
  seed,
  className,
}: {
  name: string
  seed?: string
  className?: string
}) {
  const key = seed ?? name
  let hash = 0
  for (let i = 0; i < key.length; i++) hash = (hash * 31 + key.charCodeAt(i)) | 0
  const color = palette[Math.abs(hash) % palette.length]

  return (
    <span
      className={cn(
        'flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-sm font-semibold',
        color,
        className,
      )}
      aria-hidden
    >
      {initials(name)}
    </span>
  )
}

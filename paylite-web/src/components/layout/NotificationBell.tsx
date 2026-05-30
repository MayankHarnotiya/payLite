import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowDownLeft, ArrowUpRight, Bell, Check, Info, Trash2 } from 'lucide-react'
import { useNotifications, type AppNotification } from '@/context/NotificationContext'
import { formatRelative } from '@/lib/format'
import { cn } from '@/lib/utils'

const kindIcon = {
  'transfer-in': ArrowDownLeft,
  'transfer-out': ArrowUpRight,
  system: Info,
} as const

const kindColor = {
  'transfer-in': 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400',
  'transfer-out': 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400',
  system: 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400',
} as const

export function NotificationBell() {
  const { notifications, unreadCount, markAllRead, clearAll, remove } = useNotifications()
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  useEffect(() => {
    if (!open) return
    const onClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false)
    document.addEventListener('mousedown', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  const toggle = () => {
    setOpen((v) => {
      if (!v && unreadCount > 0) markAllRead()
      return !v
    })
  }

  const handleClick = (n: AppNotification) => {
    setOpen(false)
    if (n.href) navigate(n.href)
  }

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        onClick={toggle}
        aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
        aria-haspopup="true"
        aria-expanded={open}
        className="relative rounded-xl p-2 text-slate-600 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div
          role="menu"
          className="glass absolute right-0 z-50 mt-2 w-80 max-w-[90vw] overflow-hidden rounded-2xl shadow-2xl"
        >
          <div className="flex items-center justify-between border-b border-slate-200/70 px-4 py-3 dark:border-slate-700/70">
            <p className="text-sm font-semibold text-slate-900 dark:text-white">
              Notifications
            </p>
            {notifications.length > 0 && (
              <button
                type="button"
                onClick={clearAll}
                className="inline-flex items-center gap-1 text-xs font-medium text-slate-500 hover:text-red-500"
              >
                <Trash2 className="h-3.5 w-3.5" />
                Clear
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="flex flex-col items-center gap-2 px-4 py-10 text-center">
                <Check className="h-8 w-8 text-slate-300 dark:text-slate-600" />
                <p className="text-sm text-slate-500">You&apos;re all caught up</p>
              </div>
            ) : (
              <ul className="divide-y divide-slate-100 dark:divide-slate-800">
                {notifications.map((n) => {
                  const Icon = kindIcon[n.kind]
                  return (
                    <li key={n.id}>
                      <button
                        type="button"
                        onClick={() => handleClick(n)}
                        className="flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/60"
                      >
                        <span
                          className={cn(
                            'mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg',
                            kindColor[n.kind],
                          )}
                        >
                          <Icon className="h-4 w-4" />
                        </span>
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-sm font-medium text-slate-900 dark:text-white">
                            {n.title}
                          </span>
                          <span className="block truncate text-xs text-slate-500">{n.body}</span>
                          <span className="mt-0.5 block text-[11px] text-slate-400">
                            {formatRelative(n.createdAt)}
                          </span>
                        </span>
                        <span
                          role="button"
                          tabIndex={0}
                          onClick={(e) => {
                            e.stopPropagation()
                            remove(n.id)
                          }}
                          onKeyDown={(e) => {
                            if (e.key === 'Enter' || e.key === ' ') {
                              e.stopPropagation()
                              remove(n.id)
                            }
                          }}
                          aria-label="Dismiss notification"
                          className="rounded p-1 text-slate-300 hover:text-red-500"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </span>
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

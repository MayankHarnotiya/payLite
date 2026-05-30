import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { useAuth } from '@/context/AuthContext'

export interface AppNotification {
  id: string
  title: string
  body: string
  /** Visual accent + icon hint. */
  kind: 'transfer-in' | 'transfer-out' | 'system'
  createdAt: string
  read: boolean
  /** Optional deep-link target within the app. */
  href?: string
}

interface NotificationContextValue {
  notifications: AppNotification[]
  unreadCount: number
  notify: (n: Omit<AppNotification, 'id' | 'createdAt' | 'read'>) => void
  markAllRead: () => void
  remove: (id: string) => void
  clearAll: () => void
}

const NotificationContext = createContext<NotificationContextValue | null>(null)

const MAX_STORED = 50

function storageKey(userId: number | string): string {
  return `paylite_notifications_${userId}`
}

function load(key: string): AppNotification[] {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return []
    const parsed = JSON.parse(raw) as AppNotification[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export function NotificationProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const key = user ? storageKey(user.id) : null

  const [notifications, setNotifications] = useState<AppNotification[]>(() =>
    key ? load(key) : [],
  )

  // Reload (and switch) the feed when the signed-in user changes. Adjusting
  // state during render — React's recommended alternative to a setState effect.
  const prevKey = useRef(key)
  if (prevKey.current !== key) {
    prevKey.current = key
    setNotifications(key ? load(key) : [])
  }

  // Persist on every change for the current user.
  useEffect(() => {
    if (key) localStorage.setItem(key, JSON.stringify(notifications))
  }, [key, notifications])

  const notify = useCallback<NotificationContextValue['notify']>((n) => {
    setNotifications((prev) =>
      [
        {
          ...n,
          id: crypto.randomUUID(),
          createdAt: new Date().toISOString(),
          read: false,
        },
        ...prev,
      ].slice(0, MAX_STORED),
    )
  }, [])

  const markAllRead = useCallback(() => {
    setNotifications((prev) =>
      prev.some((n) => !n.read) ? prev.map((n) => ({ ...n, read: true })) : prev,
    )
  }, [])

  const remove = useCallback((id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id))
  }, [])

  const clearAll = useCallback(() => setNotifications([]), [])

  const value = useMemo<NotificationContextValue>(
    () => ({
      notifications,
      unreadCount: notifications.reduce((acc, n) => acc + (n.read ? 0 : 1), 0),
      notify,
      markAllRead,
      remove,
      clearAll,
    }),
    [notifications, notify, markAllRead, remove, clearAll],
  )

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const ctx = useContext(NotificationContext)
  if (!ctx) throw new Error('useNotifications must be used within NotificationProvider')
  return ctx
}

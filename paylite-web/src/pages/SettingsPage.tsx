import { useNavigate } from 'react-router-dom'
import { Calendar, Copy, LogOut, Mail, Moon, Phone, Sun, User as UserIcon } from 'lucide-react'
import { toast } from 'sonner'
import { useAuth } from '@/context/AuthContext'
import { useTheme } from '@/context/ThemeContext'
import { formatDateTime } from '@/lib/format'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Avatar } from '@/components/ui/Avatar'

function Field({
  icon: Icon,
  label,
  value,
  onCopy,
}: {
  icon: typeof Mail
  label: string
  value: string
  onCopy?: () => void
}) {
  return (
    <div className="flex items-center gap-3 py-3">
      <Icon className="h-4 w-4 shrink-0 text-slate-400" />
      <div className="min-w-0 flex-1">
        <p className="text-xs text-slate-500">{label}</p>
        <p className="truncate text-sm font-medium text-slate-900 dark:text-white">{value}</p>
      </div>
      {onCopy && (
        <button
          type="button"
          onClick={onCopy}
          aria-label={`Copy ${label}`}
          className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700 dark:hover:bg-slate-800"
        >
          <Copy className="h-4 w-4" />
        </button>
      )}
    </div>
  )
}

export function SettingsPage() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const copy = (value: string, label: string) => {
    navigator.clipboard.writeText(value)
    toast.success(`${label} copied`)
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl dark:text-white">Settings</h1>
        <p className="mt-1 text-slate-500">Manage your account and preferences</p>
      </div>

      <Card>
        <div className="flex items-center gap-4">
          <Avatar name={user?.fullName ?? '?'} seed={user?.email} className="h-16 w-16 text-lg" />
          <div className="min-w-0">
            <p className="truncate text-lg font-bold text-slate-900 dark:text-white">
              {user?.fullName}
            </p>
            <p className="truncate text-sm text-slate-500">{user?.email}</p>
          </div>
        </div>
      </Card>

      <Card padding="md">
        <h2 className="mb-1 text-sm font-semibold uppercase tracking-wide text-slate-500">
          Profile
        </h2>
        <div className="divide-y divide-slate-100 dark:divide-slate-800">
          <Field icon={UserIcon} label="Full name" value={user?.fullName ?? '—'} />
          <Field
            icon={Mail}
            label="Email"
            value={user?.email ?? '—'}
            onCopy={user ? () => copy(user.email, 'Email') : undefined}
          />
          <Field icon={Phone} label="Phone" value={user?.phone ?? 'Not added'} />
          {user?.createdAt && (
            <Field icon={Calendar} label="Member since" value={formatDateTime(user.createdAt)} />
          )}
        </div>
      </Card>

      <Card padding="md">
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">
          Appearance
        </h2>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            {theme === 'dark' ? (
              <Moon className="h-5 w-5 text-slate-400" />
            ) : (
              <Sun className="h-5 w-5 text-slate-400" />
            )}
            <div>
              <p className="text-sm font-medium text-slate-900 dark:text-white">Theme</p>
              <p className="text-xs text-slate-500">
                Currently {theme === 'dark' ? 'dark' : 'light'} mode
              </p>
            </div>
          </div>
          <Button variant="secondary" size="sm" onClick={toggleTheme}>
            Switch to {theme === 'dark' ? 'light' : 'dark'}
          </Button>
        </div>
      </Card>

      <Card padding="md" className="border border-red-200/60 dark:border-red-900/40">
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-red-500">
          Account
        </h2>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-slate-900 dark:text-white">Sign out</p>
            <p className="text-xs text-slate-500">End your session on this device</p>
          </div>
          <Button variant="danger" size="sm" onClick={handleLogout}>
            <LogOut className="h-4 w-4" />
            Sign out
          </Button>
        </div>
      </Card>
    </div>
  )
}

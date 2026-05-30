import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  ArrowLeftRight,
  History,
  LayoutDashboard,
  LogOut,
  Menu,
  Moon,
  Settings,
  Sun,
  Wallet,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useTheme } from '@/context/ThemeContext'
import { useTransferWatcher } from '@/hooks/useTransferWatcher'
import { cn } from '@/lib/utils'
import { Avatar } from '@/components/ui/Avatar'
import { Button } from '@/components/ui/Button'
import { NotificationBell } from '@/components/layout/NotificationBell'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/transfer', label: 'Send money', icon: ArrowLeftRight },
  { to: '/history', label: 'History', icon: History },
  { to: '/settings', label: 'Settings', icon: Settings },
]

interface SidebarProps {
  user: ReturnType<typeof useAuth>['user']
  theme: ReturnType<typeof useTheme>['theme']
  onToggleTheme: () => void
  onLogout: () => void
  onNavigate: () => void
}

function SidebarContent({ user, theme, onToggleTheme, onLogout, onNavigate }: SidebarProps) {
  return (
    <>
      <div className="mb-8 flex items-center gap-3 px-2">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 text-white shadow-lg shadow-emerald-600/30">
          <Wallet className="h-5 w-5" />
        </div>
        <div>
          <p className="font-display font-bold text-white">PayLite</p>
          <p className="text-xs text-slate-400">Digital wallet</p>
        </div>
      </div>

      <nav className="flex flex-1 flex-col gap-1">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onNavigate}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all',
                isActive
                  ? 'bg-gradient-to-r from-emerald-600/90 to-teal-600/90 text-white shadow-md shadow-emerald-900/30'
                  : 'text-slate-400 hover:bg-white/5 hover:text-white',
              )
            }
          >
            <Icon className="h-5 w-5 shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto space-y-3 border-t border-white/10 pt-4">
        <NavLink
          to="/settings"
          onClick={onNavigate}
          className="flex items-center gap-3 rounded-xl bg-white/5 px-3 py-2.5 transition-colors hover:bg-white/10"
        >
          <Avatar name={user?.fullName ?? '?'} seed={user?.email} className="h-9 w-9" />
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-white">
              {user?.fullName}
            </p>
            <p className="truncate text-xs text-slate-400">{user?.email}</p>
          </div>
        </NavLink>
        <Button variant="ghost" className="w-full justify-start" onClick={onToggleTheme}>
          {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          {theme === 'dark' ? 'Light mode' : 'Dark mode'}
        </Button>
        <Button variant="ghost" className="w-full justify-start text-red-600 dark:text-red-400" onClick={onLogout}>
          <LogOut className="h-4 w-4" />
          Sign out
        </Button>
      </div>
    </>
  )
}

export function AppShell() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)

  useTransferWatcher()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const sidebar = (
    <SidebarContent
      user={user}
      theme={theme}
      onToggleTheme={toggleTheme}
      onLogout={handleLogout}
      onNavigate={() => setMobileOpen(false)}
    />
  )

  return (
    <div className="relative min-h-screen lg:flex">
      <div className="pointer-events-none fixed inset-0 aurora-bg opacity-60" />
      <div className="pointer-events-none fixed inset-0 noise-overlay opacity-[0.025]" />

      <aside className="relative z-10 hidden w-64 shrink-0 border-r border-white/5 bg-slate-950/80 p-4 backdrop-blur-xl lg:flex lg:flex-col dark:border-white/5">
        {sidebar}
      </aside>

      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <button type="button" className="absolute inset-0 bg-slate-900/50" onClick={() => setMobileOpen(false)} />
          <aside className="relative flex h-full w-72 max-w-[85vw] flex-col bg-slate-950 p-4 shadow-2xl">
            <button
              type="button"
              className="absolute right-3 top-3 rounded-lg p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800"
              onClick={() => setMobileOpen(false)}
            >
              <X className="h-5 w-5" />
            </button>
            {sidebar}
          </aside>
        </div>
      )}

      <div className="relative z-10 flex min-h-screen flex-1 flex-col">
        <header className="sticky top-0 z-30 flex items-center justify-between gap-3 border-b border-white/5 bg-slate-950/70 px-4 py-3 backdrop-blur-xl">
          <button
            type="button"
            className="rounded-xl p-2 text-slate-700 hover:bg-slate-100 lg:hidden dark:text-slate-300 dark:hover:bg-slate-800"
            onClick={() => setMobileOpen(true)}
            aria-label="Open menu"
          >
            <Menu className="h-6 w-6" />
          </button>

          <span className="font-bold text-slate-900 lg:hidden dark:text-white">PayLite</span>

          <span className="hidden items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700 lg:inline-flex dark:border-emerald-900/50 dark:bg-emerald-950/40 dark:text-emerald-300">
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
            </span>
            Live
          </span>

          <div className="ml-auto flex items-center gap-1">
            <NotificationBell />
            <button
              type="button"
              className="rounded-xl p-2 text-slate-700 hover:bg-slate-100 lg:hidden dark:text-slate-300 dark:hover:bg-slate-800"
              onClick={toggleTheme}
              aria-label="Toggle theme"
            >
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>
          </div>
        </header>

        <main className="flex-1 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

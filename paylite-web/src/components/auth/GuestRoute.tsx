import { Navigate, Outlet } from 'react-router-dom'
import { isSessionValid } from '@/lib/auth-storage'

export function GuestRoute() {
  if (isSessionValid()) {
    return <Navigate to="/dashboard" replace />
  }
  return <Outlet />
}

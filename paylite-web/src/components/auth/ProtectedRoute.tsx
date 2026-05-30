import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { isSessionValid } from '@/lib/auth-storage'
import { useAuth } from '@/context/AuthContext'

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const valid = isAuthenticated && isSessionValid()

  if (!valid) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}

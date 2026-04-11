import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import type { UserRole } from '../types/jobs'

type Props = {
  role: UserRole
  children: React.ReactNode
}

export function RequireRole({ role, children }: Props) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return <p className="muted">Loading…</p>
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (user.role !== role) {
    return <Navigate to="/" replace />
  }
  return <>{children}</>
}

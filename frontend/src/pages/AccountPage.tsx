import { useAuth } from '../auth/useAuth'

export function AccountPage() {
  const { user, loading, logout } = useAuth()

  if (loading) return <p>Loading…</p>
  if (!user) return <p role="alert">Not signed in.</p>

  return (
    <section>
      <h1>Account</h1>
      <dl className="account-dl">
        <dt>Email</dt>
        <dd>{user.email}</dd>
        <dt>Role</dt>
        <dd>{user.role}</dd>
        <dt>Company ID</dt>
        <dd>{user.companyId ?? '—'}</dd>
      </dl>
      <button type="button" onClick={() => logout()}>
        Sign out
      </button>
    </section>
  )
}

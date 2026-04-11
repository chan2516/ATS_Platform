import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function HomePage() {
  const { user, loading } = useAuth()

  return (
    <section>
      <h1>AI-assisted interview & ATS</h1>
      <p>
        Phase 1 is live: JWT auth, roles (candidate / recruiter / admin), core database schema, and{' '}
        <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer">
          OpenAPI docs
        </a>{' '}
        when the API runs locally.
      </p>
      {!loading && !user ? (
        <p>
          <Link to="/register">Register</Link> or <Link to="/login">sign in</Link> to try the flow.
        </p>
      ) : null}
      {user ? (
        <p>
          Signed in as <strong>{user.email}</strong> ({user.role}). Open <Link to="/account">Account</Link> to see your
          profile from <code>GET /api/me</code>.
        </p>
      ) : null}
    </section>
  )
}

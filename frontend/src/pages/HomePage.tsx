import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function HomePage() {
  const { user, loading } = useAuth()

  return (
    <section>
      <h1>AI-assisted interview & ATS</h1>
      <p>
        <strong>Phase 2</strong> adds job postings, applications, recruiter tooling, and public job search. API docs:{' '}
        <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer">
          Swagger UI
        </a>{' '}
        (with the API running locally).
      </p>
      <p>
        <Link to="/jobs">Browse open jobs</Link>
        {user?.role === 'RECRUITER' ? (
          <>
            {' '}
            · <Link to="/recruiter/jobs">Manage company jobs</Link>
          </>
        ) : null}
        {user?.role === 'CANDIDATE' ? (
          <>
            {' '}
            · <Link to="/my-applications">My applications</Link>
          </>
        ) : null}
      </p>
      {!loading && !user ? (
        <p>
          <Link to="/register">Register</Link> or <Link to="/login">sign in</Link> (candidate or recruiter) to use the
          full flow.
        </p>
      ) : null}
      {user ? (
        <p>
          Signed in as <strong>{user.email}</strong> ({user.role}). <Link to="/account">Account</Link> shows{' '}
          <code>GET /api/me</code>.
        </p>
      ) : null}
    </section>
  )
}

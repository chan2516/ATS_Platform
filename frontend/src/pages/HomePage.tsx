import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function HomePage() {
  const { user, loading } = useAuth()

  return (
    <section>
      <div className="home-hero">
        <h1>Hire and apply with clarity</h1>
        <p>
          Recruiters post roles; candidates browse, apply, and upload resumes. Match scores help your team prioritize
          without extra tooling.
        </p>
        <div className="home-actions">
          <Link className="primary" to="/jobs">
            Browse open jobs
          </Link>
          {!user ? (
            <Link className="secondary" to="/register">
              Create an account
            </Link>
          ) : null}
        </div>
      </div>

      <div className="home-card">
        <p>
          <strong>Stack:</strong> Spring Boot API, PostgreSQL, Flyway, JWT auth, React + TanStack Query. See{' '}
          <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer">
            Swagger UI
          </a>{' '}
          when the API runs locally (production: use your deployed API URL).
        </p>
        <p>
          <Link to="/jobs">Open positions</Link>
          {user?.role === 'RECRUITER' ? (
            <>
              {' · '}
              <Link to="/recruiter/jobs">Company jobs</Link>
            </>
          ) : null}
          {user?.role === 'CANDIDATE' ? (
            <>
              {' · '}
              <Link to="/my-applications">My applications</Link>
            </>
          ) : null}
        </p>
        {!loading && !user ? (
          <p className="muted">
            <Link to="/register">Register</Link> or <Link to="/login">sign in</Link> as candidate or recruiter.
          </p>
        ) : null}
        {user ? (
          <p>
            Signed in as <strong>{user.email}</strong> ({user.role}).{' '}
            <Link to="/account">Account</Link> — <code>GET /api/me</code>.
          </p>
        ) : null}
      </div>
    </section>
  )
}

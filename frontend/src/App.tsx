import { Link, NavLink, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/useAuth'
import { RequireRole } from './components/RequireRole'
import { AccountPage } from './pages/AccountPage'
import { HealthPage } from './pages/HealthPage'
import { HomePage } from './pages/HomePage'
import { JobApplicationsPage } from './pages/JobApplicationsPage'
import { JobDetailPage } from './pages/JobDetailPage'
import { JobsListPage } from './pages/JobsListPage'
import { LoginPage } from './pages/LoginPage'
import { MyApplicationsPage } from './pages/MyApplicationsPage'
import { RecruiterJobFormPage } from './pages/RecruiterJobFormPage'
import { RecruiterJobsPage } from './pages/RecruiterJobsPage'
import { RegisterPage } from './pages/RegisterPage'
import './App.css'

export default function App() {
  const { user, loading } = useAuth()

  const navLink = ({ isActive }: { isActive: boolean }) => (isActive ? 'nav-link active' : 'nav-link')

  return (
    <div className="app">
      <header className="app-header">
        <Link to="/" className="brand">
          ATS Platform
        </Link>
        <nav>
          <NavLink to="/" className={navLink} end>
            Home
          </NavLink>
          <NavLink to="/jobs" className={navLink}>
            Jobs
          </NavLink>
          <NavLink to="/health" className={navLink}>
            API health
          </NavLink>
          {loading ? (
            <span className="muted">…</span>
          ) : user ? (
            <>
              {user.role === 'CANDIDATE' ? (
                <NavLink to="/my-applications" className={navLink}>
                  My applications
                </NavLink>
              ) : null}
              {user.role === 'RECRUITER' ? (
                <NavLink to="/recruiter/jobs" className={navLink}>
                  Recruiter
                </NavLink>
              ) : null}
              <NavLink to="/account" className={navLink}>
                Account
              </NavLink>
              <span className="nav-user" title={user.email}>
                {user.email}
              </span>
            </>
          ) : (
            <>
              <Link to="/login">Sign in</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/jobs" element={<JobsListPage />} />
          <Route path="/jobs/:id" element={<JobDetailPage />} />
          <Route path="/health" element={<HealthPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/account" element={<AccountPage />} />
          <Route
            path="/my-applications"
            element={
              <RequireRole role="CANDIDATE">
                <MyApplicationsPage />
              </RequireRole>
            }
          />
          <Route
            path="/recruiter/jobs/new"
            element={
              <RequireRole role="RECRUITER">
                <RecruiterJobFormPage />
              </RequireRole>
            }
          />
          <Route
            path="/recruiter/jobs/:id/edit"
            element={
              <RequireRole role="RECRUITER">
                <RecruiterJobFormPage />
              </RequireRole>
            }
          />
          <Route
            path="/recruiter/jobs/:id/applications"
            element={
              <RequireRole role="RECRUITER">
                <JobApplicationsPage />
              </RequireRole>
            }
          />
          <Route
            path="/recruiter/jobs"
            element={
              <RequireRole role="RECRUITER">
                <RecruiterJobsPage />
              </RequireRole>
            }
          />
        </Routes>
      </main>
      <footer className="app-footer">
        ATS Platform — jobs, applications & resume matching. API: Spring Boot · UI: React.
      </footer>
    </div>
  )
}

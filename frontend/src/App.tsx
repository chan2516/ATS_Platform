import { Link, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/useAuth'
import { AccountPage } from './pages/AccountPage'
import { HealthPage } from './pages/HealthPage'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import './App.css'

export default function App() {
  const { user, loading } = useAuth()

  return (
    <div className="app">
      <header className="app-header">
        <Link to="/" className="brand">
          ATS Platform
        </Link>
        <nav>
          <Link to="/">Home</Link>
          <Link to="/health">API health</Link>
          {loading ? (
            <span className="muted">…</span>
          ) : user ? (
            <>
              <Link to="/account">Account</Link>
              <span className="nav-user">{user.email}</span>
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
          <Route path="/health" element={<HealthPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/account" element={<AccountPage />} />
        </Routes>
      </main>
    </div>
  )
}

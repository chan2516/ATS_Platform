import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function RegisterPage() {
  const { register, error, clearError } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<'CANDIDATE' | 'RECRUITER'>('CANDIDATE')
  const [companyName, setCompanyName] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    clearError()
    setSubmitting(true)
    try {
      await register(
        email.trim(),
        password,
        role,
        role === 'RECRUITER' ? companyName : undefined,
      )
      navigate('/')
    } catch {
      /* error surfaced via context */
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-section">
      <h1>Create account</h1>
      <p className="muted">Self-service signup is limited to candidates and recruiters (not admin).</p>
      <form onSubmit={onSubmit} className="auth-form">
        <label>
          Email
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        <label>
          Role
          <select value={role} onChange={(e) => setRole(e.target.value as 'CANDIDATE' | 'RECRUITER')}>
            <option value="CANDIDATE">Candidate</option>
            <option value="RECRUITER">Recruiter</option>
          </select>
        </label>
        {role === 'RECRUITER' ? (
          <label>
            Company name (optional)
            <input
              type="text"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              placeholder="e.g. Acme Corp"
            />
          </label>
        ) : null}
        {error ? (
          <p className="form-error" role="alert">
            {error}
          </p>
        ) : null}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating…' : 'Register'}
        </button>
      </form>
      <p className="muted">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </section>
  )
}

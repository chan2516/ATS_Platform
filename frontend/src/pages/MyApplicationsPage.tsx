import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getMyApplications } from '../api/jobsApi'
import { useAuth } from '../auth/useAuth'

export function MyApplicationsPage() {
  const { token } = useAuth()
  const { data, isLoading, error } = useQuery({
    queryKey: ['myApplications'],
    queryFn: () => getMyApplications(token!),
    enabled: !!token,
  })

  if (!token) return <p>Not signed in.</p>
  if (isLoading) return <p>Loading…</p>
  if (error) return <p role="alert">{(error as Error).message}</p>
  if (!data) return null

  return (
    <section>
      <h1>My applications</h1>
      <ul className="job-list">
        {data.content.map((a) => (
          <li key={a.id}>
            <Link to={`/jobs/${a.jobPostingId}`}>
              <strong>{a.jobTitle}</strong>
            </Link>
            <span className="muted"> · {a.companyName}</span>
            <span className="muted"> · {a.status}</span>
          </li>
        ))}
      </ul>
      {data.content.length === 0 ? <p className="muted">You have not applied yet.</p> : null}
    </section>
  )
}

import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getRecruiterJobs } from '../api/jobsApi'
import { useAuth } from '../auth/useAuth'

export function RecruiterJobsPage() {
  const { token } = useAuth()
  const { data, isLoading, error } = useQuery({
    queryKey: ['recruiterJobs'],
    queryFn: () => getRecruiterJobs(token!),
    enabled: !!token,
  })

  if (!token) return <p>Not signed in.</p>
  if (isLoading) return <p>Loading…</p>
  if (error) return <p role="alert">{(error as Error).message}</p>
  if (!data) return null

  return (
    <section>
      <div className="page-head">
        <h1>Company jobs</h1>
        <Link className="button-link" to="/recruiter/jobs/new">
          New job
        </Link>
      </div>
      <ul className="job-list">
        {data.content.map((j) => (
          <li key={j.id}>
            <Link to={`/recruiter/jobs/${j.id}/edit`}>{j.title}</Link>
            <span className="muted"> · {j.status}</span>
            <Link className="muted" to={`/recruiter/jobs/${j.id}/applications`}>
              {' '}
              · Applications
            </Link>
          </li>
        ))}
      </ul>
      {data.content.length === 0 ? <p className="muted">No postings yet.</p> : null}
    </section>
  )
}

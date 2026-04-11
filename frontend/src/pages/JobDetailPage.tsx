import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { applyToJob, getPublicJob } from '../api/jobsApi'
import { useAuth } from '../auth/useAuth'

export function JobDetailPage() {
  const { id } = useParams<{ id: string }>()
  const jobId = Number(id)
  const { user, token } = useAuth()
  const qc = useQueryClient()

  const { data, isLoading, error } = useQuery({
    queryKey: ['publicJob', jobId],
    queryFn: () => getPublicJob(jobId),
    enabled: Number.isFinite(jobId),
  })

  const applyMut = useMutation({
    mutationFn: () => {
      if (!token) throw new Error('Sign in as a candidate to apply')
      return applyToJob(jobId, token)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['myApplications'] })
    },
  })

  if (!Number.isFinite(jobId)) return <p>Invalid job</p>
  if (isLoading) return <p>Loading…</p>
  if (error || !data) return <p role="alert">{(error as Error)?.message ?? 'Not found'}</p>

  return (
    <article>
      <h1>{data.title}</h1>
      <p className="muted">
        {data.companyName}
        {data.location ? ` · ${data.location}` : ''}
      </p>
      <pre className="job-desc">{data.description ?? 'No description.'}</pre>
      {user?.role === 'CANDIDATE' ? (
        <div className="apply-row">
          <button
            type="button"
            onClick={() => applyMut.mutate()}
            disabled={applyMut.isPending}
          >
            {applyMut.isPending ? 'Submitting…' : 'Apply'}
          </button>
          {applyMut.isError ? (
            <span className="form-error" role="alert">
              {(applyMut.error as Error).message}
            </span>
          ) : null}
          {applyMut.isSuccess ? <span className="muted">Application submitted.</span> : null}
        </div>
      ) : (
        <p className="muted">
          <Link to="/login">Sign in</Link> as a candidate to apply.
        </p>
      )}
      <p>
        <Link to="/jobs">← All jobs</Link>
      </p>
    </article>
  )
}

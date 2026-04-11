import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getMyApplications, uploadApplicationResume } from '../api/jobsApi'
import { useAuth } from '../auth/useAuth'

export function MyApplicationsPage() {
  const { token } = useAuth()
  const qc = useQueryClient()

  const { data, isLoading, error } = useQuery({
    queryKey: ['myApplications'],
    queryFn: () => getMyApplications(token!),
    enabled: !!token,
  })

  const uploadMut = useMutation({
    mutationFn: ({ applicationId, file }: { applicationId: number; file: File }) =>
      uploadApplicationResume(token!, applicationId, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['myApplications'] }),
  })

  if (!token) return <p>Not signed in.</p>
  if (isLoading) return <p>Loading…</p>
  if (error) return <p role="alert">{(error as Error).message}</p>
  if (!data) return null

  return (
    <section>
      <h1>My applications</h1>
      <p className="muted">
        Upload a PDF or DOCX resume per application. We extract text and compute a match score against the job
        description.
      </p>
      <ul className="job-list">
        {data.content.map((a) => (
          <li key={a.id}>
            <div className="app-row">
              <div>
                <Link to={`/jobs/${a.jobPostingId}`}>
                  <strong>{a.jobTitle}</strong>
                </Link>
                <span className="muted"> · {a.companyName}</span>
                <span className="muted"> · {a.status}</span>
              </div>
              <div className="app-row-meta">
                {a.resumeUploaded && a.matchScore != null ? (
                  <span className="muted" title={a.matchReasons.join('\n')}>
                    Match: <strong>{a.matchScore}</strong>/100
                  </span>
                ) : (
                  <span className="muted">No resume yet</span>
                )}
                <label className="file-upload">
                  <input
                    type="file"
                    accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    disabled={uploadMut.isPending}
                    onChange={(e) => {
                      const file = e.target.files?.[0]
                      e.target.value = ''
                      if (file) uploadMut.mutate({ applicationId: a.id, file })
                    }}
                  />
                  <span>{uploadMut.isPending ? 'Uploading…' : a.resumeUploaded ? 'Replace resume' : 'Upload resume'}</span>
                </label>
              </div>
            </div>
            {a.resumeUploaded && a.matchReasons.length > 0 ? (
              <ul className="reasons">
                {a.matchReasons.map((r) => (
                  <li key={r} className="muted small">
                    {r}
                  </li>
                ))}
              </ul>
            ) : null}
          </li>
        ))}
      </ul>
      {data.content.length === 0 ? <p className="muted">You have not applied yet.</p> : null}
      {uploadMut.isError ? (
        <p role="alert" className="error">
          {(uploadMut.error as Error).message}
        </p>
      ) : null}
    </section>
  )
}

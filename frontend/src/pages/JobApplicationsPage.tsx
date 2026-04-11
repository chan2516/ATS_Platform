import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { getJobApplications, patchApplication } from '../api/jobsApi'
import { useAuth } from '../auth/useAuth'
import type { ApplicationStatus, RecruiterApplication } from '../types/jobs'

const STATUSES: ApplicationStatus[] = [
  'SUBMITTED',
  'SCREENING',
  'INTERVIEW',
  'OFFER',
  'REJECTED',
  'WITHDRAWN',
]

export function JobApplicationsPage() {
  const { id } = useParams<{ id: string }>()
  const jobId = Number(id)
  const { token } = useAuth()
  const qc = useQueryClient()

  const { data, isLoading, error } = useQuery({
    queryKey: ['jobApplications', jobId],
    queryFn: () => getJobApplications(token!, jobId),
    enabled: !!token && Number.isFinite(jobId),
  })

  const patchMut = useMutation({
    mutationFn: ({
      applicationId,
      status,
      notes,
    }: {
      applicationId: number
      status: ApplicationStatus
      notes: string
    }) => patchApplication(token!, applicationId, { status, notes: notes || null }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['jobApplications', jobId] }),
  })

  if (!Number.isFinite(jobId)) return <p>Invalid job</p>
  if (!token) return <p>Not signed in.</p>
  if (isLoading) return <p>Loading…</p>
  if (error) return <p role="alert">{(error as Error).message}</p>
  if (!data) return null

  return (
    <section>
      <h1>Applications</h1>
      <p>
        <Link to="/recruiter/jobs">← Jobs</Link>
      </p>
      <table className="data-table">
        <thead>
          <tr>
            <th>Candidate</th>
            <th>Match</th>
            <th>Resume</th>
            <th>Status</th>
            <th>Notes</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {data.content.map((a: RecruiterApplication) => (
            <ApplicationRow
              key={`${a.id}-${a.status}-${a.notes ?? ''}-${a.matchScore ?? 'x'}-${a.resumeUploaded ? '1' : '0'}`}
              a={a}
              onSave={(status, notes) =>
                patchMut.mutate({ applicationId: a.id, status, notes })
              }
              saving={patchMut.isPending}
            />
          ))}
        </tbody>
      </table>
      {data.content.length === 0 ? <p className="muted">No applications yet.</p> : null}
    </section>
  )
}

function ApplicationRow({
  a,
  onSave,
  saving,
}: {
  a: RecruiterApplication
  onSave: (status: ApplicationStatus, notes: string) => void
  saving: boolean
}) {
  const [status, setStatus] = useState(a.status)
  const [notes, setNotes] = useState(a.notes ?? '')
  return (
    <tr>
      <td>{a.candidateEmail}</td>
      <td>
        {a.matchScore != null ? (
          <span title={a.matchReasons.join('\n')}>
            <strong>{a.matchScore}</strong>
            <span className="muted">/100</span>
          </span>
        ) : (
          <span className="muted">—</span>
        )}
      </td>
      <td className="small muted">
        {a.resumeUploaded ? (
          <>
            {a.resumeFileName ?? 'file'}
            {a.resumeUploadedAt ? (
              <>
                <br />
                {new Date(a.resumeUploadedAt).toLocaleString()}
              </>
            ) : null}
          </>
        ) : (
          '—'
        )}
      </td>
      <td>
        <select value={status} onChange={(e) => setStatus(e.target.value as ApplicationStatus)}>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </td>
      <td>
        <input
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="Internal notes"
        />
      </td>
      <td>
        <button type="button" disabled={saving} onClick={() => onSave(status, notes)}>
          Save
        </button>
      </td>
    </tr>
  )
}

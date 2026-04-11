import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getPublicJobs } from '../api/jobsApi'
import { useState } from 'react'

export function JobsListPage() {
  const [q, setQ] = useState('')
  const [location, setLocation] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['publicJobs', q, location, page],
    queryFn: () => getPublicJobs({ q: q || undefined, location: location || undefined, page, size: 10 }),
  })

  return (
    <section>
      <h1>Open positions</h1>
      <p className="muted">Browse jobs without signing in. Apply with a candidate account.</p>
      <form
        className="filters"
        onSubmit={(e) => {
          e.preventDefault()
          setPage(0)
        }}
      >
        <input
          placeholder="Search title or description"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          aria-label="Search"
        />
        <input
          placeholder="Location"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          aria-label="Location"
        />
        <button type="submit">Search</button>
      </form>
      {isLoading ? <p>Loading…</p> : null}
      {isError ? <p role="alert">{(error as Error).message}</p> : null}
      {data ? (
        <>
          <ul className="job-list">
            {data.content.map((j) => (
              <li key={j.id}>
                <Link to={`/jobs/${j.id}`}>
                  <strong>{j.title}</strong>
                </Link>
                <span className="muted"> · {j.companyName}</span>
                {j.location ? <span className="muted"> · {j.location}</span> : null}
              </li>
            ))}
          </ul>
          <div className="pager">
            <button type="button" disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>
              Previous
            </button>
            <span className="muted">
              Page {data.number + 1} of {Math.max(1, data.totalPages)} ({data.totalElements} jobs)
            </span>
            <button
              type="button"
              disabled={page >= data.totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        </>
      ) : null}
    </section>
  )
}

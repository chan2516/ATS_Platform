import { useEffect, useState } from 'react'

type ActuatorHealth = {
  status?: string
}

function healthUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''
  return base ? `${base}/actuator/health` : '/actuator/health'
}

export function HealthPage() {
  const [data, setData] = useState<ActuatorHealth | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const res = await fetch(healthUrl())
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const json = (await res.json()) as ActuatorHealth
        if (!cancelled) setData(json)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Request failed')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  if (loading) return <p>Loading health…</p>
  if (error) return <p role="alert">Could not reach API: {error}</p>
  return (
    <section>
      <h1>Backend health</h1>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </section>
  )
}

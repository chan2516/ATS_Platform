/** API path prefix: empty uses Vite dev proxy to the backend. */
export function apiUrl(path: string): string {
  const base = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''
  const p = path.startsWith('/') ? path : `/${path}`
  return base ? `${base}${p}` : p
}

export async function fetchJson<T>(
  path: string,
  options: RequestInit & { token?: string | null } = {},
): Promise<T> {
  const { token, headers: initHeaders, ...rest } = options
  const headers = new Headers(initHeaders)
  if (!headers.has('Content-Type') && rest.body && !(rest.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const res = await fetch(apiUrl(path), { ...rest, headers })
  if (!res.ok) {
    let detail = res.statusText
    try {
      const err = (await res.json()) as { message?: string }
      if (err.message) detail = err.message
    } catch {
      /* ignore */
    }
    throw new Error(detail || `HTTP ${res.status}`)
  }
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

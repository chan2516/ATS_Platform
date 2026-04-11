import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { fetchJson } from '../api/client'
import { AuthContext } from './context'
import type { AuthContextValue, AuthResponse, UserMe } from './types'

const STORAGE_KEY = 'ats_access_token'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(STORAGE_KEY))
  const [user, setUser] = useState<UserMe | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadMe = useCallback(async (t: string) => {
    const me = await fetchJson<UserMe>('/api/me', { token: t })
    setUser(me)
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      if (!token) {
        setUser(null)
        setLoading(false)
        return
      }
      try {
        await loadMe(token)
      } catch {
        if (!cancelled) {
          localStorage.removeItem(STORAGE_KEY)
          setToken(null)
          setUser(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token, loadMe])

  const login = useCallback(
    async (email: string, password: string) => {
      setError(null)
      try {
        const body = JSON.stringify({ email, password })
        const res = await fetchJson<AuthResponse>('/api/auth/login', { method: 'POST', body })
        localStorage.setItem(STORAGE_KEY, res.accessToken)
        setToken(res.accessToken)
        await loadMe(res.accessToken)
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Login failed'
        setError(msg)
        throw e
      }
    },
    [loadMe],
  )

  const register = useCallback(
    async (
      email: string,
      password: string,
      role: 'CANDIDATE' | 'RECRUITER',
      companyName: string | undefined,
    ) => {
      setError(null)
      try {
        const payload: Record<string, unknown> = { email, password, role }
        if (companyName?.trim()) payload.companyName = companyName.trim()
        const body = JSON.stringify(payload)
        const res = await fetchJson<AuthResponse>('/api/auth/register', { method: 'POST', body })
        localStorage.setItem(STORAGE_KEY, res.accessToken)
        setToken(res.accessToken)
        await loadMe(res.accessToken)
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Registration failed'
        setError(msg)
        throw e
      }
    },
    [loadMe],
  )

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const clearError = useCallback(() => setError(null), [])

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      loading,
      error,
      login,
      register,
      logout,
      clearError,
    }),
    [token, user, loading, error, login, register, logout, clearError],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

import { fetchJson } from './client'
import { normalizeSpringPage } from '../lib/springPage'
import type { JobPosting, MyApplication, Page, RecruiterApplication } from '../types/jobs'

export async function getPublicJobs(params: {
  q?: string
  location?: string
  page?: number
  size?: number
}): Promise<Page<JobPosting>> {
  const sp = new URLSearchParams()
  if (params.q) sp.set('q', params.q)
  if (params.location) sp.set('location', params.location)
  if (params.page != null) sp.set('page', String(params.page))
  if (params.size != null) sp.set('size', String(params.size))
  const q = sp.toString()
  const raw = await fetchJson<unknown>(`/api/jobs${q ? `?${q}` : ''}`)
  return normalizeSpringPage<JobPosting>(raw)
}

export function getPublicJob(id: number): Promise<JobPosting> {
  return fetchJson<JobPosting>(`/api/jobs/${id}`)
}

export function applyToJob(jobId: number, token: string): Promise<MyApplication> {
  return fetchJson<MyApplication>(`/api/jobs/${jobId}/applications`, { method: 'POST', token })
}

export async function getMyApplications(
  token: string,
  page = 0,
  size = 20,
): Promise<Page<MyApplication>> {
  const raw = await fetchJson<unknown>(`/api/me/applications?page=${page}&size=${size}`, {
    token,
  })
  return normalizeSpringPage<MyApplication>(raw)
}

export async function getRecruiterJobs(
  token: string,
  page = 0,
  size = 20,
): Promise<Page<JobPosting>> {
  const raw = await fetchJson<unknown>(`/api/recruiter/jobs?page=${page}&size=${size}`, { token })
  return normalizeSpringPage<JobPosting>(raw)
}

export function getRecruiterJob(token: string, id: number): Promise<JobPosting> {
  return fetchJson<JobPosting>(`/api/recruiter/jobs/${id}`, { token })
}

export function createRecruiterJob(token: string, body: unknown): Promise<JobPosting> {
  return fetchJson<JobPosting>('/api/recruiter/jobs', {
    method: 'POST',
    body: JSON.stringify(body),
    token,
  })
}

export function updateRecruiterJob(token: string, id: number, body: unknown): Promise<JobPosting> {
  return fetchJson<JobPosting>(`/api/recruiter/jobs/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
    token,
  })
}

export function deleteRecruiterJob(token: string, id: number): Promise<void> {
  return fetchJson<void>(`/api/recruiter/jobs/${id}`, { method: 'DELETE', token })
}

export async function getJobApplications(
  token: string,
  jobId: number,
  page = 0,
  size = 20,
): Promise<Page<RecruiterApplication>> {
  const raw = await fetchJson<unknown>(
    `/api/recruiter/jobs/${jobId}/applications?page=${page}&size=${size}`,
    { token },
  )
  return normalizeSpringPage<RecruiterApplication>(raw)
}

export function uploadApplicationResume(
  token: string,
  applicationId: number,
  file: File,
): Promise<MyApplication> {
  const body = new FormData()
  body.append('file', file)
  return fetchJson<MyApplication>(`/api/me/applications/${applicationId}/resume`, {
    method: 'POST',
    token,
    body,
  })
}

export function patchApplication(
  token: string,
  applicationId: number,
  body: { status: string; notes?: string | null },
): Promise<RecruiterApplication> {
  return fetchJson<RecruiterApplication>(`/api/recruiter/applications/${applicationId}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
    token,
  })
}

import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useMatch, useNavigate, useParams } from 'react-router-dom'
import { z } from 'zod'
import {
  createRecruiterJob,
  getRecruiterJob,
  updateRecruiterJob,
} from '../api/jobsApi'
import { useAuth } from '../auth/useAuth'
import type { EmploymentType, JobPosting, JobPostingStatus } from '../types/jobs'

const empEnum = z.enum(['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP'])

const schema = z
  .object({
    title: z.string().min(1).max(500),
    description: z.string().max(20000).optional(),
    location: z.string().max(255).optional(),
    employmentType: z
      .union([empEnum, z.literal('')])
      .optional()
      .transform((v) => (v === '' ? null : v)),
    salaryMin: z.coerce.number().int().positive().optional().nullable(),
    salaryMax: z.coerce.number().int().positive().optional().nullable(),
    status: z.enum(['OPEN', 'CLOSED']),
  })
  .refine(
    (d) =>
      d.salaryMin == null || d.salaryMax == null || (d.salaryMin != null && d.salaryMax >= d.salaryMin),
    { message: 'salaryMax must be >= salaryMin', path: ['salaryMax'] },
  )

type FormValues = z.input<typeof schema>
type FormOutput = z.output<typeof schema>

function toApiPayload(values: FormOutput) {
  return {
    title: values.title,
    description: values.description || null,
    location: values.location || null,
    employmentType: values.employmentType as EmploymentType | null,
    salaryMin: values.salaryMin ?? null,
    salaryMax: values.salaryMax ?? null,
    status: values.status as JobPostingStatus,
  }
}

function mapJobToForm(j: JobPosting): FormValues {
  return {
    title: j.title,
    description: j.description ?? '',
    location: j.location ?? '',
    employmentType: j.employmentType ?? '',
    salaryMin: j.salaryMin,
    salaryMax: j.salaryMax,
    status: j.status,
  }
}

const emptyForm: FormValues = {
  title: '',
  description: '',
  location: '',
  employmentType: '',
  salaryMin: null,
  salaryMax: null,
  status: 'OPEN',
}

export function RecruiterJobFormPage() {
  const isNew = !!useMatch('/recruiter/jobs/new')
  const { id } = useParams<{ id: string }>()
  const jobId = isNew ? NaN : Number(id)
  const { token } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const { data: existing, isLoading } = useQuery({
    queryKey: ['recruiterJob', jobId],
    queryFn: () => getRecruiterJob(token!, jobId),
    enabled: !!token && !isNew && Number.isFinite(jobId),
  })

  const form = useForm<FormValues, unknown, FormOutput>({
    resolver: zodResolver(schema),
    defaultValues: emptyForm,
  })

  useEffect(() => {
    if (existing) {
      form.reset(mapJobToForm(existing))
    }
  }, [existing, form])

  const saveMut = useMutation({
    mutationFn: (values: FormOutput) => {
      const body = toApiPayload(values)
      if (isNew) return createRecruiterJob(token!, body)
      return updateRecruiterJob(token!, jobId, body)
    },
    onSuccess: (job) => {
      qc.invalidateQueries({ queryKey: ['recruiterJobs'] })
      if (isNew) {
        navigate(`/recruiter/jobs/${job.id}/edit`, { replace: true })
      }
    },
  })

  if (!token) return <p>Not signed in.</p>
  if (!isNew && !Number.isFinite(jobId)) return <p>Invalid job</p>
  if (!isNew && isLoading) return <p>Loading…</p>
  if (!isNew && !existing) return <p>Job not found.</p>

  return (
    <section>
      <h1>{isNew ? 'Create job' : 'Edit job'}</h1>
      <form
        className="auth-form wide"
        onSubmit={form.handleSubmit((v) => saveMut.mutate(v))}
      >
        <label>
          Title *
          <input {...form.register('title')} />
        </label>
        {form.formState.errors.title ? (
          <p className="form-error">{form.formState.errors.title.message}</p>
        ) : null}
        <label>
          Description
          <textarea rows={6} {...form.register('description')} />
        </label>
        <label>
          Location
          <input {...form.register('location')} />
        </label>
        <label>
          Employment type
          <select {...form.register('employmentType')}>
            <option value="">—</option>
            <option value="FULL_TIME">Full time</option>
            <option value="PART_TIME">Part time</option>
            <option value="CONTRACT">Contract</option>
            <option value="INTERNSHIP">Internship</option>
          </select>
        </label>
        <div className="salary-row">
          <label>
            Salary min
            <input type="number" {...form.register('salaryMin')} />
          </label>
          <label>
            Salary max
            <input type="number" {...form.register('salaryMax')} />
          </label>
        </div>
        {form.formState.errors.salaryMax ? (
          <p className="form-error">{form.formState.errors.salaryMax.message}</p>
        ) : null}
        <label>
          Status
          <select {...form.register('status')}>
            <option value="OPEN">Open</option>
            <option value="CLOSED">Closed</option>
          </select>
        </label>
        {saveMut.isError ? (
          <p className="form-error" role="alert">
            {(saveMut.error as Error).message}
          </p>
        ) : null}
        <button type="submit" disabled={saveMut.isPending}>
          {saveMut.isPending ? 'Saving…' : 'Save'}
        </button>
      </form>
      <p>
        <Link to="/recruiter/jobs">← Back to jobs</Link>
      </p>
    </section>
  )
}

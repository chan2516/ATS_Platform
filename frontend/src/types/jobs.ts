export type UserRole = 'CANDIDATE' | 'RECRUITER' | 'ADMIN'

export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP'

export type JobPostingStatus = 'OPEN' | 'CLOSED'

export type ApplicationStatus =
  | 'SUBMITTED'
  | 'SCREENING'
  | 'INTERVIEW'
  | 'OFFER'
  | 'REJECTED'
  | 'WITHDRAWN'

export type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export type JobPosting = {
  id: number
  companyId: number
  companyName: string
  title: string
  description: string | null
  location: string | null
  employmentType: EmploymentType | null
  salaryMin: number | null
  salaryMax: number | null
  status: JobPostingStatus
  createdAt: string
}

export type MyApplication = {
  id: number
  jobPostingId: number
  jobTitle: string
  companyName: string
  status: ApplicationStatus
  createdAt: string
}

export type RecruiterApplication = {
  id: number
  jobPostingId: number
  jobTitle: string
  candidateId: number
  candidateEmail: string
  status: ApplicationStatus
  notes: string | null
  createdAt: string
}

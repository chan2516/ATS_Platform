export type UserRole = 'CANDIDATE' | 'RECRUITER' | 'ADMIN'

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
}

export type UserMe = {
  id: number
  email: string
  role: UserRole
  companyId: number | null
}

export type AuthContextValue = {
  token: string | null
  user: UserMe | null
  loading: boolean
  error: string | null
  login: (email: string, password: string) => Promise<void>
  register: (
    email: string,
    password: string,
    role: 'CANDIDATE' | 'RECRUITER',
    companyName: string | undefined,
  ) => Promise<void>
  logout: () => void
  clearError: () => void
}

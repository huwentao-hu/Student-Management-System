export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT'

export type Session = {
  token: string
  expiresAt: string
  userId: number
  username: string
  role: Role
  studentId: number | null
}

export type StudentStatus = 'ACTIVE' | 'SUSPENDED' | 'GRADUATED' | 'WITHDRAWN'

export type Student = {
  id: number
  studentNumber: string
  name: string
  gender: string | null
  dateOfBirth: string | null
  phone: string | null
  email: string | null
  enrollmentDate: string | null
  status: StudentStatus
  createdAt: string
  updatedAt: string
}

export type StudentFormData = {
  name: string
  gender: string
  dateOfBirth: string
  phone: string
  email: string
  enrollmentDate: string
}

export type UpdateStudentData = StudentFormData & {
  status: StudentStatus
}

export type Page<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

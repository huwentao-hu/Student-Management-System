import type { ClassAssignment, CreateSchoolClassData, Page, SchoolClass, Session, Student, StudentFormData, StudentStatus, TeacherAccount, UpdateStudentData } from './types'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')
const SESSION_KEY = 'student-management-session'

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

export function loadSession(): Session | null {
  const stored = localStorage.getItem(SESSION_KEY)
  if (!stored) return null

  try {
    const session = JSON.parse(stored) as Session
    if (new Date(session.expiresAt).getTime() <= Date.now()) {
      clearSession()
      return null
    }
    return session
  } catch {
    clearSession()
    return null
  }
}

export function saveSession(session: Session) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY)
}

async function request<T>(path: string, init: RequestInit = {}, session?: Session | null): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  if (session) headers.set('Authorization', `Bearer ${session.token}`)

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers })
  if (response.status === 401) clearSession()
  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null
    throw new ApiError(response.status, body?.message ?? `请求失败（${response.status}）`)
  }
  return response.status === 204 ? undefined as T : response.json() as Promise<T>
}

export const api = {
  login(username: string, password: string) {
    return request<Session>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
  },
  logout(session: Session) {
    return request<void>('/api/auth/logout', { method: 'POST' }, session)
  },
  students(session: Session, keyword: string, status: StudentStatus | '') {
    const params = new URLSearchParams({ page: '0', size: '50' })
    if (keyword) params.set('keyword', keyword)
    if (status) params.set('status', status)
    return request<Page<Student>>(`/api/students?${params}`, {}, session)
  },
  student(session: Session, id: string) {
    return request<Student>(`/api/students/${id}`, {}, session)
  },
  createStudent(session: Session, data: StudentFormData) {
    return request<Student>('/api/students', {
      method: 'POST',
      body: JSON.stringify(normalizeStudentData(data)),
    }, session)
  },
  updateStudent(session: Session, id: string, data: UpdateStudentData) {
    return request<Student>(`/api/students/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ ...normalizeStudentData(data), status: data.status }),
    }, session)
  },
  classes(session: Session, keyword = '', entryYear = '') {
    const params = new URLSearchParams({ page: '0', size: '100' })
    if (keyword) params.set('keyword', keyword)
    if (entryYear) params.set('entryYear', entryYear)
    return request<Page<SchoolClass>>(`/api/classes?${params}`, {}, session)
  },
  schoolClass(session: Session, id: string) {
    return request<SchoolClass>(`/api/classes/${id}`, {}, session)
  },
  createSchoolClass(session: Session, data: CreateSchoolClassData) {
    return request<SchoolClass>('/api/classes', {
      method: 'POST',
      body: JSON.stringify({ ...data, name: data.name.trim() }),
    }, session)
  },
  teachers(session: Session) {
    return request<TeacherAccount[]>('/api/accounts/teachers', {}, session)
  },
  classStudents(session: Session, classId: string) {
    return request<Student[]>(`/api/classes/${classId}/students`, {}, session)
  },
  classAssignments(session: Session, studentId: number) {
    return request<ClassAssignment[]>(`/api/students/${studentId}/class-assignments`, {}, session)
  },
  assignClass(session: Session, studentId: number, classId: number, effectiveDate: string) {
    return request<ClassAssignment>(`/api/students/${studentId}/class-assignments`, {
      method: 'POST',
      body: JSON.stringify({ classId, effectiveDate }),
    }, session)
  },
  leaveClass(session: Session, studentId: number, effectiveDate: string) {
    return request<ClassAssignment>(`/api/students/${studentId}/class-assignments/leave`, {
      method: 'POST',
      body: JSON.stringify({ effectiveDate }),
    }, session)
  },
  pageCount(session: Session, resource: string) {
    return request<Page<unknown>>(`/api/${resource}?page=0&size=1`, {}, session)
  },
}

function normalizeStudentData(data: StudentFormData) {
  return {
    name: data.name.trim(),
    gender: data.gender.trim() || null,
    dateOfBirth: data.dateOfBirth || null,
    phone: data.phone.trim() || null,
    email: data.email.trim() || null,
    enrollmentDate: data.enrollmentDate || null,
  }
}

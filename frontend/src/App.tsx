import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react'
import {
  ArrowLeft, BookOpen, CalendarDays, CheckCircle2, ChevronRight, ClipboardCheck, Edit3,
  GraduationCap, History, Layers3, LayoutDashboard, LogOut, Mail, Menu, Phone, Plus, Save, School,
  Search, Settings, TrendingUp, UserMinus, UserPlus, UserRound, Users, X,
} from 'lucide-react'
import { BrowserRouter, Link, NavLink, Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { api, ApiError, clearSession, loadSession, saveSession } from './api'
import type { ClassAssignment, Course, CourseOffering, CourseStatus, Role, SchoolClass, Semester, Session, Student, StudentFormData, StudentStatus, TeacherAccount, UpdateStudentData } from './types'
import './App.css'

const roleLabels: Record<Role, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
const statusLabels: Record<StudentStatus, string> = {
  ACTIVE: '在读', SUSPENDED: '停学', GRADUATED: '毕业', WITHDRAWN: '退学',
}
const courseStatusLabels: Record<CourseStatus, string> = { ACTIVE: '启用', INACTIVE: '停用' }
const semesterLabels: Record<Semester, string> = { FIRST: '第一学期', SECOND: '第二学期' }

const navItems = [
  { path: '/', label: '工作台', icon: LayoutDashboard, roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/students', label: '学生管理', icon: Users, roles: ['ADMIN', 'TEACHER'] },
  { path: '/classes', label: '班级管理', icon: School, roles: ['ADMIN', 'TEACHER'] },
  { path: '/courses', label: '课程管理', icon: BookOpen, roles: ['ADMIN', 'TEACHER'] },
  { path: '/timetable', label: '课程表', icon: CalendarDays, roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/grades', label: '成绩管理', icon: TrendingUp, roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/attendance', label: '考勤管理', icon: ClipboardCheck, roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/settings', label: '系统设置', icon: Settings, roles: ['ADMIN'] },
] as const

function LoginPage({ onLogin }: { onLogin: (session: Session) => void }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const session = await api.login(username, password)
      saveSession(session)
      onLogin(session)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : '无法连接到后端服务，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return <main className="login-page">
    <section className="login-intro">
      <div className="brand-mark"><GraduationCap size={30} /></div>
      <p className="eyebrow">STUDENT MANAGEMENT SYSTEM</p>
      <h1>让每一份学生信息，<br />清晰、有序、可追溯。</h1>
      <p className="login-copy">统一管理学生档案、班级课程、成绩与考勤，让日常教学管理更从容。</p>
      <div className="intro-features">
        <span><CheckCircle2 size={18} /> 分角色数据权限</span>
        <span><CheckCircle2 size={18} /> 完整历史记录</span>
        <span><CheckCircle2 size={18} /> 实时统计分析</span>
      </div>
    </section>
    <section className="login-panel">
      <form className="login-card" onSubmit={submit}>
        <div className="mobile-brand"><GraduationCap size={24} /> 教务管理系统</div>
        <p className="eyebrow">欢迎回来</p>
        <h2>登录管理平台</h2>
        <p className="muted">使用学校分配的账号继续</p>
        <label>用户名<input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="请输入用户名" autoFocus required /></label>
        <label>密码<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="请输入密码" required /></label>
        {error && <p className="form-error">{error}</p>}
        <button className="primary-button" disabled={loading}>{loading ? '正在登录...' : '登录系统'}<ChevronRight size={18} /></button>
      </form>
    </section>
  </main>
}

function AppShell({ session, onLogout, children }: { session: Session, onLogout: () => void, children: ReactNode }) {
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()
  const items = navItems.filter((item) => (item.roles as readonly Role[]).includes(session.role))

  async function logout() {
    try { await api.logout(session) } catch { /* Local logout still succeeds. */ }
    clearSession()
    onLogout()
    navigate('/')
  }

  return <div className="app-shell">
    <aside className={open ? 'sidebar open' : 'sidebar'}>
      <div className="sidebar-brand"><span><GraduationCap size={25} /></span><div><strong>教务管理</strong><small>Student Hub</small></div><button className="icon-button close-nav" onClick={() => setOpen(false)}><X /></button></div>
      <nav>{items.map(({ path, label, icon: Icon }) => <NavLink key={path} to={path} onClick={() => setOpen(false)} className={({ isActive }) => isActive ? 'active' : ''}><Icon size={19} />{label}</NavLink>)}</nav>
      <div className="sidebar-user"><div className="avatar">{session.username.slice(0, 1).toUpperCase()}</div><div><strong>{session.username}</strong><small>{roleLabels[session.role]}</small></div><button className="icon-button" title="退出登录" onClick={logout}><LogOut size={18} /></button></div>
    </aside>
    {open && <button className="nav-overlay" onClick={() => setOpen(false)} />}
    <div className="main-column">
      <header className="topbar"><button className="icon-button menu-button" onClick={() => setOpen(true)}><Menu /></button><div><p className="eyebrow">一所学校 · 一个清晰视图</p><strong>学生管理系统</strong></div><div className="role-pill">{roleLabels[session.role]}</div></header>
      <main className="content">{children}</main>
    </div>
  </div>
}

function Dashboard({ session }: { session: Session }) {
  const [counts, setCounts] = useState({ students: '—', classes: '—', courses: '—' })

  useEffect(() => {
    if (session.role === 'STUDENT') return
    Promise.all(['students', 'classes', 'courses'].map((resource) => api.pageCount(session, resource)))
      .then(([students, classes, courses]) => setCounts({
        students: String(students.totalElements), classes: String(classes.totalElements), courses: String(courses.totalElements),
      })).catch(() => undefined)
  }, [session])

  return <>
    <section className="page-heading"><div><p className="eyebrow">工作台</p><h1>你好，{session.username}</h1><p className="muted">这里汇总了当前学校的核心教学管理信息。</p></div><span className="date-chip">{new Intl.DateTimeFormat('zh-CN', { dateStyle: 'long' }).format(new Date())}</span></section>
    <section className="metric-grid">
      <Metric icon={<Users />} label="学生总数" value={session.role === 'STUDENT' ? '个人中心' : counts.students} tone="blue" />
      <Metric icon={<School />} label="班级总数" value={session.role === 'STUDENT' ? '我的班级' : counts.classes} tone="violet" />
      <Metric icon={<BookOpen />} label="课程总数" value={session.role === 'STUDENT' ? '我的课程' : counts.courses} tone="green" />
    </section>
    <section className="dashboard-grid">
      <div className="panel"><div className="panel-heading"><div><p className="eyebrow">快速开始</p><h2>常用功能</h2></div></div><div className="quick-grid">{navItems.filter((item) => item.path !== '/' && (item.roles as readonly Role[]).includes(session.role)).slice(0, 6).map(({ path, label, icon: Icon }) => <NavLink to={path} key={path}><Icon size={20} /><span>{label}</span><ChevronRight size={16} /></NavLink>)}</div></div>
      <div className="panel notice-panel"><p className="eyebrow">系统状态</p><h2>后端已连接</h2><p className="muted">登录成功即表示 API 服务与数据库可访问。后续页面将按模块逐步接入现有后端能力。</p><div className="status-line"><span className="status-dot" /> 服务运行正常</div></div>
    </section>
  </>
}

function Metric({ icon, label, value, tone }: { icon: ReactNode, label: string, value: string, tone: string }) {
  return <article className="metric-card"><span className={`metric-icon ${tone}`}>{icon}</span><div><p>{label}</p><strong>{value}</strong></div></article>
}

function StudentList({ session }: { session: Session }) {
  const [students, setStudents] = useState<Student[]>([])
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<StudentStatus | ''>('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      setError('')
      api.students(session, keyword, status).then((page) => setStudents(page.content))
        .catch((reason) => setError(reason instanceof Error ? reason.message : '学生数据加载失败'))
        .finally(() => setLoading(false))
    }, 250)
    return () => clearTimeout(timer)
  }, [keyword, status, session])

  return <>
    <section className="page-heading"><div><p className="eyebrow">学生档案</p><h1>学生管理</h1><p className="muted">查询学生基础信息与当前状态。</p></div><div className="heading-actions"><span className="date-chip">共 {students.length} 条当前结果</span>{session.role === 'ADMIN' && <Link className="primary-link" to="/students/new"><Plus size={17} />新增学生</Link>}</div></section>
    <section className="panel">
      <div className="filters"><label className="search-box"><Search size={18} /><input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="搜索姓名或学号" /></label><select value={status} onChange={(e) => setStatus(e.target.value as StudentStatus | '')}><option value="">全部状态</option>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
      {error ? <div className="empty-state">{error}</div> : loading ? <div className="empty-state">正在加载学生数据...</div> : students.length === 0 ? <div className="empty-state">暂无符合条件的学生</div> :
        <div className="table-wrap"><table><thead><tr><th>学生</th><th>学号</th><th>联系方式</th><th>入学日期</th><th>状态</th><th></th></tr></thead><tbody>{students.map((student) => <tr key={student.id}><td><strong>{student.name}</strong><small>{student.gender || '未填写性别'}</small></td><td>{student.studentNumber}</td><td>{student.phone || student.email || '—'}</td><td>{student.enrollmentDate || '—'}</td><td><span className={`status-badge ${student.status.toLowerCase()}`}>{statusLabels[student.status]}</span></td><td><Link className="row-link" to={`/students/${student.id}`}>查看<ChevronRight size={15} /></Link></td></tr>)}</tbody></table></div>}
    </section>
  </>
}

const emptyStudentForm: StudentFormData = {
  name: '', gender: '', dateOfBirth: '', phone: '', email: '', enrollmentDate: '',
}

function StudentFormPage({ session, mode }: { session: Session, mode: 'create' | 'edit' }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState<UpdateStudentData>({ ...emptyStudentForm, status: 'ACTIVE' })
  const [studentNumber, setStudentNumber] = useState('')
  const [loading, setLoading] = useState(mode === 'edit')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (mode !== 'edit' || !id) return
    api.student(session, id).then((student) => {
      setStudentNumber(student.studentNumber)
      setForm({
        name: student.name, gender: student.gender ?? '', dateOfBirth: student.dateOfBirth ?? '',
        phone: student.phone ?? '', email: student.email ?? '', enrollmentDate: student.enrollmentDate ?? '',
        status: student.status,
      })
    }).catch((reason) => setError(reason instanceof Error ? reason.message : '学生信息加载失败'))
      .finally(() => setLoading(false))
  }, [id, mode, session])

  function field(name: keyof UpdateStudentData, value: string) {
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      const student = mode === 'create'
        ? await api.createStudent(session, form)
        : await api.updateStudent(session, id!, form)
      navigate(`/students/${student.id}`, { state: { saved: mode === 'create' ? '学生档案创建成功' : '学生档案更新成功' } })
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : '保存失败，请稍后重试')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="empty-state">正在加载学生信息...</div>

  return <>
    <section className="page-heading"><div><p className="eyebrow">{mode === 'create' ? '新建档案' : '编辑档案'}</p><h1>{mode === 'create' ? '新增学生' : '编辑学生'}</h1><p className="muted">{mode === 'create' ? '学号将在创建成功后由系统自动生成。' : `学号 ${studentNumber} 创建后不可修改。`}</p></div><Link className="secondary-link" to={mode === 'edit' ? `/students/${id}` : '/students'}><ArrowLeft size={17} />返回</Link></section>
    <form className="panel student-form" onSubmit={submit}>
      <div className="form-section"><div><p className="eyebrow">基础信息</p><h2>学生身份</h2></div><div className="form-grid">
        <label className="wide">姓名 <span>*</span><input value={form.name} onChange={(e) => field('name', e.target.value)} maxLength={100} required /></label>
        <label>性别<input value={form.gender} onChange={(e) => field('gender', e.target.value)} maxLength={16} placeholder="例如：男、女或 MALE" /></label>
        <label>出生日期<input type="date" value={form.dateOfBirth} onChange={(e) => field('dateOfBirth', e.target.value)} max={new Date(Date.now() - 86400000).toISOString().slice(0, 10)} /></label>
        <label>入学日期<input type="date" value={form.enrollmentDate} onChange={(e) => field('enrollmentDate', e.target.value)} /></label>
        {mode === 'edit' && <label>当前状态<select value={form.status} onChange={(e) => field('status', e.target.value)}>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>}
      </div></div>
      <div className="form-section"><div><p className="eyebrow">联系信息</p><h2>联系方式</h2></div><div className="form-grid">
        <label>手机号<input value={form.phone} onChange={(e) => field('phone', e.target.value)} maxLength={32} /></label>
        <label>电子邮箱<input type="email" value={form.email} onChange={(e) => field('email', e.target.value)} maxLength={255} /></label>
      </div></div>
      {error && <p className="form-error form-message">{error}</p>}
      <div className="form-actions"><Link className="secondary-link" to={mode === 'edit' ? `/students/${id}` : '/students'}>取消</Link><button className="primary-button compact" disabled={saving}><Save size={17} />{saving ? '正在保存...' : '保存学生档案'}</button></div>
    </form>
  </>
}

function StudentDetail({ session }: { session: Session }) {
  const { id } = useParams()
  const [student, setStudent] = useState<Student | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    api.student(session, id).then(setStudent)
      .catch((reason) => setError(reason instanceof Error ? reason.message : '学生信息加载失败'))
  }, [id, session])

  if (error) return <div className="empty-state">{error}</div>
  if (!student) return <div className="empty-state">正在加载学生信息...</div>

  return <>
    <section className="page-heading"><div><p className="eyebrow">学生详情</p><h1>{student.name}</h1><p className="muted">学号 {student.studentNumber}</p></div><div className="heading-actions"><Link className="secondary-link" to="/students"><ArrowLeft size={17} />返回列表</Link>{session.role === 'ADMIN' && <Link className="primary-link" to={`/students/${student.id}/edit`}><Edit3 size={16} />编辑档案</Link>}</div></section>
    <section className="student-profile">
      <article className="panel profile-card"><div className="profile-avatar"><UserRound size={34} /></div><h2>{student.name}</h2><span className={`status-badge ${student.status.toLowerCase()}`}>{statusLabels[student.status]}</span><dl><div><dt>学号</dt><dd>{student.studentNumber}</dd></div><div><dt>性别</dt><dd>{student.gender || '未填写'}</dd></div><div><dt>出生日期</dt><dd>{student.dateOfBirth || '未填写'}</dd></div><div><dt>入学日期</dt><dd>{student.enrollmentDate || '未填写'}</dd></div></dl></article>
      <div className="profile-details">
        <article className="panel detail-card"><p className="eyebrow">联系信息</p><h2>联系方式</h2><div className="detail-lines"><div><Phone size={18} /><span><small>手机号</small>{student.phone || '未填写'}</span></div><div><Mail size={18} /><span><small>电子邮箱</small>{student.email || '未填写'}</span></div></div></article>
        <article className="panel detail-card"><p className="eyebrow">记录信息</p><h2>档案时间</h2><div className="detail-lines"><div><CalendarDays size={18} /><span><small>创建时间</small>{formatDateTime(student.createdAt)}</span></div><div><Edit3 size={18} /><span><small>最后更新</small>{formatDateTime(student.updatedAt)}</span></div></div></article>
        <ClassAssignmentPanel session={session} student={student} />
      </div>
    </section>
  </>
}

function ClassAssignmentPanel({ session, student }: { session: Session, student: Student }) {
  const [assignments, setAssignments] = useState<ClassAssignment[]>([])
  const [classes, setClasses] = useState<SchoolClass[]>([])
  const [classId, setClassId] = useState('')
  const [effectiveDate, setEffectiveDate] = useState(new Date().toISOString().slice(0, 10))
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const current = assignments.find((assignment) => assignment.current)

  function load() {
    api.classAssignments(session, student.id).then(setAssignments)
      .catch((reason) => setError(reason instanceof Error ? reason.message : '分班历史加载失败'))
    if (session.role === 'ADMIN') api.classes(session).then((page) => setClasses(page.content)).catch(() => undefined)
  }

  useEffect(load, [session, student.id])

  async function assign() {
    if (!classId || !effectiveDate) return
    setSaving(true)
    setError('')
    try {
      await api.assignClass(session, student.id, Number(classId), effectiveDate)
      load()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '分班操作失败')
    } finally {
      setSaving(false)
    }
  }

  async function leave() {
    if (!effectiveDate) return
    setSaving(true)
    setError('')
    try {
      await api.leaveClass(session, student.id, effectiveDate)
      load()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '离班操作失败')
    } finally {
      setSaving(false)
    }
  }

  return <article className="panel detail-card assignment-card">
    <p className="eyebrow">班级归属</p><h2>分班历史</h2>
    <div className="current-class"><School size={20} /><span><small>当前班级</small>{current ? `${current.classEntryYear}级 · ${current.className}` : '当前未分班'}</span></div>
    {session.role === 'ADMIN' && <div className="assignment-actions"><select value={classId} onChange={(e) => setClassId(e.target.value)}><option value="">选择目标班级</option>{classes.map((item) => <option key={item.id} value={item.id}>{item.entryYear}级 · {item.name}</option>)}</select><input type="date" value={effectiveDate} onChange={(e) => setEffectiveDate(e.target.value)} /><button className="primary-link" type="button" disabled={saving || !classId} onClick={assign}><UserPlus size={15} />{current ? '转班' : '入班'}</button>{current && <button className="danger-link" type="button" disabled={saving} onClick={leave}><UserMinus size={15} />离班</button>}</div>}
    {error && <p className="form-error">{error}</p>}
    <div className="history-list">{assignments.length === 0 ? <p className="muted">暂无分班历史</p> : assignments.map((assignment) => <div key={assignment.id}><span className={assignment.current ? 'history-dot current' : 'history-dot'} /><div><strong>{assignment.classEntryYear}级 · {assignment.className}</strong><small>{assignment.startDate} 至 {assignment.endDate || '现在'}</small></div>{assignment.current && <span className="status-badge active">当前</span>}</div>)}</div>
  </article>
}

function ClassList({ session }: { session: Session }) {
  const [classes, setClasses] = useState<SchoolClass[]>([])
  const [keyword, setKeyword] = useState('')
  const [entryYear, setEntryYear] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true); setError('')
      api.classes(session, keyword, entryYear).then((page) => setClasses(page.content))
        .catch((reason) => setError(reason instanceof Error ? reason.message : '班级数据加载失败'))
        .finally(() => setLoading(false))
    }, 250)
    return () => clearTimeout(timer)
  }, [entryYear, keyword, session])

  return <>
    <section className="page-heading"><div><p className="eyebrow">教学组织</p><h1>班级管理</h1><p className="muted">查询班级、入学年份与班主任信息。</p></div><div className="heading-actions"><span className="date-chip">共 {classes.length} 个班级</span>{session.role === 'ADMIN' && <Link className="primary-link" to="/classes/new"><Plus size={17} />新增班级</Link>}</div></section>
    <section className="panel"><div className="filters"><label className="search-box"><Search size={18} /><input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="搜索班级名称" /></label><input className="year-filter" type="number" min="1900" max="2200" value={entryYear} onChange={(e) => setEntryYear(e.target.value)} placeholder="入学年份" /></div>
      {error ? <div className="empty-state">{error}</div> : loading ? <div className="empty-state">正在加载班级数据...</div> : classes.length === 0 ? <div className="empty-state">暂无符合条件的班级</div> : <div className="class-grid">{classes.map((item) => <Link className="class-card" to={`/classes/${item.id}`} key={item.id}><span><School size={22} /></span><div><small>{item.entryYear}级</small><h3>{item.name}</h3><p>班主任：{item.homeroomTeacherUsername}</p></div><ChevronRight size={17} /></Link>)}</div>}
    </section>
  </>
}

function ClassCreate({ session }: { session: Session }) {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [entryYear, setEntryYear] = useState(new Date().getFullYear())
  const [teacherId, setTeacherId] = useState('')
  const [teachers, setTeachers] = useState<TeacherAccount[]>([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.teachers(session).then(setTeachers)
      .catch((reason) => setError(reason instanceof Error ? reason.message : '教师列表加载失败'))
  }, [session])

  async function submit(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError('')
    try {
      const created = await api.createSchoolClass(session, { name, entryYear, homeroomTeacherId: Number(teacherId) })
      navigate(`/classes/${created.id}`)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '班级创建失败')
    } finally { setSaving(false) }
  }

  return <>
    <section className="page-heading"><div><p className="eyebrow">新建教学组织</p><h1>新增班级</h1><p className="muted">同一入学年份内班级名称不能重复。</p></div><Link className="secondary-link" to="/classes"><ArrowLeft size={17} />返回列表</Link></section>
    <form className="panel student-form" onSubmit={submit}><div className="form-section"><div><p className="eyebrow">班级信息</p><h2>基础资料</h2></div><div className="form-grid"><label className="wide">班级名称 <span>*</span><input value={name} onChange={(e) => setName(e.target.value)} maxLength={100} required /></label><label>入学年份 <span>*</span><input type="number" min="1900" max="2200" value={entryYear} onChange={(e) => setEntryYear(Number(e.target.value))} required /></label><label>班主任教师 <span>*</span><select value={teacherId} onChange={(e) => setTeacherId(e.target.value)} required><option value="">请选择启用教师</option>{teachers.map((teacher) => <option key={teacher.id} value={teacher.id}>{teacher.username}</option>)}</select>{teachers.length === 0 && <small className="field-help">暂无可选教师，请先创建教师账号。</small>}</label></div></div>{error && <p className="form-error form-message">{error}</p>}<div className="form-actions"><Link className="secondary-link" to="/classes">取消</Link><button className="primary-button compact" disabled={saving}><Save size={17} />{saving ? '正在创建...' : '创建班级'}</button></div></form>
  </>
}

function ClassDetail({ session }: { session: Session }) {
  const { id } = useParams()
  const [schoolClass, setSchoolClass] = useState<SchoolClass | null>(null)
  const [error, setError] = useState('')
  const [students, setStudents] = useState<Student[]>([])
  useEffect(() => {
    if (!id) return
    Promise.all([api.schoolClass(session, id), api.classStudents(session, id)])
      .then(([classData, roster]) => { setSchoolClass(classData); setStudents(roster) })
      .catch((reason) => setError(reason instanceof Error ? reason.message : '班级加载失败'))
  }, [id, session])
  if (error) return <div className="empty-state">{error}</div>
  if (!schoolClass) return <div className="empty-state">正在加载班级信息...</div>
  return <>
    <section className="page-heading"><div><p className="eyebrow">班级详情</p><h1>{schoolClass.name}</h1><p className="muted">{schoolClass.entryYear}级教学班</p></div><Link className="secondary-link" to="/classes"><ArrowLeft size={17} />返回列表</Link></section>
    <section className="class-detail-grid"><article className="panel detail-card"><p className="eyebrow">基本资料</p><h2>班级信息</h2><div className="detail-lines"><div><School size={18} /><span><small>班级名称</small>{schoolClass.name}</span></div><div><CalendarDays size={18} /><span><small>入学年份</small>{schoolClass.entryYear}</span></div><div><UserRound size={18} /><span><small>班主任账号</small>{schoolClass.homeroomTeacherUsername}</span></div><div><History size={18} /><span><small>创建时间</small>{formatDateTime(schoolClass.createdAt)}</span></div></div></article><article className="panel roster-panel"><div className="panel-heading"><div><p className="eyebrow">当前花名册</p><h2>{students.length} 名学生</h2></div><Link className="secondary-link" to="/students">管理分班<ChevronRight size={15} /></Link></div>{students.length === 0 ? <div className="empty-state">当前班级暂无学生</div> : <div className="roster-list">{students.map((student) => <Link to={`/students/${student.id}`} key={student.id}><span className="avatar">{student.name.slice(0, 1)}</span><div><strong>{student.name}</strong><small>{student.studentNumber}</small></div><span className={`status-badge ${student.status.toLowerCase()}`}>{statusLabels[student.status]}</span><ChevronRight size={15} /></Link>)}</div>}</article></section>
  </>
}

function CourseManagement({ session }: { session: Session }) {
  const [tab, setTab] = useState<'catalog' | 'offerings'>('catalog')
  return <>
    <section className="page-heading"><div><p className="eyebrow">教学课程</p><h1>课程管理</h1><p className="muted">维护全校课程目录，并将课程安排给具体班级和教师。</p></div></section>
    <div className="tab-bar"><button className={tab === 'catalog' ? 'active' : ''} onClick={() => setTab('catalog')}><BookOpen size={16} />课程目录</button><button className={tab === 'offerings' ? 'active' : ''} onClick={() => setTab('offerings')}><Layers3 size={16} />开课安排</button></div>
    {tab === 'catalog' ? <CourseCatalog session={session} /> : <OfferingList session={session} />}
  </>
}

function CourseCatalog({ session }: { session: Session }) {
  const [courses, setCourses] = useState<Course[]>([])
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<CourseStatus | ''>('')
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [credits, setCredits] = useState('3.0')
  const [courseStatus, setCourseStatus] = useState<CourseStatus>('ACTIVE')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    setLoading(true); setError('')
    api.courses(session, keyword, status).then((page) => setCourses(page.content))
      .catch((reason) => setError(reason instanceof Error ? reason.message : '课程目录加载失败'))
      .finally(() => setLoading(false))
  }, [keyword, session, status])
  useEffect(() => { const timer = setTimeout(load, 250); return () => clearTimeout(timer) }, [load])

  async function create(event: FormEvent) {
    event.preventDefault(); setError('')
    try {
      await api.createCourse(session, { name, credits: Number(credits), status: courseStatus })
      setShowForm(false); setName(''); setCredits('3.0'); load()
    } catch (reason) { setError(reason instanceof Error ? reason.message : '课程创建失败') }
  }

  return <section className="panel">
    <div className="filters"><label className="search-box"><Search size={18} /><input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="搜索课程名称或编号" /></label><select value={status} onChange={(e) => setStatus(e.target.value as CourseStatus | '')}><option value="">全部状态</option><option value="ACTIVE">启用</option><option value="INACTIVE">停用</option></select>{session.role === 'ADMIN' && <button className="primary-link filter-action" onClick={() => setShowForm(!showForm)}><Plus size={16} />新增课程</button>}</div>
    {showForm && <form className="inline-create" onSubmit={create}><label>课程名称<input value={name} onChange={(e) => setName(e.target.value)} maxLength={100} required /></label><label>学分<input type="number" min="0.5" max="30" step="0.1" value={credits} onChange={(e) => setCredits(e.target.value)} required /></label><label>状态<select value={courseStatus} onChange={(e) => setCourseStatus(e.target.value as CourseStatus)}><option value="ACTIVE">启用</option><option value="INACTIVE">停用</option></select></label><button className="primary-link"><Save size={15} />保存课程</button></form>}
    {error ? <div className="empty-state">{error}</div> : loading ? <div className="empty-state">正在加载课程目录...</div> : courses.length === 0 ? <div className="empty-state">暂无符合条件的课程</div> : <div className="course-grid">{courses.map((course) => <Link className="course-card" to={`/courses/${course.id}`} key={course.id}><span><BookOpen size={21} /></span><div><small>{course.courseCode}</small><h3>{course.name}</h3><p>{course.credits} 学分</p></div><span className={`status-badge ${course.status === 'ACTIVE' ? 'active' : 'withdrawn'}`}>{courseStatusLabels[course.status]}</span></Link>)}</div>}
  </section>
}

function OfferingList({ session }: { session: Session }) {
  const [offerings, setOfferings] = useState<CourseOffering[]>([])
  const [courses, setCourses] = useState<Course[]>([])
  const [classes, setClasses] = useState<SchoolClass[]>([])
  const [teachers, setTeachers] = useState<TeacherAccount[]>([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ courseId: '', classId: '', teacherId: '', academicYear: String(new Date().getFullYear()), semester: 'FIRST' as Semester })
  const [filters, setFilters] = useState({ courseId: '', classId: '', academicYear: '', semester: '' as Semester | '' })
  const [error, setError] = useState('')

  function load() { setError(''); api.offerings(session, filters).then((page) => setOfferings(page.content)).catch((reason) => setError(reason instanceof Error ? reason.message : '开课安排加载失败')) }
  useEffect(() => {
    setError('')
    api.offerings(session, filters).then((page) => setOfferings(page.content))
      .catch((reason) => setError(reason instanceof Error ? reason.message : '开课安排加载失败'))
  }, [filters, session])
  useEffect(() => {
    Promise.all([api.courses(session, '', 'ACTIVE'), api.classes(session)]).then(([coursePage, classPage]) => { setCourses(coursePage.content); setClasses(classPage.content) })
    if (session.role === 'ADMIN') api.teachers(session).then(setTeachers).catch(() => undefined)
  }, [session])

  async function create(event: FormEvent) {
    event.preventDefault(); setError('')
    try {
      await api.createOffering(session, { courseId: Number(form.courseId), classId: Number(form.classId), teacherId: Number(form.teacherId), academicYear: Number(form.academicYear), semester: form.semester })
      setShowForm(false); load()
    } catch (reason) { setError(reason instanceof Error ? reason.message : '开课安排创建失败') }
  }

  function filter(name: keyof typeof filters, value: string) { setFilters((current) => ({ ...current, [name]: value })) }
  function field(name: keyof typeof form, value: string) { setForm((current) => ({ ...current, [name]: value })) }

  return <section className="panel">
    <div className="filters offering-filters"><select value={filters.courseId} onChange={(e) => filter('courseId', e.target.value)}><option value="">全部课程</option>{courses.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><select value={filters.classId} onChange={(e) => filter('classId', e.target.value)}><option value="">全部班级</option>{classes.map((item) => <option key={item.id} value={item.id}>{item.entryYear}级 · {item.name}</option>)}</select><input className="year-filter" type="number" placeholder="学年" value={filters.academicYear} onChange={(e) => filter('academicYear', e.target.value)} /><select value={filters.semester} onChange={(e) => filter('semester', e.target.value)}><option value="">全部学期</option><option value="FIRST">第一学期</option><option value="SECOND">第二学期</option></select>{session.role === 'ADMIN' && <button className="primary-link filter-action" onClick={() => setShowForm(!showForm)}><Plus size={16} />新增安排</button>}</div>
    {showForm && <form className="inline-create offering-create" onSubmit={create}><label>课程<select value={form.courseId} onChange={(e) => field('courseId', e.target.value)} required><option value="">请选择</option>{courses.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>班级<select value={form.classId} onChange={(e) => field('classId', e.target.value)} required><option value="">请选择</option>{classes.map((item) => <option key={item.id} value={item.id}>{item.entryYear}级 · {item.name}</option>)}</select></label><label>授课教师<select value={form.teacherId} onChange={(e) => field('teacherId', e.target.value)} required><option value="">请选择</option>{teachers.map((item) => <option key={item.id} value={item.id}>{item.username}</option>)}</select></label><label>学年<input type="number" min="1900" max="2200" value={form.academicYear} onChange={(e) => field('academicYear', e.target.value)} required /></label><label>学期<select value={form.semester} onChange={(e) => field('semester', e.target.value)}><option value="FIRST">第一学期</option><option value="SECOND">第二学期</option></select></label><button className="primary-link"><Save size={15} />保存安排</button></form>}
    {error ? <div className="empty-state">{error}</div> : offerings.length === 0 ? <div className="empty-state">暂无符合条件的开课安排</div> : <div className="table-wrap"><table><thead><tr><th>课程</th><th>班级</th><th>授课教师</th><th>学年学期</th><th></th></tr></thead><tbody>{offerings.map((item) => <tr key={item.id}><td><strong>{item.courseName}</strong><small>{item.courseCode}</small></td><td>{item.className}</td><td>{item.teacherUsername}</td><td>{item.academicYear} · {semesterLabels[item.semester]}</td><td><Link className="row-link" to={`/offerings/${item.id}`}>查看<ChevronRight size={15} /></Link></td></tr>)}</tbody></table></div>}
  </section>
}

function CourseDetail({ session }: { session: Session }) {
  const { id } = useParams(); const [course, setCourse] = useState<Course | null>(null); const [offerings, setOfferings] = useState<CourseOffering[]>([]); const [error, setError] = useState('')
  useEffect(() => { if (!id) return; Promise.all([api.course(session, id), api.offerings(session, { courseId: id })]).then(([data, page]) => { setCourse(data); setOfferings(page.content) }).catch((reason) => setError(reason instanceof Error ? reason.message : '课程加载失败')) }, [id, session])
  if (error) return <div className="empty-state">{error}</div>; if (!course) return <div className="empty-state">正在加载课程...</div>
  return <><section className="page-heading"><div><p className="eyebrow">课程详情</p><h1>{course.name}</h1><p className="muted">{course.courseCode}</p></div><Link className="secondary-link" to="/courses"><ArrowLeft size={17} />返回课程管理</Link></section><section className="class-detail-grid"><article className="panel detail-card"><p className="eyebrow">课程资料</p><h2>基本信息</h2><div className="detail-lines"><div><BookOpen size={18} /><span><small>课程编号</small>{course.courseCode}</span></div><div><TrendingUp size={18} /><span><small>学分</small>{course.credits}</span></div><div><CheckCircle2 size={18} /><span><small>状态</small>{courseStatusLabels[course.status]}</span></div><div><History size={18} /><span><small>创建时间</small>{formatDateTime(course.createdAt)}</span></div></div></article><article className="panel roster-panel"><div className="panel-heading"><div><p className="eyebrow">开课安排</p><h2>{offerings.length} 条记录</h2></div></div>{offerings.length === 0 ? <div className="empty-state">暂无开课安排</div> : <div className="roster-list">{offerings.map((item) => <Link to={`/offerings/${item.id}`} key={item.id}><span className="avatar"><Layers3 size={15} /></span><div><strong>{item.className}</strong><small>{item.academicYear} · {semesterLabels[item.semester]} · {item.teacherUsername}</small></div><ChevronRight size={15} /></Link>)}</div>}</article></section></>
}

function OfferingDetail({ session }: { session: Session }) {
  const { id } = useParams(); const [item, setItem] = useState<CourseOffering | null>(null); const [error, setError] = useState('')
  useEffect(() => { if (id) api.offering(session, id).then(setItem).catch((reason) => setError(reason instanceof Error ? reason.message : '开课安排加载失败')) }, [id, session])
  if (error) return <div className="empty-state">{error}</div>; if (!item) return <div className="empty-state">正在加载开课安排...</div>
  return <><section className="page-heading"><div><p className="eyebrow">开课安排详情</p><h1>{item.courseName}</h1><p className="muted">{item.academicYear} · {semesterLabels[item.semester]}</p></div><Link className="secondary-link" to="/courses"><ArrowLeft size={17} />返回课程管理</Link></section><article className="panel detail-card"><div className="detail-lines"><div><BookOpen size={18} /><span><small>课程</small>{item.courseCode} · {item.courseName}</span></div><div><School size={18} /><span><small>班级</small>{item.className}</span></div><div><UserRound size={18} /><span><small>授课教师</small>{item.teacherUsername}</span></div><div><CalendarDays size={18} /><span><small>学年学期</small>{item.academicYear} · {semesterLabels[item.semester]}</span></div></div></article></>
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function ModulePlaceholder({ title, description, icon }: { title: string, description: string, icon: ReactNode }) {
  return <section className="placeholder panel"><span className="placeholder-icon">{icon}</span><p className="eyebrow">模块入口已就绪</p><h1>{title}</h1><p className="muted">{description}</p><p className="next-note">下一轮将接入对应后端接口并完成增删改查与统计视图。</p></section>
}

function ProtectedRoutes({ session, onLogout }: { session: Session, onLogout: () => void }) {
  return <AppShell session={session} onLogout={onLogout}><Routes>
    <Route path="/" element={<Dashboard session={session} />} />
    <Route path="/students" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <StudentList session={session} />} />
    <Route path="/students/new" element={session.role === 'ADMIN' ? <StudentFormPage session={session} mode="create" /> : <Navigate to="/students" />} />
    <Route path="/students/:id" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <StudentDetail session={session} />} />
    <Route path="/students/:id/edit" element={session.role === 'ADMIN' ? <StudentFormPage session={session} mode="edit" /> : <Navigate to="/students" />} />
    <Route path="/classes" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <ClassList session={session} />} />
    <Route path="/classes/new" element={session.role === 'ADMIN' ? <ClassCreate session={session} /> : <Navigate to="/classes" />} />
    <Route path="/classes/:id" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <ClassDetail session={session} />} />
    <Route path="/courses" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <CourseManagement session={session} />} />
    <Route path="/courses/:id" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <CourseDetail session={session} />} />
    <Route path="/offerings/:id" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <OfferingDetail session={session} />} />
    <Route path="/timetable" element={<ModulePlaceholder title="学生课程表" description="按学年学期查看学生课程安排。" icon={<CalendarDays />} />} />
    <Route path="/grades" element={<ModulePlaceholder title="成绩管理" description="录入成绩并查看教学班与学生学期统计。" icon={<TrendingUp />} />} />
    <Route path="/attendance" element={<ModulePlaceholder title="考勤管理" description="登记课堂考勤并查看实时统计。" icon={<ClipboardCheck />} />} />
    <Route path="/settings" element={<ModulePlaceholder title="系统设置" description="管理账号与系统运行信息。" icon={<Settings />} />} />
    <Route path="*" element={<Navigate to="/" />} />
  </Routes></AppShell>
}

export default function App() {
  const [session, setSession] = useState<Session | null>(() => loadSession())
  return <BrowserRouter>{session ? <ProtectedRoutes session={session} onLogout={() => setSession(null)} /> : <LoginPage onLogin={setSession} />}</BrowserRouter>
}

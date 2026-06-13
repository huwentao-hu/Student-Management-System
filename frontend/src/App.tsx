import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import {
  BookOpen, CalendarDays, CheckCircle2, ChevronRight, ClipboardCheck, GraduationCap,
  LayoutDashboard, LogOut, Menu, School, Search, Settings, TrendingUp, Users, X,
} from 'lucide-react'
import { BrowserRouter, NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { api, ApiError, clearSession, loadSession, saveSession } from './api'
import type { Role, Session, Student, StudentStatus } from './types'
import './App.css'

const roleLabels: Record<Role, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
const statusLabels: Record<StudentStatus, string> = {
  ACTIVE: '在读', SUSPENDED: '停学', GRADUATED: '毕业', WITHDRAWN: '退学',
}

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
      api.students(session, keyword, status).then((page) => setStudents(page.content))
        .catch((reason) => setError(reason instanceof Error ? reason.message : '学生数据加载失败'))
        .finally(() => setLoading(false))
    }, 250)
    return () => clearTimeout(timer)
  }, [keyword, status, session])

  return <>
    <section className="page-heading"><div><p className="eyebrow">学生档案</p><h1>学生管理</h1><p className="muted">查询学生基础信息与当前状态。</p></div><span className="date-chip">共 {students.length} 条当前结果</span></section>
    <section className="panel">
      <div className="filters"><label className="search-box"><Search size={18} /><input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="搜索姓名或学号" /></label><select value={status} onChange={(e) => setStatus(e.target.value as StudentStatus | '')}><option value="">全部状态</option>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
      {error ? <div className="empty-state">{error}</div> : loading ? <div className="empty-state">正在加载学生数据...</div> : students.length === 0 ? <div className="empty-state">暂无符合条件的学生</div> :
        <div className="table-wrap"><table><thead><tr><th>学生</th><th>学号</th><th>联系方式</th><th>入学日期</th><th>状态</th></tr></thead><tbody>{students.map((student) => <tr key={student.id}><td><strong>{student.name}</strong><small>{student.gender || '未填写性别'}</small></td><td>{student.studentNumber}</td><td>{student.phone || student.email || '—'}</td><td>{student.enrollmentDate || '—'}</td><td><span className={`status-badge ${student.status.toLowerCase()}`}>{statusLabels[student.status]}</span></td></tr>)}</tbody></table></div>}
    </section>
  </>
}

function ModulePlaceholder({ title, description, icon }: { title: string, description: string, icon: ReactNode }) {
  return <section className="placeholder panel"><span className="placeholder-icon">{icon}</span><p className="eyebrow">模块入口已就绪</p><h1>{title}</h1><p className="muted">{description}</p><p className="next-note">下一轮将接入对应后端接口并完成增删改查与统计视图。</p></section>
}

function ProtectedRoutes({ session, onLogout }: { session: Session, onLogout: () => void }) {
  return <AppShell session={session} onLogout={onLogout}><Routes>
    <Route path="/" element={<Dashboard session={session} />} />
    <Route path="/students" element={session.role === 'STUDENT' ? <Navigate to="/" /> : <StudentList session={session} />} />
    <Route path="/classes" element={<ModulePlaceholder title="班级管理" description="管理班级、班主任与学生分班历史。" icon={<School />} />} />
    <Route path="/courses" element={<ModulePlaceholder title="课程管理" description="管理课程目录与学期开课安排。" icon={<BookOpen />} />} />
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

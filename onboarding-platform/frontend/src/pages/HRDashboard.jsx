import { useState, useEffect } from 'react';
import Sidebar from '../components/shared/Sidebar';
import Chatbot from '../components/chatbot/Chatbot';
import { hrApi, aiService } from '../services/api';
import {
  LayoutDashboard, Users, FileText, Bell, Calendar, MessageSquare,
  AlertTriangle, TrendingUp, Shield, CheckCircle2, XCircle, Send,
  Mail, RefreshCw, Eye, BarChart3, PieChart as PieIcon
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
  PieChart, Pie, Cell, Legend, LineChart, Line, AreaChart, Area
} from 'recharts';
import toast from 'react-hot-toast';

const NAV = [
  { id:'dashboard',  label:'Dashboard',     icon:LayoutDashboard },
  { id:'employees',  label:'All Employees', icon:Users },
  { id:'documents',  label:'Doc Verification', icon:FileText },
  { id:'risk',       label:'Risk Analysis',  icon:AlertTriangle },
  { id:'meetings',   label:'Meetings',       icon:Calendar },
  { id:'feedback',   label:'Feedback',       icon:MessageSquare },
  { id:'analytics',  label:'AI Analytics',   icon:BarChart3 },
];

const RISK_COLORS = { Critical:'#ef4444', High:'#f97316', Medium:'#f59e0b', Low:'#10b981' };
const DEPT_COLORS = ['#6366f1','#10b981','#f59e0b','#ef4444','#3b82f6','#8b5cf6','#ec4899'];

function StatCard({ label, value, icon: Icon, color, sub }) {
  return (
    <div className="stat-card">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium" style={{ color:'var(--text-muted)' }}>{label}</p>
        <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background:`${color}18` }}>
          <Icon size={16} style={{ color }} />
        </div>
      </div>
      <p className="text-2xl font-bold mt-1" style={{ color:'var(--text)' }}>{value}</p>
      {sub && <p className="text-xs" style={{ color:'var(--text-muted)' }}>{sub}</p>}
    </div>
  );
}

function RiskBadge({ score }) {
  const [level,cls] = score >= 75 ? ['Critical','badge-danger'] :
                      score >= 50 ? ['High',    'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400'] :
                      score >= 30 ? ['Medium',  'badge-warning'] :
                                    ['Low',     'badge-success'];
  return <span className={`badge ${cls}`}>{level} ({score})</span>;
}

export default function HRDashboard() {
  const [tab, setTab]         = useState('dashboard');
  const [stats, setStats]     = useState(null);
  const [employees, setEmps]  = useState([]);
  const [meetings, setMeetings]= useState([]);
  const [feedbacks, setFeedbacks]= useState([]);
  const [aiData, setAiData]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedEmp, setSelEmp]= useState(null);
  const [meetForm, setMeetForm]= useState({ title:'', scheduledAt:'', durationMinutes:30, meetingType:'ONE_ON_ONE', participantId:'', meetLink:'', description:'' });
  const [showMeetModal, setShowMeetModal] = useState(false);
  const [search, setSearch]   = useState('');
  const [riskFilter, setRiskFilter] = useState('all');
  const [deptFilter, setDeptFilter] = useState('all');

  const load = async () => {
    try {
      const [s, e, m, f] = await Promise.all([
        hrApi.dashboard(), hrApi.employees(), hrApi.meetings(), hrApi.feedback()
      ]);
      setStats(s.data);
      setEmps(e.data);
      setMeetings(m.data);
      setFeedbacks(f.data);
      try {
        const ai = await aiService.analytics();
        setAiData(ai.data);
      } catch {}
    } catch { toast.error('Failed to load HR dashboard'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleVerifyDoc = async (empId, docType, approved) => {
    try {
      await hrApi.verifyDoc(empId, docType, approved, '');
      toast.success(`Document ${approved ? 'verified ✅' : 'rejected ❌'}`);
      const res = await hrApi.employees(); setEmps(res.data);
    } catch { toast.error('Action failed'); }
  };

  const handleRemind = async (id) => {
    try { await hrApi.remindDocs(id); toast.success('Reminder email sent! 📧'); }
    catch { toast.error('Failed to send reminder'); }
  };

  const handleRiskAlert = async (id) => {
    try { await hrApi.sendRiskAlert(id); toast.success('Risk alert emails sent to HR team! 🚨'); }
    catch { toast.error('Failed to send alert'); }
  };

  const handleScheduleMeeting = async (e) => {
    e.preventDefault();
    if (!meetForm.participantId || !meetForm.scheduledAt || !meetForm.title) {
      toast.error('Fill all required fields'); return;
    }
    try {
      await hrApi.scheduleMeeting({ ...meetForm, durationMinutes: Number(meetForm.durationMinutes) });
      toast.success('Meeting scheduled & email sent! 📅');
      setShowMeetModal(false);
      setMeetForm({ title:'', scheduledAt:'', durationMinutes:30, meetingType:'ONE_ON_ONE', participantId:'', meetLink:'', description:'' });
      const m = await hrApi.meetings(); setMeetings(m.data);
    } catch { toast.error('Failed to schedule meeting'); }
  };

  const filtered = employees.filter(e => {
    const matchSearch = search === '' || e.name.toLowerCase().includes(search.toLowerCase()) ||
                        e.email.toLowerCase().includes(search.toLowerCase()) ||
                        (e.department||'').toLowerCase().includes(search.toLowerCase());
    const rs = e.riskScore || 0;
    const matchRisk = riskFilter === 'all' || (
      riskFilter === 'critical' ? rs >= 75 :
      riskFilter === 'high' ? rs >= 50 && rs < 75 :
      riskFilter === 'medium' ? rs >= 30 && rs < 50 : rs < 30
    );
    const matchDept = deptFilter === 'all' || e.department === deptFilter;
    return matchSearch && matchRisk && matchDept;
  });

  const depts = [...new Set(employees.map(e => e.department).filter(Boolean))];

  if (loading) return (
    <div className="flex items-center justify-center h-screen" style={{ background:'var(--bg)' }}>
      <div className="text-center">
        <div className="w-12 h-12 border-4 border-violet-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p style={{ color:'var(--text-muted)' }}>Loading HR dashboard…</p>
      </div>
    </div>
  );

  const summary = stats?.summary || {};
  const riskDist = stats?.riskDistribution || {};
  const byDept   = stats?.byDepartment || {};
  const monthly  = stats?.monthlyJoins  || {};

  const pieData = Object.entries(riskDist).map(([name,val]) => ({ name, value:val, color:RISK_COLORS[name] }));
  const deptData = Object.entries(byDept).map(([name,val]) => ({ name, value:Number(val) }));
  const monthlyData = Object.entries(monthly).map(([name,val]) => ({ name, joins:Number(val) }));

  return (
    <div className="flex min-h-screen" style={{ background:'var(--bg)' }}>
      <Sidebar links={NAV} activeTab={tab} onTabChange={setTab} />
      <main className="flex-1 lg:ml-60 p-6 pb-20">

        {/* ── Dashboard Overview ────────────────────────────────── */}
        {tab === 'dashboard' && (
          <div className="fade-in space-y-6 max-w-7xl">
            <div className="flex items-center justify-between">
              <h1 className="text-2xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>HR Dashboard</h1>
              <button onClick={load} className="btn-ghost gap-2"><RefreshCw size={14} />Refresh</button>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <StatCard label="Total Employees" value={summary.totalEmployees || 0} icon={Users} color="#6366f1" sub={`${summary.totalManagers} managers • ${summary.totalHR} HR`} />
              <StatCard label="Onboarding Complete" value={`${summary.completionRate || 0}%`} icon={CheckCircle2} color="#10b981" sub={`${summary.onboardingCompleted}/${summary.totalEmployees} employees`} />
              <StatCard label="High Risk Employees" value={summary.highRiskCount || 0} icon={AlertTriangle} color="#ef4444" sub={`${summary.criticalRiskCount} critical`} />
              <StatCard label="Avg Engagement" value={`${summary.avgEngagement || 0}%`} icon={TrendingUp} color="#f59e0b" sub={`Avg task: ${summary.avgTaskCompletion || 0}%`} />
            </div>

            {/* Charts */}
            <div className="grid lg:grid-cols-3 gap-6">
              {/* Risk Pie */}
              <div className="card p-5">
                <p className="section-title">Risk Distribution</p>
                <ResponsiveContainer width="100%" height={200}>
                  <PieChart>
                    <Pie data={pieData} cx="50%" cy="50%" innerRadius={55} outerRadius={80} dataKey="value" paddingAngle={3}>
                      {pieData.map((e,i) => <Cell key={i} fill={e.color} />)}
                    </Pie>
                    <Tooltip formatter={(v,n) => [v, n]} />
                    <Legend iconType="circle" iconSize={10} formatter={(v) => <span style={{ color:'var(--text)', fontSize:12 }}>{v}</span>} />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              {/* Dept Bar */}
              <div className="card p-5 lg:col-span-2">
                <p className="section-title">Employees by Department</p>
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={deptData} barSize={28}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="name" tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                    <YAxis tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                    <Tooltip contentStyle={{ background:'var(--bg-card)', border:'1px solid var(--border)', borderRadius:12 }} />
                    <Bar dataKey="value" radius={[6,6,0,0]}>
                      {deptData.map((e,i) => <Cell key={i} fill={DEPT_COLORS[i % DEPT_COLORS.length]} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Monthly joining trend */}
            {monthlyData.length > 0 && (
              <div className="card p-5">
                <p className="section-title">Monthly Joining Trend</p>
                <ResponsiveContainer width="100%" height={160}>
                  <AreaChart data={monthlyData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="name" tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                    <YAxis tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                    <Tooltip contentStyle={{ background:'var(--bg-card)', border:'1px solid var(--border)', borderRadius:12 }} />
                    <Area type="monotone" dataKey="joins" stroke="#6366f1" fill="rgba(99,102,241,.15)" strokeWidth={2} />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        )}

        {/* ── All Employees ────────────────────────────────────── */}
        {tab === 'employees' && (
          <div className="fade-in space-y-4 max-w-7xl">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>All Employees</h2>
              <span className="badge badge-purple">{filtered.length} / {employees.length}</span>
            </div>
            {/* Filters */}
            <div className="flex flex-wrap gap-3">
              <input value={search} onChange={e=>setSearch(e.target.value)}
                     placeholder="Search name, email, department…" className="input-field w-64 py-2" />
              <select value={riskFilter} onChange={e=>setRiskFilter(e.target.value)} className="input-field w-36 py-2">
                <option value="all">All Risk</option>
                <option value="critical">Critical (75+)</option>
                <option value="high">High (50-74)</option>
                <option value="medium">Medium (30-49)</option>
                <option value="low">Low (&lt;30)</option>
              </select>
              <select value={deptFilter} onChange={e=>setDeptFilter(e.target.value)} className="input-field w-44 py-2">
                <option value="all">All Departments</option>
                {depts.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
            </div>
            {/* Table */}
            <div className="card overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr style={{ background:'var(--bg)', borderBottom:'1px solid var(--border)' }}>
                      {['Employee','Department','Progress','Risk Score','Documents','Engagement','Actions'].map(h => (
                        <th key={h} className="px-4 py-3 text-left text-xs font-semibold" style={{ color:'var(--text-muted)' }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((emp, i) => {
                      const docs = emp.documents || {};
                      const docsDone = Object.values(docs).filter(Boolean).length;
                      const rs = emp.riskScore || 0;
                      return (
                        <tr key={emp.id} style={{ borderBottom:'1px solid var(--border)' }}
                            className="hover:bg-gray-50 dark:hover:bg-gray-800/40 transition-colors">
                          <td className="px-4 py-3">
                            <div className="flex items-center gap-3">
                              <img src={emp.avatar} alt="" className="w-8 h-8 rounded-full bg-gray-100" />
                              <div>
                                <p className="font-semibold" style={{ color:'var(--text)' }}>{emp.name}</p>
                                <p className="text-xs" style={{ color:'var(--text-muted)' }}>{emp.email}</p>
                              </div>
                            </div>
                          </td>
                          <td className="px-4 py-3 text-xs" style={{ color:'var(--text-muted)' }}>{emp.department}</td>
                          <td className="px-4 py-3">
                            <div className="flex items-center gap-2">
                              <div className="risk-bar w-16">
                                <div className="risk-fill bg-indigo-500" style={{ width:`${emp.onboardingProgress||0}%` }} />
                              </div>
                              <span className="text-xs font-medium" style={{ color:'var(--text)' }}>{emp.onboardingProgress||0}%</span>
                            </div>
                          </td>
                          <td className="px-4 py-3"><RiskBadge score={rs} /></td>
                          <td className="px-4 py-3">
                            <span className={`text-xs font-semibold ${docsDone >= 8 ? 'text-emerald-500' : docsDone >= 5 ? 'text-yellow-500' : 'text-red-500'}`}>
                              {docsDone}/{Object.keys(docs).length}
                            </span>
                          </td>
                          <td className="px-4 py-3">
                            <span className="text-xs font-semibold" style={{ color:'var(--text)' }}>{emp.engagementScore}%</span>
                          </td>
                          <td className="px-4 py-3">
                            <div className="flex gap-1.5">
                              <button onClick={() => setSelEmp(emp)} className="p-1.5 rounded-lg hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-500" title="View">
                                <Eye size={14} />
                              </button>
                              <button onClick={() => handleRemind(emp.id)} className="p-1.5 rounded-lg hover:bg-amber-100 dark:hover:bg-amber-900/30 text-amber-500" title="Send reminder">
                                <Mail size={14} />
                              </button>
                              {rs >= 50 && (
                                <button onClick={() => handleRiskAlert(emp.id)} className="p-1.5 rounded-lg hover:bg-red-100 dark:hover:bg-red-900/30 text-red-500" title="Send risk alert">
                                  <AlertTriangle size={14} />
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Employee detail modal */}
            {selectedEmp && (
              <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                <div className="card p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto slide-up">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <img src={selectedEmp.avatar} alt="" className="w-14 h-14 rounded-2xl" />
                      <div>
                        <h3 className="font-bold text-lg" style={{ color:'var(--text)' }}>{selectedEmp.name}</h3>
                        <p className="text-sm" style={{ color:'var(--text-muted)' }}>{selectedEmp.employeeCode} • {selectedEmp.department}</p>
                        <RiskBadge score={selectedEmp.riskScore || 0} />
                      </div>
                    </div>
                    <button onClick={() => setSelEmp(null)} className="btn-ghost text-xs">Close</button>
                  </div>

                  <div className="grid grid-cols-2 gap-4 mb-4">
                    {[
                      ['Email', selectedEmp.email], ['Location', selectedEmp.location],
                      ['Manager', selectedEmp.managerName], ['Joined', selectedEmp.joiningDate],
                      ['Engagement', `${selectedEmp.engagementScore}%`], ['Task Completion', `${selectedEmp.taskCompletion}%`],
                      ['Onboarding', `${selectedEmp.onboardingProgress}%`], ['Sentiment', `${Math.round((selectedEmp.sentimentScore||0.5)*100)}%`],
                    ].map(([k,v]) => (
                      <div key={k} className="p-3 rounded-xl" style={{ background:'var(--bg)' }}>
                        <p className="text-xs" style={{ color:'var(--text-muted)' }}>{k}</p>
                        <p className="font-semibold text-sm mt-0.5" style={{ color:'var(--text)' }}>{v || 'N/A'}</p>
                      </div>
                    ))}
                  </div>

                  <p className="font-semibold mb-2 text-sm" style={{ color:'var(--text)' }}>Documents Status</p>
                  <div className="grid grid-cols-2 gap-2 mb-4">
                    {Object.entries(selectedEmp.documents || {}).map(([doc, uploaded]) => (
                      <div key={doc} className="flex items-center justify-between p-2.5 rounded-lg text-xs"
                           style={{ background:'var(--bg)' }}>
                        <span style={{ color:'var(--text)' }}>{doc.replace(/_/g,' ').toUpperCase()}</span>
                        <div className="flex gap-1.5">
                          <button onClick={() => { handleVerifyDoc(selectedEmp.id, doc, true); setSelEmp(null); }}
                                  className={`px-2 py-1 rounded-md font-medium ${uploaded ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-gray-100 text-gray-500 dark:bg-gray-800 dark:text-gray-400'}`}>
                            {uploaded ? '✅' : 'Verify'}
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>

                  {selectedEmp.lastFeedback && (
                    <div className="p-3 rounded-xl border-l-4 border-indigo-400 mb-4" style={{ background:'var(--bg)' }}>
                      <p className="text-xs font-semibold mb-1" style={{ color:'var(--text-muted)' }}>LAST FEEDBACK</p>
                      <p className="text-sm italic" style={{ color:'var(--text)' }}>"{selectedEmp.lastFeedback}"</p>
                    </div>
                  )}

                  <div className="flex gap-2 flex-wrap">
                    <button onClick={() => handleRemind(selectedEmp.id)} className="btn-primary text-sm py-2 gap-2">
                      <Mail size={14} /> Send Doc Reminder
                    </button>
                    {(selectedEmp.riskScore || 0) >= 50 && (
                      <button onClick={() => handleRiskAlert(selectedEmp.id)} className="text-sm py-2 px-4 rounded-xl font-semibold text-white gap-2 flex items-center" style={{ background:'#ef4444' }}>
                        <AlertTriangle size={14} /> Send Risk Alert
                      </button>
                    )}
                    <button onClick={() => { setMeetForm(f => ({ ...f, participantId: selectedEmp.id })); setShowMeetModal(true); setSelEmp(null); }}
                            className="btn-ghost text-sm py-2">
                      <Calendar size={14} /> Schedule Meeting
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ── Document Verification ─────────────────────────────── */}
        {tab === 'documents' && (
          <div className="fade-in max-w-5xl space-y-4">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Document Verification</h2>
            <div className="space-y-4">
              {employees.filter(e => Object.values(e.documents||{}).some(v=>!v)).map(emp => {
                const missing = Object.entries(emp.documents||{}).filter(([,v])=>!v).map(([k])=>k);
                return (
                  <div key={emp.id} className="card p-5">
                    <div className="flex items-center gap-3 mb-3">
                      <img src={emp.avatar} alt="" className="w-10 h-10 rounded-full" />
                      <div className="flex-1">
                        <p className="font-bold" style={{ color:'var(--text)' }}>{emp.name}</p>
                        <p className="text-xs" style={{ color:'var(--text-muted)' }}>{emp.department} • {emp.email}</p>
                      </div>
                      <span className="badge badge-danger">{missing.length} missing</span>
                      <button onClick={() => handleRemind(emp.id)} className="btn-primary text-xs py-1.5">
                        <Mail size={12} /> Remind
                      </button>
                    </div>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                      {Object.entries(emp.documents||{}).map(([doc,uploaded]) => (
                        <div key={doc} className={`flex items-center justify-between p-2.5 rounded-xl text-xs border
                          ${uploaded ? 'border-emerald-300/40 bg-emerald-50/50 dark:bg-emerald-900/10' :
                                       'border-red-300/40 bg-red-50/50 dark:bg-red-900/10'}`}>
                          <div className="flex items-center gap-1.5">
                            {uploaded ? <CheckCircle2 size={12} className="text-emerald-500" /> :
                                        <XCircle size={12} className="text-red-500" />}
                            <span style={{ color:'var(--text)' }}>{doc.replace(/_/g,' ')}</span>
                          </div>
                          {!uploaded && (
                            <button onClick={() => handleVerifyDoc(emp.id, doc, true)}
                                    className="text-xs px-2 py-0.5 rounded-lg bg-emerald-500 text-white font-semibold">
                              Verify
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* ── Risk Analysis ────────────────────────────────────── */}
        {tab === 'risk' && (
          <div className="fade-in max-w-5xl space-y-4">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Risk Analysis</h2>
            {employees.filter(e=>(e.riskScore||0)>=30).sort((a,b)=>(b.riskScore||0)-(a.riskScore||0)).map(emp => {
              const rs = emp.riskScore || 0;
              const level = rs>=75?'Critical':rs>=50?'High':rs>=30?'Medium':'Low';
              return (
                <div key={emp.id} className={`card p-5 border-l-4 ${rs>=75?'border-red-500':rs>=50?'border-orange-500':'border-yellow-500'}`}>
                  <div className="flex items-center gap-4 flex-wrap">
                    <img src={emp.avatar} alt="" className="w-10 h-10 rounded-full" />
                    <div className="flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-bold" style={{ color:'var(--text)' }}>{emp.name}</p>
                        <RiskBadge score={rs} />
                      </div>
                      <p className="text-xs mt-0.5" style={{ color:'var(--text-muted)' }}>
                        {emp.department} • Engagement: {emp.engagementScore}% • Tasks: {emp.taskCompletion}%
                      </p>
                      {emp.lastFeedback && (
                        <p className="text-xs mt-1 italic" style={{ color:'var(--text-muted)' }}>
                          "{emp.lastFeedback.substring(0,100)}..."
                        </p>
                      )}
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => handleRiskAlert(emp.id)}
                              className="text-xs px-3 py-1.5 rounded-xl font-semibold text-white flex items-center gap-1"
                              style={{ background:'#ef4444' }}>
                        <AlertTriangle size={12} /> Alert HR
                      </button>
                      <button onClick={() => { setMeetForm(f=>({...f,participantId:emp.id})); setShowMeetModal(true); }}
                              className="btn-ghost text-xs">
                        <Calendar size={12} /> Meet
                      </button>
                    </div>
                  </div>
                  <div className="mt-3">
                    <div className="risk-bar">
                      <div className="risk-fill transition-all duration-1000"
                           style={{ width:`${rs}%`, background: rs>=75?'#ef4444':rs>=50?'#f97316':'#f59e0b' }} />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* ── Meetings ─────────────────────────────────────────── */}
        {tab === 'meetings' && (
          <div className="fade-in max-w-4xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Meetings</h2>
              <button onClick={() => setShowMeetModal(true)} className="btn-primary">+ Schedule Meeting</button>
            </div>
            {meetings.length === 0
              ? <div className="card p-10 text-center" style={{ color:'var(--text-muted)' }}>
                  <Calendar size={40} className="mx-auto mb-3 opacity-40" /><p>No meetings yet.</p>
                </div>
              : meetings.map(m => (
                  <div key={m.id} className="card p-5">
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                        <Calendar size={22} className="text-blue-500" />
                      </div>
                      <div className="flex-1">
                        <p className="font-bold" style={{ color:'var(--text)' }}>{m.title}</p>
                        <p className="text-sm" style={{ color:'var(--text-muted)' }}>
                          {new Date(m.scheduledAt).toLocaleDateString('en-IN',{weekday:'long',day:'numeric',month:'long',hour:'2-digit',minute:'2-digit'})}
                        </p>
                        <p className="text-sm" style={{ color:'var(--text-muted)' }}>
                          Organizer: {m.organizer} • {m.durationMinutes} min • {m.meetingType?.replace(/_/g,' ')}
                        </p>
                        {m.meetLink && <a href={m.meetLink} target="_blank" rel="noreferrer"
                                          className="text-xs text-indigo-500 hover:underline mt-1 block">🎥 {m.meetLink}</a>}
                      </div>
                      <span className={`badge ${m.status==='COMPLETED'?'badge-success':'badge-info'}`}>{m.status}</span>
                    </div>
                  </div>
                ))
            }
          </div>
        )}

        {/* ── Feedback ─────────────────────────────────────────── */}
        {tab === 'feedback' && (
          <div className="fade-in max-w-4xl space-y-4">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Employee Feedback</h2>
            {feedbacks.map(f => (
              <div key={f.id} className="card p-5">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-9 h-9 rounded-full bg-indigo-100 dark:bg-indigo-900/30 flex items-center justify-center font-bold text-indigo-600 dark:text-indigo-400">
                    {f.userName?.charAt(0)}
                  </div>
                  <div>
                    <p className="font-semibold text-sm" style={{ color:'var(--text)' }}>{f.userName}</p>
                    <p className="text-xs" style={{ color:'var(--text-muted)' }}>
                      {f.category} • {'⭐'.repeat(f.rating)} • {new Date(f.createdAt).toLocaleDateString('en-IN')}
                    </p>
                  </div>
                  <span className={`badge ml-auto ${f.sentimentLabel==='Positive'?'badge-success':f.sentimentLabel==='Negative'?'badge-danger':'badge-info'}`}>
                    {f.sentimentLabel || 'Neutral'}
                  </span>
                </div>
                <p className="text-sm" style={{ color:'var(--text)' }}>{f.content}</p>
              </div>
            ))}
          </div>
        )}

        {/* ── AI Analytics ─────────────────────────────────────── */}
        {tab === 'analytics' && (
          <div className="fade-in max-w-5xl space-y-6">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>AI Analytics</h2>
            {aiData ? (
              <>
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                  {[
                    { label:'Total Analyzed',   value:aiData.summary?.total_employees,     color:'#6366f1' },
                    { label:'Completion Rate',   value:`${aiData.summary?.completion_rate}%`, color:'#10b981' },
                    { label:'Avg Engagement',    value:`${aiData.summary?.avg_engagement}%`,  color:'#f59e0b' },
                    { label:'High Risk',         value:aiData.summary?.high_risk_count,     color:'#ef4444' },
                  ].map(s => (
                    <div key={s.label} className="stat-card">
                      <p className="text-xs" style={{ color:'var(--text-muted)' }}>{s.label}</p>
                      <p className="text-2xl font-bold mt-1" style={{ color:s.color }}>{s.value}</p>
                    </div>
                  ))}
                </div>
                <div className="grid lg:grid-cols-2 gap-6">
                  <div className="card p-5">
                    <p className="section-title">AI Risk Distribution</p>
                    <ResponsiveContainer width="100%" height={200}>
                      <PieChart>
                        <Pie data={Object.entries(aiData.risk_distribution||{}).map(([n,v])=>({name:n,value:v,color:RISK_COLORS[n]}))}
                             cx="50%" cy="50%" outerRadius={80} dataKey="value">
                          {Object.keys(aiData.risk_distribution||{}).map((k,i) =>
                            <Cell key={i} fill={RISK_COLORS[k]} />)}
                        </Pie>
                        <Tooltip /><Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                  <div className="card p-5">
                    <p className="section-title">Sentiment Distribution</p>
                    <ResponsiveContainer width="100%" height={200}>
                      <BarChart data={Object.entries(aiData.sentiment_distribution||{}).map(([n,v])=>({name:n,value:v}))} barSize={30}>
                        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                        <XAxis dataKey="name" tick={{ fill:'var(--text-muted)', fontSize:10 }} />
                        <YAxis tick={{ fill:'var(--text-muted)', fontSize:10 }} />
                        <Tooltip contentStyle={{ background:'var(--bg-card)', border:'1px solid var(--border)', borderRadius:10 }} />
                        <Bar dataKey="value" fill="#6366f1" radius={[4,4,0,0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </>
            ) : (
              <div className="card p-10 text-center" style={{ color:'var(--text-muted)' }}>
                <p>AI service not reachable. Start ai-service/app.py and refresh.</p>
              </div>
            )}
          </div>
        )}

        {/* ── Schedule Meeting Modal ────────────────────────────── */}
        {showMeetModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
            <form onSubmit={handleScheduleMeeting} className="card p-6 w-full max-w-lg slide-up space-y-4">
              <h3 className="text-lg font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Schedule Meeting</h3>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Employee *</label>
                <select value={meetForm.participantId} onChange={e=>setMeetForm(f=>({...f,participantId:e.target.value}))}
                        className="input-field" required>
                  <option value="">Select employee</option>
                  {employees.map(e => <option key={e.id} value={e.id}>{e.name} — {e.department}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Title *</label>
                <input value={meetForm.title} onChange={e=>setMeetForm(f=>({...f,title:e.target.value}))}
                       className="input-field" placeholder="e.g. 30-Day Check-in" required />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Date & Time *</label>
                  <input type="datetime-local" value={meetForm.scheduledAt}
                         onChange={e=>setMeetForm(f=>({...f,scheduledAt:e.target.value}))}
                         className="input-field" required />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Duration (min)</label>
                  <select value={meetForm.durationMinutes} onChange={e=>setMeetForm(f=>({...f,durationMinutes:e.target.value}))} className="input-field">
                    {[15,30,45,60,90].map(d=><option key={d} value={d}>{d} min</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Meeting Type</label>
                <select value={meetForm.meetingType} onChange={e=>setMeetForm(f=>({...f,meetingType:e.target.value}))} className="input-field">
                  {['ONE_ON_ONE','TEAM','TRAINING','ORIENTATION'].map(t=><option key={t} value={t}>{t.replace(/_/g,' ')}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Meet Link</label>
                <input value={meetForm.meetLink} onChange={e=>setMeetForm(f=>({...f,meetLink:e.target.value}))}
                       className="input-field" placeholder="https://meet.google.com/..." />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Description</label>
                <textarea value={meetForm.description} onChange={e=>setMeetForm(f=>({...f,description:e.target.value}))}
                          rows={2} className="input-field resize-none" />
              </div>
              <div className="flex gap-3">
                <button type="submit" className="btn-primary flex-1 justify-center">Schedule & Notify</button>
                <button type="button" onClick={() => setShowMeetModal(false)} className="btn-ghost flex-1 justify-center">Cancel</button>
              </div>
            </form>
          </div>
        )}
      </main>
      <Chatbot />
    </div>
  );
}

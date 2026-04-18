import { useState, useEffect } from 'react';
import Sidebar from '../components/shared/Sidebar';
import Chatbot from '../components/chatbot/Chatbot';
import { managerApi } from '../services/api';
import {
  LayoutDashboard, Users, Calendar, MessageSquare, AlertTriangle,
  TrendingUp, CheckCircle2, Clock, Eye, Plus
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis
} from 'recharts';
import toast from 'react-hot-toast';

const NAV = [
  { id:'dashboard', label:'Dashboard',   icon:LayoutDashboard },
  { id:'team',      label:'My Team',     icon:Users },
  { id:'meetings',  label:'Meetings',    icon:Calendar },
  { id:'risk',      label:'Risk Monitor',icon:AlertTriangle },
  { id:'feedback',  label:'Team Feedback',icon:MessageSquare },
];

function RiskBadge({ score }) {
  const [level,cls] = score >= 75 ? ['Critical','badge-danger'] :
                      score >= 50 ? ['High',    'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400'] :
                      score >= 30 ? ['Medium',  'badge-warning'] :
                                    ['Low',     'badge-success'];
  return <span className={`badge ${cls}`}>{level} ({score})</span>;
}

export default function ManagerDashboard() {
  const [tab, setTab]       = useState('dashboard');
  const [data, setData]     = useState(null);
  const [team, setTeam]     = useState([]);
  const [meetings, setMeetings] = useState([]);
  const [feedbacks, setFeedbacks] = useState([]);
  const [highRisk, setHighRisk]  = useState([]);
  const [loading, setLoading]    = useState(true);
  const [selMember, setSelMember]= useState(null);
  const [showMeetModal, setShowMeetModal] = useState(false);
  const [meetForm, setMeetForm]  = useState({ title:'', scheduledAt:'', durationMinutes:30, meetingType:'ONE_ON_ONE', participantId:'', meetLink:'', description:'' });

  const load = async () => {
    try {
      const [d, t, m, f, hr] = await Promise.all([
        managerApi.dashboard(), managerApi.team(), managerApi.meetings(),
        managerApi.teamFeedback(), managerApi.highRisk()
      ]);
      setData(d.data);
      setTeam(t.data);
      setMeetings(m.data);
      setFeedbacks(f.data);
      setHighRisk(hr.data);
    } catch { toast.error('Failed to load manager dashboard'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleMeeting = async (e) => {
    e.preventDefault();
    if (!meetForm.participantId || !meetForm.scheduledAt || !meetForm.title) {
      toast.error('Fill all required fields'); return;
    }
    try {
      await managerApi.scheduleMeeting({ ...meetForm, durationMinutes: Number(meetForm.durationMinutes) });
      toast.success('Meeting scheduled & email sent! 📅');
      setShowMeetModal(false);
      const m = await managerApi.meetings(); setMeetings(m.data);
    } catch { toast.error('Failed to schedule meeting'); }
  };

  const completeMeeting = async (id) => {
    try { await managerApi.completeMeeting(id); toast.success('Meeting completed!'); await load(); }
    catch { toast.error('Failed to update meeting'); }
  };

  if (loading) return (
    <div className="flex items-center justify-center h-screen" style={{ background:'var(--bg)' }}>
      <div className="text-center">
        <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p style={{ color:'var(--text-muted)' }}>Loading manager dashboard…</p>
      </div>
    </div>
  );

  const ts = data?.teamStats || {};
  const rd = data?.riskDistribution || {};

  const radarData = team.slice(0,6).map(m => ({
    name: m.name?.split(' ')[0],
    engagement: m.engagementScore || 0,
    tasks: m.taskCompletion || 0,
    progress: m.onboardingProgress || 0,
  }));

  const riskData = Object.entries(rd).map(([name, value]) => ({ name, value: Number(value) }));

  return (
    <div className="flex min-h-screen" style={{ background:'var(--bg)' }}>
      <Sidebar links={NAV} activeTab={tab} onTabChange={setTab} />
      <main className="flex-1 lg:ml-60 p-6 pb-20">

        {/* ── Dashboard ────────────────────────────────────────── */}
        {tab === 'dashboard' && (
          <div className="fade-in space-y-6 max-w-6xl">
            <h1 className="text-2xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>
              Manager Dashboard
            </h1>

            {/* Stats */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              {[
                { label:'Team Size',       value:ts.totalTeamSize||0,            icon:Users,       color:'#6366f1', sub:`${ts.onboardingDone} completed` },
                { label:'Completion Rate', value:`${ts.completionRate||0}%`,      icon:CheckCircle2,color:'#10b981', sub:`${ts.onboardingPending} pending` },
                { label:'High Risk',       value:ts.highRiskCount||0,            icon:AlertTriangle,color:'#ef4444', sub:'Need attention' },
                { label:'Avg Engagement',  value:`${ts.avgEngagement||0}%`,       icon:TrendingUp,  color:'#f59e0b', sub:`Avg tasks: ${ts.avgTaskCompletion||0}%` },
              ].map(s => (
                <div key={s.label} className="stat-card">
                  <div className="flex items-center justify-between">
                    <p className="text-xs font-medium" style={{ color:'var(--text-muted)' }}>{s.label}</p>
                    <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background:`${s.color}18` }}>
                      <s.icon size={16} style={{ color:s.color }} />
                    </div>
                  </div>
                  <p className="text-2xl font-bold mt-1" style={{ color:'var(--text)' }}>{s.value}</p>
                  <p className="text-xs" style={{ color:'var(--text-muted)' }}>{s.sub}</p>
                </div>
              ))}
            </div>

            {/* Charts */}
            <div className="grid lg:grid-cols-2 gap-6">
              {radarData.length > 0 && (
                <div className="card p-5">
                  <p className="section-title">Team Performance Radar</p>
                  <ResponsiveContainer width="100%" height={220}>
                    <RadarChart data={radarData}>
                      <PolarGrid stroke="var(--border)" />
                      <PolarAngleAxis dataKey="name" tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                      <PolarRadiusAxis domain={[0,100]} tick={{ fill:'var(--text-muted)', fontSize:9 }} />
                      <Radar name="Engagement" dataKey="engagement" stroke="#6366f1" fill="rgba(99,102,241,.2)" />
                      <Radar name="Tasks"      dataKey="tasks"      stroke="#10b981" fill="rgba(16,185,129,.15)" />
                      <Tooltip contentStyle={{ background:'var(--bg-card)', border:'1px solid var(--border)', borderRadius:10 }} />
                    </RadarChart>
                  </ResponsiveContainer>
                </div>
              )}

              <div className="card p-5">
                <p className="section-title">Team Risk Distribution</p>
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={riskData} barSize={32}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="name" tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                    <YAxis tick={{ fill:'var(--text-muted)', fontSize:11 }} />
                    <Tooltip contentStyle={{ background:'var(--bg-card)', border:'1px solid var(--border)', borderRadius:10 }} />
                    <Bar dataKey="value" radius={[6,6,0,0]} fill="#6366f1" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Upcoming meetings */}
            {meetings.filter(m=>m.status==='SCHEDULED').length > 0 && (
              <div className="card p-5">
                <div className="flex items-center justify-between mb-4">
                  <p className="section-title mb-0">Upcoming Meetings</p>
                  <button onClick={() => setShowMeetModal(true)} className="btn-primary text-sm py-2">+ Schedule</button>
                </div>
                <div className="space-y-3">
                  {meetings.filter(m=>m.status==='SCHEDULED').slice(0,4).map(m => (
                    <div key={m.id} className="flex items-center gap-4 p-3 rounded-xl" style={{ background:'var(--bg)' }}>
                      <div className="w-10 h-10 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                        <Calendar size={18} className="text-blue-500" />
                      </div>
                      <div className="flex-1">
                        <p className="font-semibold text-sm" style={{ color:'var(--text)' }}>{m.title}</p>
                        <p className="text-xs" style={{ color:'var(--text-muted)' }}>
                          {new Date(m.scheduledAt).toLocaleDateString('en-IN',{day:'numeric',month:'short',hour:'2-digit',minute:'2-digit'})} • {m.durationMinutes} min
                        </p>
                      </div>
                      <button onClick={() => completeMeeting(m.id)} className="text-xs px-3 py-1.5 rounded-lg bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 font-semibold">
                        Done
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ── My Team ──────────────────────────────────────────── */}
        {tab === 'team' && (
          <div className="fade-in max-w-5xl space-y-4">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>
              My Team ({team.length})
            </h2>
            <div className="grid md:grid-cols-2 gap-4">
              {team.map(member => {
                const rs = member.riskScore || 0;
                return (
                  <div key={member.id} className="card p-5 hover:shadow-md transition-all cursor-pointer"
                       onClick={() => setSelMember(member)}>
                    <div className="flex items-start gap-3 mb-3">
                      <img src={member.avatar} alt="" className="w-12 h-12 rounded-2xl" />
                      <div className="flex-1 min-w-0">
                        <p className="font-bold" style={{ color:'var(--text)' }}>{member.name}</p>
                        <p className="text-xs" style={{ color:'var(--text-muted)' }}>{member.employeeCode} • {member.department}</p>
                        <RiskBadge score={rs} />
                      </div>
                      <button onClick={e => { e.stopPropagation(); setMeetForm(f=>({...f,participantId:member.id})); setShowMeetModal(true); }}
                              className="btn-ghost text-xs py-1.5">
                        <Calendar size={12} /> Meet
                      </button>
                    </div>
                    {/* Progress bars */}
                    {[
                      { label:'Onboarding Progress', value:member.onboardingProgress||0, color:'#6366f1' },
                      { label:'Engagement',           value:member.engagementScore||0,    color:'#10b981' },
                      { label:'Task Completion',      value:member.taskCompletion||0,     color:'#f59e0b' },
                    ].map(b => (
                      <div key={b.label} className="mb-2">
                        <div className="flex justify-between text-xs mb-1" style={{ color:'var(--text-muted)' }}>
                          <span>{b.label}</span><span className="font-semibold" style={{ color:'var(--text)' }}>{b.value}%</span>
                        </div>
                        <div className="risk-bar">
                          <div className="risk-fill" style={{ width:`${b.value}%`, background:b.color }} />
                        </div>
                      </div>
                    ))}
                    {member.onboardingComplete && (
                      <div className="mt-2 flex items-center gap-1 text-emerald-500 text-xs font-semibold">
                        <CheckCircle2 size={12} /> Onboarding Complete
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

            {/* Team member detail modal */}
            {selMember && (
              <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                <div className="card p-6 w-full max-w-xl max-h-[85vh] overflow-y-auto slide-up">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <img src={selMember.avatar} alt="" className="w-14 h-14 rounded-2xl" />
                      <div>
                        <h3 className="font-bold text-lg" style={{ color:'var(--text)' }}>{selMember.name}</h3>
                        <p className="text-sm" style={{ color:'var(--text-muted)' }}>{selMember.department} • {selMember.location}</p>
                        <RiskBadge score={selMember.riskScore || 0} />
                      </div>
                    </div>
                    <button onClick={() => setSelMember(null)} className="btn-ghost text-xs">Close</button>
                  </div>
                  <div className="grid grid-cols-2 gap-3 mb-4">
                    {[
                      ['Email', selMember.email], ['Joining Date', selMember.joiningDate],
                      ['Experience', `${selMember.experienceYears} years`], ['Employee Code', selMember.employeeCode],
                      ['Engagement', `${selMember.engagementScore}%`], ['Task Completion', `${selMember.taskCompletion}%`],
                      ['Risk Score', selMember.riskScore], ['Sentiment', `${Math.round((selMember.sentimentScore||0.5)*100)}%`],
                    ].map(([k,v]) => (
                      <div key={k} className="p-3 rounded-xl" style={{ background:'var(--bg)' }}>
                        <p className="text-xs" style={{ color:'var(--text-muted)' }}>{k}</p>
                        <p className="font-semibold text-sm mt-0.5" style={{ color:'var(--text)' }}>{v||'N/A'}</p>
                      </div>
                    ))}
                  </div>
                  <p className="font-semibold text-sm mb-2" style={{ color:'var(--text)' }}>Document Status</p>
                  <div className="grid grid-cols-3 gap-2 mb-4">
                    {Object.entries(selMember.documents||{}).map(([doc,ok]) => (
                      <div key={doc} className={`flex items-center gap-1.5 p-2 rounded-lg text-xs
                        ${ok?'bg-emerald-50 dark:bg-emerald-900/10 text-emerald-700 dark:text-emerald-400':'bg-red-50 dark:bg-red-900/10 text-red-600 dark:text-red-400'}`}>
                        {ok ? <CheckCircle2 size={10}/> : <AlertTriangle size={10}/>}
                        {doc.replace(/_/g,' ')}
                      </div>
                    ))}
                  </div>
                  {selMember.lastFeedback && (
                    <div className="p-3 rounded-xl border-l-4 border-indigo-400 mb-4" style={{ background:'var(--bg)' }}>
                      <p className="text-xs font-semibold mb-1" style={{ color:'var(--text-muted)' }}>EMPLOYEE FEEDBACK</p>
                      <p className="text-sm italic" style={{ color:'var(--text)' }}>"{selMember.lastFeedback}"</p>
                    </div>
                  )}
                  <button onClick={() => { setMeetForm(f=>({...f,participantId:selMember.id})); setShowMeetModal(true); setSelMember(null); }}
                          className="btn-primary w-full justify-center">
                    <Calendar size={15} /> Schedule 1:1 Meeting
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ── Meetings ─────────────────────────────────────────── */}
        {tab === 'meetings' && (
          <div className="fade-in max-w-4xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Meetings</h2>
              <button onClick={() => setShowMeetModal(true)} className="btn-primary">+ Schedule</button>
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
                        <div className="flex items-center gap-2">
                          <p className="font-bold" style={{ color:'var(--text)' }}>{m.title}</p>
                          <span className={`badge ${m.status==='COMPLETED'?'badge-success':'badge-info'}`}>{m.status}</span>
                        </div>
                        <p className="text-sm mt-1" style={{ color:'var(--text-muted)' }}>
                          {new Date(m.scheduledAt).toLocaleDateString('en-IN',{weekday:'short',day:'numeric',month:'long',hour:'2-digit',minute:'2-digit'})}
                          {' '}• {m.durationMinutes} min
                        </p>
                        {m.meetLink && <a href={m.meetLink} target="_blank" rel="noreferrer"
                                          className="text-xs text-indigo-500 hover:underline">🎥 Join Meeting</a>}
                      </div>
                      {m.status === 'SCHEDULED' && (
                        <button onClick={() => completeMeeting(m.id)} className="btn-ghost text-xs py-1.5">Mark Done</button>
                      )}
                    </div>
                  </div>
                ))
            }
          </div>
        )}

        {/* ── Risk Monitor ────────────────────────────────────── */}
        {tab === 'risk' && (
          <div className="fade-in max-w-4xl space-y-4">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Risk Monitor</h2>
            {highRisk.length === 0
              ? <div className="card p-10 text-center">
                  <CheckCircle2 size={40} className="mx-auto mb-3 text-emerald-500" />
                  <p className="font-semibold text-emerald-600">Great news! No high-risk team members.</p>
                </div>
              : highRisk.map(emp => {
                  const rs = emp.riskScore || 0;
                  return (
                    <div key={emp.id} className={`card p-5 border-l-4 ${rs>=75?'border-red-500':rs>=50?'border-orange-500':'border-yellow-500'}`}>
                      <div className="flex items-center gap-4 flex-wrap">
                        <img src={emp.avatar} alt="" className="w-10 h-10 rounded-full" />
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <p className="font-bold" style={{ color:'var(--text)' }}>{emp.name}</p>
                            <RiskBadge score={rs} />
                          </div>
                          <p className="text-xs mt-0.5" style={{ color:'var(--text-muted)' }}>
                            Engagement: {emp.engagementScore}% • Tasks: {emp.taskCompletion}% • Progress: {emp.onboardingProgress}%
                          </p>
                          {emp.lastFeedback && (
                            <p className="text-xs mt-1 italic" style={{ color:'var(--text-muted)' }}>
                              "{emp.lastFeedback.substring(0,120)}…"
                            </p>
                          )}
                        </div>
                        <button onClick={() => { setMeetForm(f=>({...f,participantId:emp.id})); setShowMeetModal(true); }}
                                className="btn-primary text-sm py-2">
                          <Calendar size={14} /> Schedule 1:1
                        </button>
                      </div>
                      <div className="mt-3">
                        <div className="risk-bar">
                          <div className="risk-fill" style={{ width:`${rs}%`, background:rs>=75?'#ef4444':rs>=50?'#f97316':'#f59e0b' }} />
                        </div>
                      </div>
                    </div>
                  );
                })
            }
          </div>
        )}

        {/* ── Team Feedback ─────────────────────────────────────── */}
        {tab === 'feedback' && (
          <div className="fade-in max-w-3xl space-y-4">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Team Feedback</h2>
            {feedbacks.length === 0
              ? <div className="card p-10 text-center" style={{ color:'var(--text-muted)' }}>
                  <MessageSquare size={40} className="mx-auto mb-3 opacity-40" /><p>No feedback yet.</p>
                </div>
              : feedbacks.map(f => (
                  <div key={f.id} className="card p-5">
                    <div className="flex items-center gap-3 mb-2">
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
                ))
            }
          </div>
        )}

        {/* ── Schedule Meeting Modal ────────────────────────────── */}
        {showMeetModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
            <form onSubmit={handleMeeting} className="card p-6 w-full max-w-lg slide-up space-y-4">
              <h3 className="text-lg font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Schedule Meeting</h3>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Team Member *</label>
                <select value={meetForm.participantId} onChange={e=>setMeetForm(f=>({...f,participantId:e.target.value}))} className="input-field" required>
                  <option value="">Select team member</option>
                  {team.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Title *</label>
                <input value={meetForm.title} onChange={e=>setMeetForm(f=>({...f,title:e.target.value}))} className="input-field" placeholder="e.g. 30-Day 1:1 Check-in" required />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Date & Time *</label>
                  <input type="datetime-local" value={meetForm.scheduledAt}
                         onChange={e=>setMeetForm(f=>({...f,scheduledAt:e.target.value}))} className="input-field" required />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Duration</label>
                  <select value={meetForm.durationMinutes} onChange={e=>setMeetForm(f=>({...f,durationMinutes:e.target.value}))} className="input-field">
                    {[15,30,45,60].map(d=><option key={d} value={d}>{d} min</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Meet Link</label>
                <input value={meetForm.meetLink} onChange={e=>setMeetForm(f=>({...f,meetLink:e.target.value}))} className="input-field" placeholder="https://meet.google.com/..." />
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

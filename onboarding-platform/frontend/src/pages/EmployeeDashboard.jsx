import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/shared/Sidebar';
import Chatbot from '../components/chatbot/Chatbot';
import {
  LayoutDashboard, FileText, CheckSquare, Bell, Calendar,
  MessageSquare, TrendingUp, Upload, CheckCircle2, XCircle,
  Clock, AlertTriangle, User, Award, Target, BookOpen, Users
} from 'lucide-react';
import { employeeApi, aiService } from '../services/api';
import { RadialBarChart, RadialBar, ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import toast from 'react-hot-toast';

const NAV = (unread) => [
  { id:'overview',   label:'Dashboard',    icon:LayoutDashboard },
  { id:'documents',  label:'My Documents', icon:FileText },
  { id:'tasks',      label:'My Tasks',     icon:CheckSquare },
  { id:'meetings',   label:'Meetings',     icon:Calendar },
  { id:'feedback',   label:'Feedback',     icon:MessageSquare },
  { id:'notifications', label:'Notifications', icon:Bell, badge:unread },
];

const DOCS = [
  { key:'pan',              label:'PAN Card',           required:true  },
  { key:'aadhaar',          label:'Aadhaar Card',        required:true  },
  { key:'voter_id',         label:'Voter ID',            required:false },
  { key:'passport',         label:'Passport',            required:false },
  { key:'salary_slip',      label:'Last Salary Slip',    required:true  },
  { key:'offer_letter',     label:'Offer Letter',        required:true  },
  { key:'tenth_cert',       label:'10th Certificate',    required:true  },
  { key:'twelfth_cert',     label:'12th Certificate',    required:true  },
  { key:'degree',           label:'Degree Certificate',  required:true  },
  { key:'experience_letter',label:'Experience Letter',   required:false },
  { key:'relieving_letter', label:'Relieving Letter',    required:false },
  { key:'photo',            label:'Recent Photo',        required:true  },
];

const TASKS = [
  { key:'taskItSetup',        label:'IT Setup & Laptop Configuration',    icon:'💻', points:6 },
  { key:'taskEmailSetup',     label:'Company Email & Slack Setup',         icon:'📧', points:6 },
  { key:'taskBuddyMeet',      label:'Meet Your Buddy / Mentor',            icon:'🤝', points:6 },
  { key:'taskTeamIntro',      label:'Team Introduction Session',           icon:'👥', points:6 },
  { key:'taskHrOrientation',  label:'HR Orientation & Policy Review',      icon:'📋', points:6 },
  { key:'taskPoliciesRead',   label:'Read Company Policies & Handbook',    icon:'📖', points:6 },
  { key:'taskFirstProject',   label:'First Project Assignment',            icon:'🚀', points:6 },
  { key:'taskTraining1',      label:'Role-Specific Technical Training',    icon:'🎓', points:6 },
  { key:'taskTraining2',      label:'Compliance & Legal Training',         icon:'⚖️', points:6 },
  { key:'taskTraining3',      label:'Leadership & Culture Workshop',       icon:'🌟', points:6 },
];

function ProgressRing({ value, size = 120, stroke = 10, color = '#6366f1' }) {
  const r = (size - stroke) / 2;
  const circ = 2 * Math.PI * r;
  const dash = circ - (value / 100) * circ;
  return (
    <svg width={size} height={size} className="-rotate-90">
      <circle cx={size/2} cy={size/2} r={r} stroke="var(--border)" strokeWidth={stroke} fill="none" />
      <circle cx={size/2} cy={size/2} r={r} stroke={color} strokeWidth={stroke} fill="none"
              strokeDasharray={circ} strokeDashoffset={dash} strokeLinecap="round"
              style={{ transition:'stroke-dashoffset 1s ease-in-out' }} />
    </svg>
  );
}

function RiskBadge({ score }) {
  const level = score >= 75 ? ['Critical','bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'] :
                score >= 50 ? ['High',    'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400'] :
                score >= 30 ? ['Medium',  'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400'] :
                              ['Low',     'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400'];
  return <span className={`badge ${level[1]}`}>Risk: {level[0]} ({score})</span>;
}

export default function EmployeeDashboard() {
  const { user }       = useAuth();
  const [tab, setTab]  = useState('overview');
  const [data, setData]= useState(null);
  const [notifs, setNotifs] = useState([]);
  const [meetings, setMeetings] = useState([]);
  const [feedbacks, setFeedbacks] = useState([]);
  const [uploading, setUploading] = useState({});
  const [fbText, setFbText] = useState('');
  const [fbRating, setFbRating] = useState(4);
  const [fbCat, setFbCat] = useState('ONBOARDING');
  const [loading, setLoading] = useState(true);
  const [unread, setUnread] = useState(0);

  const load = async () => {
    try {
      const [d, n, m, f] = await Promise.all([
        employeeApi.dashboard(),
        employeeApi.getNotifications(),
        employeeApi.getMeetings(),
        employeeApi.getFeedback(),
      ]);
      setData(d.data);
      setNotifs(n.data.notifications || []);
      setUnread(n.data.unreadCount || 0);
      setMeetings(m.data || []);
      setFeedbacks(f.data || []);
    } catch (e) {
      toast.error('Failed to load dashboard data');
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleUpload = async (docKey, file) => {
    setUploading(u => ({ ...u, [docKey]: true }));
    try {
      await employeeApi.uploadDoc(docKey, file);
      toast.success(`${DOCS.find(d=>d.key===docKey)?.label} uploaded! ✅`);
      await load();
    } catch { toast.error('Upload failed. Please try again.'); }
    finally { setUploading(u => ({ ...u, [docKey]: false })); }
  };

  const handleTask = async (taskKey, completed) => {
    try {
      await employeeApi.updateTask(taskKey, completed);
      toast.success(completed ? 'Task completed! ✅' : 'Task marked incomplete');
      await load();
    } catch { toast.error('Failed to update task'); }
  };

  const handleFeedback = async (e) => {
    e.preventDefault();
    if (!fbText.trim()) { toast.error('Please write your feedback'); return; }
    try {
      await employeeApi.submitFeedback({ content: fbText, rating: fbRating, category: fbCat });
      toast.success('Feedback submitted! Thank you 🙏');
      setFbText('');
      setFbRating(4);
      await load();
    } catch { toast.error('Failed to submit feedback'); }
  };

  const markRead = async () => {
    await employeeApi.markAllRead();
    setUnread(0);
    setNotifs(n => n.map(x => ({ ...x, isRead: true })));
  };

  if (loading) return (
    <div className="flex items-center justify-center h-screen" style={{ background:'var(--bg)' }}>
      <div className="text-center">
        <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p style={{ color:'var(--text-muted)' }}>Loading your dashboard…</p>
      </div>
    </div>
  );

  const profile = data?.profile || {};
  const stats   = data?.stats   || {};
  const docs    = profile.documents || {};
  const tasks   = profile.tasks    || {};
  const progress = profile.onboardingProgress || 0;

  const engData = [
    { name:'Engagement', value: profile.engagementScore || 0, fill:'#6366f1' },
    { name:'Tasks',      value: profile.taskCompletion  || 0, fill:'#10b981' },
    { name:'Progress',   value: progress,                       fill:'#f59e0b' },
  ];

  return (
    <div className="flex min-h-screen" style={{ background:'var(--bg)' }}>
      <Sidebar links={NAV(unread)} activeTab={tab} onTabChange={setTab} notifications={unread} />
      <main className="flex-1 lg:ml-60 p-6 pb-20">

        {/* ── Overview ──────────────────────────────────────────── */}
        {tab === 'overview' && (
          <div className="fade-in space-y-6 max-w-6xl">
            {/* Header */}
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>
                  Welcome back, {profile.name?.split(' ')[0]}! 👋
                </h1>
                <p style={{ color:'var(--text-muted)' }} className="text-sm mt-1">
                  {profile.employeeCode} • {profile.department} • Joined {profile.joiningDate}
                </p>
              </div>
              <RiskBadge score={profile.riskScore || 0} />
            </div>

            {/* Stats row */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              {[
                { label:'Onboarding Progress', value:`${progress}%`, icon:TrendingUp, color:'#6366f1', sub: progress >= 90 ? '✅ Complete!' : `${100-progress}% remaining` },
                { label:'Documents Uploaded',  value:`${stats.docsUploaded}/${stats.docsTotal}`, icon:FileText, color:'#10b981', sub:'Required documents' },
                { label:'Tasks Completed',     value:`${stats.tasksDone}/${stats.tasksTotal}`,   icon:CheckSquare, color:'#f59e0b', sub:'Onboarding tasks' },
                { label:'Engagement Score',    value:`${profile.engagementScore}%`,             icon:Award, color:'#ec4899', sub: profile.engagementScore > 75 ? '🌟 Excellent' : 'Keep engaging!' },
              ].map(s => (
                <div key={s.label} className="stat-card">
                  <div className="flex items-center justify-between">
                    <p className="text-xs font-medium" style={{ color:'var(--text-muted)' }}>{s.label}</p>
                    <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background:`${s.color}18` }}>
                      <s.icon size={16} style={{ color:s.color }} />
                    </div>
                  </div>
                  <p className="text-2xl font-bold" style={{ color:'var(--text)' }}>{s.value}</p>
                  <p className="text-xs" style={{ color:'var(--text-muted)' }}>{s.sub}</p>
                </div>
              ))}
            </div>

            {/* Progress ring + chart */}
            <div className="grid lg:grid-cols-3 gap-6">
              <div className="card p-6 flex flex-col items-center justify-center gap-4">
                <div className="relative">
                  <ProgressRing value={progress} size={140} stroke={12}
                    color={progress >= 90 ? '#10b981' : progress >= 60 ? '#f59e0b' : '#6366f1'} />
                  <div className="absolute inset-0 flex flex-col items-center justify-center">
                    <span className="text-3xl font-bold" style={{ color:'var(--text)' }}>{progress}%</span>
                    <span className="text-xs" style={{ color:'var(--text-muted)' }}>Complete</span>
                  </div>
                </div>
                <div className="text-center">
                  <p className="font-semibold" style={{ color:'var(--text)' }}>Onboarding Progress</p>
                  <p className="text-xs mt-1" style={{ color:'var(--text-muted)' }}>
                    {progress >= 90 ? '🎉 Onboarding complete! Great work!' :
                     `Upload documents & complete tasks to reach 100%`}
                  </p>
                </div>
              </div>

              <div className="card p-5 lg:col-span-2">
                <p className="section-title">Performance Overview</p>
                <ResponsiveContainer width="100%" height={160}>
                  <RadialBarChart cx="50%" cy="50%" innerRadius={30} outerRadius={100}
                                  data={engData} startAngle={90} endAngle={-270}>
                    <RadialBar dataKey="value" cornerRadius={4} />
                    <Tooltip formatter={(v) => `${v}%`} />
                  </RadialBarChart>
                </ResponsiveContainer>
                <div className="flex gap-4 mt-2">
                  {engData.map(d => (
                    <div key={d.name} className="flex items-center gap-1.5 text-xs" style={{ color:'var(--text-muted)' }}>
                      <span className="w-2.5 h-2.5 rounded-full" style={{ background:d.fill }} />
                      {d.name}: <strong style={{ color:'var(--text)' }}>{d.value}%</strong>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Quick actions */}
            <div className="card p-5">
              <p className="section-title">Quick Actions</p>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {[
                  { label:'Upload Documents', icon:Upload, tab:'documents', color:'#6366f1' },
                  { label:'Complete Tasks', icon:CheckSquare, tab:'tasks', color:'#10b981' },
                  { label:'View Meetings', icon:Calendar, tab:'meetings', color:'#3b82f6' },
                  { label:'Give Feedback', icon:MessageSquare, tab:'feedback', color:'#ec4899' },
                ].map(a => (
                  <button key={a.tab} onClick={() => setTab(a.tab)}
                          className="flex flex-col items-center gap-2 p-4 rounded-xl border transition-all hover:scale-105"
                          style={{ borderColor:'var(--border)', background:'var(--bg)' }}>
                    <div className="w-10 h-10 rounded-xl flex items-center justify-center"
                         style={{ background:`${a.color}15` }}>
                      <a.icon size={20} style={{ color:a.color }} />
                    </div>
                    <span className="text-xs font-medium text-center" style={{ color:'var(--text)' }}>{a.label}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Upcoming meetings */}
            {meetings.filter(m => m.status === 'SCHEDULED').length > 0 && (
              <div className="card p-5">
                <p className="section-title">Upcoming Meetings</p>
                <div className="space-y-3">
                  {meetings.filter(m => m.status === 'SCHEDULED').slice(0,3).map(m => (
                    <div key={m.id} className="flex items-center gap-4 p-3 rounded-xl"
                         style={{ background:'var(--bg)' }}>
                      <div className="w-10 h-10 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                        <Calendar size={18} className="text-blue-600 dark:text-blue-400" />
                      </div>
                      <div className="flex-1">
                        <p className="font-semibold text-sm" style={{ color:'var(--text)' }}>{m.title}</p>
                        <p className="text-xs" style={{ color:'var(--text-muted)' }}>
                          {m.organizer} • {new Date(m.scheduledAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' })}
                        </p>
                      </div>
                      {m.meetLink && (
                        <a href={m.meetLink} target="_blank" rel="noreferrer"
                           className="text-xs px-3 py-1.5 rounded-lg text-white"
                           style={{ background:'#3b82f6' }}>Join</a>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ── Documents ─────────────────────────────────────────── */}
        {tab === 'documents' && (
          <div className="fade-in max-w-4xl space-y-6">
            <div>
              <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>My Documents</h2>
              <p className="text-sm mt-1" style={{ color:'var(--text-muted)' }}>
                Upload all required documents to complete your onboarding.
              </p>
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              {DOCS.map(doc => {
                const uploaded = docs[doc.key];
                const busy     = uploading[doc.key];
                return (
                  <div key={doc.key}
                       className={`card p-4 border-2 transition-all ${uploaded ? 'border-emerald-400/40' : doc.required ? 'border-red-300/30' : 'border-transparent'}`}>
                    <div className="flex items-start gap-3">
                      <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0
                        ${uploaded ? 'bg-emerald-100 dark:bg-emerald-900/30' : 'bg-gray-100 dark:bg-gray-800'}`}>
                        {uploaded
                          ? <CheckCircle2 size={20} className="text-emerald-500" />
                          : <FileText size={20} style={{ color:'var(--text-muted)' }} />}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="font-semibold text-sm" style={{ color:'var(--text)' }}>{doc.label}</p>
                          {doc.required && <span className="text-red-500 text-xs">*Required</span>}
                        </div>
                        <p className="text-xs mt-0.5" style={{ color:'var(--text-muted)' }}>
                          {uploaded ? '✅ Uploaded successfully' : 'Not yet uploaded'}
                        </p>
                        <div className="mt-2 flex gap-2">
                          <label className={`cursor-pointer inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all
                            ${uploaded ? 'bg-gray-100 dark:bg-gray-800 text-gray-500' : 'text-white'}`}
                               style={{ background: uploaded ? undefined : 'var(--primary)' }}>
                            <Upload size={12} />
                            {busy ? 'Uploading…' : uploaded ? 'Re-upload' : 'Upload'}
                            <input type="file" className="hidden"
                                   accept=".pdf,.jpg,.jpeg,.png"
                                   onChange={e => e.target.files[0] && handleUpload(doc.key, e.target.files[0])}
                                   disabled={busy} />
                          </label>
                        </div>
                      </div>
                      <div className={`w-2 h-2 rounded-full flex-shrink-0 mt-1 ${uploaded ? 'bg-emerald-400' : 'bg-gray-300'}`} />
                    </div>
                  </div>
                );
              })}
            </div>
            <div className="card p-4 border-l-4 border-amber-400">
              <p className="text-sm font-semibold text-amber-700 dark:text-amber-400">📋 Accepted Formats</p>
              <p className="text-xs mt-1" style={{ color:'var(--text-muted)' }}>
                PDF, JPG, JPEG, PNG • Max 15MB per file • Ensure documents are clear and legible
              </p>
            </div>
          </div>
        )}

        {/* ── Tasks ────────────────────────────────────────────── */}
        {tab === 'tasks' && (
          <div className="fade-in max-w-3xl space-y-6">
            <div>
              <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>My Tasks</h2>
              <p className="text-sm mt-1" style={{ color:'var(--text-muted)' }}>
                Complete all tasks to finish your onboarding journey.
              </p>
            </div>
            <div className="card p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-semibold" style={{ color:'var(--text)' }}>Task Progress</span>
                <span className="text-sm font-bold" style={{ color:'var(--primary)' }}>
                  {Object.values(tasks).filter(Boolean).length}/{Object.values(tasks).length}
                </span>
              </div>
              <div className="risk-bar">
                <div className="risk-fill bg-indigo-500"
                     style={{ width:`${Object.values(tasks).filter(Boolean).length / Object.values(tasks).length * 100}%` }} />
              </div>
            </div>
            <div className="space-y-3">
              {TASKS.map(task => {
                const done = tasks[task.key];
                return (
                  <div key={task.key}
                       className={`card p-4 border-l-4 transition-all ${done ? 'border-emerald-400' : 'border-gray-200 dark:border-gray-700'}`}>
                    <div className="flex items-center gap-4">
                      <span className="text-2xl">{task.icon}</span>
                      <div className="flex-1">
                        <p className={`font-semibold text-sm ${done ? 'line-through' : ''}`}
                           style={{ color: done ? 'var(--text-muted)' : 'var(--text)' }}>{task.label}</p>
                        <p className="text-xs mt-0.5" style={{ color:'var(--text-muted)' }}>
                          +{task.points} progress points
                        </p>
                      </div>
                      <button onClick={() => handleTask(task.key, !done)}
                              className={`w-7 h-7 rounded-full border-2 flex items-center justify-center transition-all
                                ${done ? 'bg-emerald-500 border-emerald-500' : 'border-gray-300 dark:border-gray-600 hover:border-indigo-400'}`}>
                        {done && <CheckCircle2 size={16} className="text-white" />}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* ── Meetings ─────────────────────────────────────────── */}
        {tab === 'meetings' && (
          <div className="fade-in max-w-3xl space-y-6">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>My Meetings</h2>
            {meetings.length === 0
              ? <div className="card p-10 text-center" style={{ color:'var(--text-muted)' }}>
                  <Calendar size={40} className="mx-auto mb-3 opacity-40" />
                  <p>No meetings scheduled yet. Your manager or HR will schedule meetings soon.</p>
                </div>
              : <div className="space-y-4">
                  {meetings.map(m => (
                    <div key={m.id} className="card p-5">
                      <div className="flex items-start gap-4">
                        <div className={`w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0
                          ${m.status==='COMPLETED' ? 'bg-gray-100 dark:bg-gray-800' : 'bg-blue-100 dark:bg-blue-900/30'}`}>
                          <Calendar size={22} className={m.status==='COMPLETED' ? 'text-gray-400' : 'text-blue-500'} />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <p className="font-bold" style={{ color:'var(--text)' }}>{m.title}</p>
                            <span className={`badge ${m.status==='COMPLETED' ? 'badge-success' : m.status==='CANCELLED' ? 'badge-danger' : 'badge-info'}`}>
                              {m.status}
                            </span>
                          </div>
                          <p className="text-sm mt-1" style={{ color:'var(--text-muted)' }}>
                            👤 Organized by {m.organizer} • ⏱ {m.durationMinutes} min
                          </p>
                          <p className="text-sm" style={{ color:'var(--text-muted)' }}>
                            📅 {new Date(m.scheduledAt).toLocaleDateString('en-IN',
                              { weekday:'long', day:'numeric', month:'long', year:'numeric', hour:'2-digit', minute:'2-digit' })}
                          </p>
                          {m.description && <p className="text-sm mt-2" style={{ color:'var(--text)' }}>{m.description}</p>}
                        </div>
                        {m.meetLink && m.status === 'SCHEDULED' && (
                          <a href={m.meetLink} target="_blank" rel="noreferrer" className="btn-primary text-xs py-2 flex-shrink-0">
                            🎥 Join
                          </a>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
            }
          </div>
        )}

        {/* ── Feedback ─────────────────────────────────────────── */}
        {tab === 'feedback' && (
          <div className="fade-in max-w-2xl space-y-6">
            <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>Submit Feedback</h2>
            <div className="card p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color:'var(--text)' }}>Category</label>
                <select value={fbCat} onChange={e => setFbCat(e.target.value)} className="input-field">
                  {['ONBOARDING','TEAM','TOOLS','TRAINING','MANAGER','CULTURE'].map(c =>
                    <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color:'var(--text)' }}>
                  Rating: {['😞','😕','😐','🙂','😊'][fbRating-1]} {fbRating}/5
                </label>
                <input type="range" min={1} max={5} value={fbRating}
                       onChange={e => setFbRating(Number(e.target.value))}
                       className="w-full accent-indigo-500" />
                <div className="flex justify-between text-xs mt-1" style={{ color:'var(--text-muted)' }}>
                  <span>Very Poor</span><span>Excellent</span>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color:'var(--text)' }}>Your Feedback</label>
                <textarea value={fbText} onChange={e => setFbText(e.target.value)}
                          rows={5} placeholder="Share your onboarding experience…"
                          className="input-field resize-none" />
              </div>
              <button onClick={handleFeedback} className="btn-primary w-full justify-center">
                Submit Feedback
              </button>
            </div>
            {feedbacks.length > 0 && (
              <div>
                <p className="section-title">Your Previous Feedback</p>
                <div className="space-y-3">
                  {feedbacks.map(f => (
                    <div key={f.id} className="card p-4">
                      <div className="flex items-center justify-between mb-2">
                        <span className="badge badge-purple">{f.category}</span>
                        <span className="text-xs" style={{ color:'var(--text-muted)' }}>
                          {new Date(f.createdAt).toLocaleDateString('en-IN')}
                        </span>
                      </div>
                      <p className="text-sm" style={{ color:'var(--text)' }}>{f.content}</p>
                      <div className="mt-2 text-xs" style={{ color:'var(--text-muted)' }}>
                        Rating: {'⭐'.repeat(f.rating)} • Sentiment: {f.sentimentLabel || 'Analyzed'}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ── Notifications ─────────────────────────────────────── */}
        {tab === 'notifications' && (
          <div className="fade-in max-w-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>
                Notifications {unread > 0 && <span className="text-base font-normal text-red-500">({unread} unread)</span>}
              </h2>
              {unread > 0 && (
                <button onClick={markRead} className="btn-ghost text-xs">Mark all read</button>
              )}
            </div>
            {notifs.length === 0
              ? <div className="card p-10 text-center" style={{ color:'var(--text-muted)' }}>
                  <Bell size={40} className="mx-auto mb-3 opacity-40" />
                  <p>No notifications yet.</p>
                </div>
              : notifs.map(n => (
                  <div key={n.id}
                       className={`card p-4 border-l-4 ${!n.isRead ? 'border-indigo-400' : 'border-transparent'}`}>
                    <div className="flex gap-3">
                      <div className={`w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 text-sm
                        ${n.type==='SUCCESS' ? 'bg-emerald-100 dark:bg-emerald-900/30' :
                          n.type==='ALERT' || n.type==='WARNING' ? 'bg-red-100 dark:bg-red-900/30' :
                          'bg-blue-100 dark:bg-blue-900/30'}`}>
                        {n.type==='SUCCESS' ? '✅' : n.type==='ALERT' ? '🚨' : n.type==='WARNING' ? '⚠️' : 'ℹ️'}
                      </div>
                      <div className="flex-1">
                        <p className="font-semibold text-sm" style={{ color:'var(--text)' }}>{n.title}</p>
                        <p className="text-xs mt-0.5" style={{ color:'var(--text-muted)' }}>{n.message}</p>
                        <p className="text-xs mt-1" style={{ color:'var(--text-muted)' }}>
                          {new Date(n.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' })}
                        </p>
                      </div>
                      {!n.isRead && <div className="w-2 h-2 rounded-full bg-indigo-500 flex-shrink-0 mt-1" />}
                    </div>
                  </div>
                ))
            }
          </div>
        )}
      </main>
      <Chatbot />
    </div>
  );
}

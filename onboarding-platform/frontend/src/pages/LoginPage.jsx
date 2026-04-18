import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { Moon, Sun, Eye, EyeOff, Building2, Users, Briefcase, UserCheck } from 'lucide-react';
import toast from 'react-hot-toast';

const ROLES = [
  { id: 'employee', label: 'Employee', icon: UserCheck, color: 'from-emerald-500 to-teal-500',
    demo: { email: 'aarav.sharma@nexaworks.in', password: 'Emp@123456' },
    desc: 'Access your onboarding dashboard, upload documents and track tasks' },
  { id: 'hr', label: 'HR Manager', icon: Users, color: 'from-violet-500 to-purple-500',
    demo: { email: 'sunita.rao@nexaworks.in', password: 'HR@123456' },
    desc: 'Manage onboarding, verify documents, view AI risk scores and analytics' },
  { id: 'manager', label: 'Manager', icon: Briefcase, color: 'from-blue-500 to-indigo-500',
    demo: { email: 'vikram.mehta@nexaworks.in', password: 'Mgr@123456' },
    desc: 'Track your team\'s onboarding progress and schedule 1:1 meetings' },
];

export default function LoginPage() {
  const [selectedRole, setSelectedRole] = useState(null);
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [showPwd, setShowPwd]   = useState(false);
  const [loading, setLoading]   = useState(false);
  const { login }     = useAuth();
  const { dark, toggle } = useTheme();
  const navigate      = useNavigate();

  const fillDemo = () => {
    if (!selectedRole) return;
    const r = ROLES.find(r => r.id === selectedRole);
    setEmail(r.demo.email);
    setPassword(r.demo.password);
    toast.success(`Demo credentials filled for ${r.label}!`);
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!selectedRole) { toast.error('Please select your role first.'); return; }
    if (!email || !password) { toast.error('Email and password are required.'); return; }
    setLoading(true);
    try {
      const user = await login(email.trim(), password, selectedRole);
      toast.success(`Welcome back, ${user.name}! 👋`);
      navigate(`/${user.role}/dashboard`, { replace: true });
    } catch (err) {
      const msg = err?.response?.data?.error || 'Login failed. Please check credentials.';
      toast.error(msg);
    } finally { setLoading(false); }
  };

  return (
    <div className={`min-h-screen flex ${dark ? 'dark' : ''}`}
         style={{ background: dark ? 'linear-gradient(135deg,#0b0f1a 0%,#111827 100%)' :
                                      'linear-gradient(135deg,#f0f4ff 0%,#e9d5ff 100%)' }}>

      {/* Left panel */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-12"
           style={{ background: 'linear-gradient(145deg,#4f46e5 0%,#7c3aed 50%,#2563eb 100%)' }}>
        <div>
          <div className="flex items-center gap-3 mb-12">
            <img src="/src/assets/nexaworks-logo.svg" alt="NexaWorks Logo" className="w-10 h-10" />
            <span style={{ fontFamily:'Syne,sans-serif' }}
                  className="text-2xl font-bold text-white">NexaWorks</span>
          </div>
          <h1 className="text-4xl font-bold text-white leading-tight mb-4"
              style={{ fontFamily:'Syne,sans-serif' }}>
            AI-Powered<br/>Employee Onboarding<br/>& Workforce Intelligence
          </h1>
          <p className="text-indigo-200 text-lg leading-relaxed">
            Streamline onboarding, predict attrition, analyze sentiment — all in one intelligent platform.
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-4">
          {[['50+','Employees'],['3','Roles'],['AI','Powered']].map(([n,l]) => (
            <div key={l} className="bg-white/10 rounded-2xl p-4 text-center">
              <div className="text-2xl font-bold text-white">{n}</div>
              <div className="text-indigo-200 text-xs mt-1">{l}</div>
            </div>
          ))}
        </div>

        {/* Feature list */}
        <div className="space-y-3">
          {['🔒 Role-based access control (RBAC)','🤖 AI attrition risk prediction',
            '📊 Real-time sentiment analysis','📧 Automated email alert system',
            '📄 Smart document verification'].map(f => (
            <div key={f} className="flex items-center gap-2 text-indigo-100 text-sm">{f}</div>
          ))}
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex flex-col items-center justify-center p-6 relative">
        <button onClick={toggle}
                className="absolute top-6 right-6 p-2 rounded-full"
                style={{ background: dark ? 'rgba(255,255,255,.08)' : 'rgba(0,0,0,.06)' }}>
          {dark ? <Sun size={18} className="text-yellow-400" /> : <Moon size={18} className="text-indigo-600" />}
        </button>

        <div className="w-full max-w-md">
          {/* Logo (mobile) */}
          <div className="lg:hidden flex items-center gap-2 mb-8 justify-center">
            <Building2 className="text-indigo-600" size={24} />
            <span style={{ fontFamily:'Syne,sans-serif' }}
                  className="text-2xl font-bold text-indigo-600">NexaWorks</span>
          </div>

          <h2 className="text-2xl font-bold mb-1" style={{ fontFamily:'Syne,sans-serif', color:'var(--text)' }}>
            Welcome back
          </h2>
          <p style={{ color:'var(--text-muted)' }} className="text-sm mb-8">
            Select your role, then sign in to your dashboard
          </p>

          {/* Role selection */}
          <div className="space-y-3 mb-6">
            {ROLES.map(r => (
              <button key={r.id}
                      onClick={() => { setSelectedRole(r.id); setEmail(''); setPassword(''); }}
                      className={`w-full flex items-start gap-4 p-4 rounded-2xl border-2 transition-all text-left
                        ${selectedRole === r.id
                          ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/40'
                          : 'border-gray-200 dark:border-gray-700 hover:border-indigo-300 dark:hover:border-indigo-700'}`}>
                <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${r.color} flex items-center justify-center flex-shrink-0`}>
                  <r.icon size={18} className="text-white" />
                </div>
                <div>
                  <div className="font-semibold text-sm" style={{ color:'var(--text)' }}>{r.label}</div>
                  <div className="text-xs mt-0.5" style={{ color:'var(--text-muted)' }}>{r.desc}</div>
                </div>
                {selectedRole === r.id && (
                  <div className="ml-auto w-5 h-5 rounded-full bg-indigo-500 flex items-center justify-center flex-shrink-0">
                    <div className="w-2 h-2 rounded-full bg-white" />
                  </div>
                )}
              </button>
            ))}
          </div>

          {/* Login form */}
          {selectedRole && (
            <form onSubmit={handleLogin} className="space-y-4 slide-up">
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Email</label>
                <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                       className="input-field" placeholder="your.email@nexaworks.in" required autoFocus />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color:'var(--text)' }}>Password</label>
                <div className="relative">
                  <input type={showPwd ? 'text' : 'password'} value={password}
                         onChange={e => setPassword(e.target.value)}
                         className="input-field pr-11" placeholder="••••••••" required />
                  <button type="button" onClick={() => setShowPwd(s => !s)}
                          className="absolute right-3 top-1/2 -translate-y-1/2"
                          style={{ color:'var(--text-muted)' }}>
                    {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              <button type="button" onClick={fillDemo}
                      className="w-full py-2 rounded-xl text-sm font-medium border border-dashed transition-all"
                      style={{ color:'var(--primary)', borderColor:'var(--primary)', background:'transparent' }}>
                ✨ Fill Demo Credentials
              </button>

              <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3">
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                    Signing in...
                  </span>
                ) : `Sign in as ${ROLES.find(r=>r.id===selectedRole)?.label}`}
              </button>
            </form>
          )}

          {/* Demo credentials helper */}
          <div className="mt-6 p-4 rounded-xl border" style={{ borderColor:'var(--border)', background:'var(--bg)' }}>
            <p className="text-xs font-semibold mb-2" style={{ color:'var(--text-muted)' }}>DEMO ACCOUNTS</p>
            <div className="space-y-1 text-xs font-mono" style={{ color:'var(--text-muted)' }}>
              <div><span className="text-emerald-500">Employee:</span> aarav.sharma@nexaworks.in / Emp@123456</div>
              <div><span className="text-violet-500">HR:</span> sunita.rao@nexaworks.in / HR@123456</div>
              <div><span className="text-blue-500">Manager:</span> vikram.mehta@nexaworks.in / Mgr@123456</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

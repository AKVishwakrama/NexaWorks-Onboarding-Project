import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Building2, LogOut, Moon, Sun, Menu, X, Bell } from 'lucide-react';

export default function Sidebar({ links, activeTab, onTabChange, notifications = 0 }) {
  const { user, logout } = useAuth();
  const { dark, toggle }  = useTheme();
  const [open, setOpen]   = useState(false);

  const roleColors = { employee: '#10b981', hr: '#8b5cf6', manager: '#3b82f6' };
  const color = roleColors[user?.role] || '#6366f1';

  const NavContent = () => (
    <div className="flex flex-col h-full">
      {/* Logo */}
      <div className="flex items-center gap-3 px-6 py-5 border-b border-white/10">
        <div className="w-9 h-9 rounded-xl bg-white/15 flex items-center justify-center">
          <Building2 size={18} className="text-white" />
        </div>
        <div>
          <div style={{ fontFamily:'Syne,sans-serif' }} className="text-white font-bold text-base leading-none">NexaWorks</div>
          <div className="text-white/50 text-xs mt-0.5">Onboarding Platform</div>
        </div>
      </div>

      {/* User */}
      <div className="px-6 py-4 border-b border-white/10">
        <div className="flex items-center gap-3">
          <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.name?.replace(' ','')}`}
               alt="" className="w-9 h-9 rounded-full bg-white/10" />
          <div className="min-w-0">
            <div className="text-white text-sm font-semibold truncate">{user?.name}</div>
            <div className="text-xs px-1.5 py-0.5 rounded-full inline-block mt-0.5 font-medium capitalize"
                 style={{ background: `${color}25`, color }}>
              {user?.role}
            </div>
          </div>
          {notifications > 0 && (
            <div className="ml-auto flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-500 text-white text-xs font-bold">
              <Bell size={10} />{notifications}
            </div>
          )}
        </div>
      </div>

      {/* Nav links */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {links.map(link => (
          <button key={link.id}
                  onClick={() => { onTabChange(link.id); setOpen(false); }}
                  className={`sidebar-link w-full ${activeTab === link.id ? 'active' : ''}`}>
            <link.icon size={17} />
            <span className="flex-1 text-left">{link.label}</span>
            {link.badge > 0 && (
              <span className="px-1.5 py-0.5 rounded-full bg-red-500 text-white text-xs font-bold min-w-[18px] text-center">
                {link.badge}
              </span>
            )}
          </button>
        ))}
      </nav>

      {/* Bottom */}
      <div className="px-3 pb-4 space-y-1 border-t border-white/10 pt-3">
        <button onClick={toggle} className="sidebar-link w-full">
          {dark ? <Sun size={17} /> : <Moon size={17} />}
          <span>{dark ? 'Light Mode' : 'Dark Mode'}</span>
        </button>
        <button onClick={logout} className="sidebar-link w-full hover:!bg-red-500/20 hover:!text-red-300">
          <LogOut size={17} />
          <span>Sign Out</span>
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex flex-col w-60 fixed inset-y-0 left-0 z-40"
             style={{ background: dark ? '#0d0f1e' : '#1e1b4b' }}>
        <NavContent />
      </aside>

      {/* Mobile hamburger */}
      <button className="lg:hidden fixed top-4 left-4 z-50 p-2 rounded-xl bg-indigo-600 text-white shadow-lg"
              onClick={() => setOpen(o => !o)}>
        {open ? <X size={20} /> : <Menu size={20} />}
      </button>

      {/* Mobile overlay */}
      {open && (
        <div className="lg:hidden fixed inset-0 z-40 flex">
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setOpen(false)} />
          <aside className="relative flex flex-col w-72 h-full z-50 shadow-2xl"
                 style={{ background: dark ? '#0d0f1e' : '#1e1b4b' }}>
            <NavContent />
          </aside>
        </div>
      )}
    </>
  );
}

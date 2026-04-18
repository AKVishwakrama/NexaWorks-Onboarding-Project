import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('nexaworks_token');
    const saved  = localStorage.getItem('nexaworks_user');
    if (token && saved) {
      try {
        setUser(JSON.parse(saved));
        // Refresh profile from backend
        authApi.me()
          .then(r => { setUser(r.data); localStorage.setItem('nexaworks_user', JSON.stringify(r.data)); })
          .catch(() => { localStorage.clear(); setUser(null); });
      } catch { localStorage.clear(); }
    }
    setLoading(false);
  }, []);

  const login = async (email, password, role) => {
    const res = await authApi.login(email, password, role);
    const { token, user: u } = res.data;
    localStorage.setItem('nexaworks_token', token);
    localStorage.setItem('nexaworks_user', JSON.stringify(u));
    setUser(u);
    return u;
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    toast.success('Logged out successfully');
  };

  const refreshUser = async () => {
    try {
      const r = await authApi.me();
      setUser(r.data);
      localStorage.setItem('nexaworks_user', JSON.stringify(r.data));
    } catch (e) { console.error('Refresh failed', e); }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
};

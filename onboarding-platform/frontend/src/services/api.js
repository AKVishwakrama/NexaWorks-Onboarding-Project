import axios from 'axios';

const BACKEND = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
const AI      = import.meta.env.VITE_AI_URL || 'http://localhost:5001';

// ── Axios instances ────────────────────────────────────────────────────────
export const backendApi = axios.create({ baseURL: BACKEND });
export const aiApi      = axios.create({ baseURL: AI });

// Attach JWT automatically
backendApi.interceptors.request.use(cfg => {
  const token = localStorage.getItem('nexaworks_token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

backendApi.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// ═══════════════════════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════════════════════
export const authApi = {
  login:    (email, password, role) =>
    backendApi.post('/api/auth/login', { email, password, role }),
  register: (data) =>
    backendApi.post('/api/auth/register', data),
  me:       () =>
    backendApi.get('/api/auth/me'),
  health:   () =>
    backendApi.get('/api/auth/health'),
};

// ═══════════════════════════════════════════════════════════
// EMPLOYEE
// ═══════════════════════════════════════════════════════════
export const employeeApi = {
  dashboard:  () => backendApi.get('/api/employee/dashboard'),
  profile:    () => backendApi.get('/api/employee/profile'),

  uploadDoc:  (docType, file) => {
    const form = new FormData();
    form.append('docType', docType);
    if (file) form.append('file', file);
    return backendApi.post('/api/employee/document/upload', form,
      { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  removeDoc:  (docType) => backendApi.delete(`/api/employee/document/${docType}`),

  updateTask: (taskKey, completed) =>
    backendApi.put(`/api/employee/task/${taskKey}`, { completed }),

  submitFeedback: (data) => backendApi.post('/api/employee/feedback', data),
  getFeedback:    ()     => backendApi.get('/api/employee/feedback'),

  getNotifications: () => backendApi.get('/api/employee/notifications'),
  markAllRead:      () => backendApi.put('/api/employee/notifications/read-all'),

  getMeetings: () => backendApi.get('/api/employee/meetings'),
};

// ═══════════════════════════════════════════════════════════
// HR
// ═══════════════════════════════════════════════════════════
export const hrApi = {
  dashboard:       () => backendApi.get('/api/hr/dashboard'),
  employees:       () => backendApi.get('/api/hr/employees'),
  employee:        (id) => backendApi.get(`/api/hr/employees/${id}`),
  verifyDoc:       (id, docType, approved, note) =>
    backendApi.put(`/api/hr/employees/${id}/document/${docType}/verify`, { approved, note }),
  remindDocs:      (id) => backendApi.post(`/api/hr/employees/${id}/remind-documents`),
  sendRiskAlert:   (id) => backendApi.post(`/api/hr/employees/${id}/risk-alert`),
  sendNotification:(id, data) => backendApi.post(`/api/hr/employees/${id}/notify`, data),
  scheduleMeeting: (data) => backendApi.post('/api/hr/meetings', data),
  meetings:        () => backendApi.get('/api/hr/meetings'),
  feedback:        () => backendApi.get('/api/hr/feedback'),
  highRisk:        () => backendApi.get('/api/hr/high-risk'),
  allUsers:        () => backendApi.get('/api/hr/all-users'),
};

// ═══════════════════════════════════════════════════════════
// MANAGER
// ═══════════════════════════════════════════════════════════
export const managerApi = {
  dashboard:       () => backendApi.get('/api/manager/dashboard'),
  team:            () => backendApi.get('/api/manager/team'),
  teamMember:      (id) => backendApi.get(`/api/manager/team/${id}`),
  scheduleMeeting: (data) => backendApi.post('/api/manager/meetings', data),
  meetings:        () => backendApi.get('/api/manager/meetings'),
  completeMeeting: (id) => backendApi.put(`/api/manager/meetings/${id}/complete`),
  teamFeedback:    () => backendApi.get('/api/manager/team-feedback'),
  highRisk:        () => backendApi.get('/api/manager/high-risk'),
};

// ═══════════════════════════════════════════════════════════
// AI SERVICE
// ═══════════════════════════════════════════════════════════
export const aiService = {
  health:        () => aiApi.get('/health'),
  analyzeSentiment: (text, employeeId) =>
    aiApi.post('/api/sentiment/analyze', { text, employee_id: employeeId }),
  bulkSentiment: () => aiApi.get('/api/sentiment/bulk'),
  predictRisk:   (emp) => aiApi.post('/api/risk/predict', { employee: emp }),
  bulkRisk:      () => aiApi.get('/api/risk/bulk'),
  analytics:     () => aiApi.get('/api/analytics/dashboard'),
  chat:          (message, history, role, userName) =>
    aiApi.post('/api/chatbot', { message, history, role, user_name: userName }),
  recommendations: (emp) => aiApi.post('/api/recommendations', { employee: emp }),
};

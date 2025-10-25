import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1/admin',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

// Attach bearer token from localStorage for protected admin calls
const attachAuth = (instance: any) => {
  instance.interceptors.request.use((config: any) => {
    const token = localStorage.getItem('admin_token') || localStorage.getItem('token');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
};

attachAuth(api);

export default api;
// Questionnaire endpoints (not under /admin namespace)
export const questionnaireApi = axios.create({
  baseURL: '/api/v1/questionnaire',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

attachAuth(questionnaireApi);

export const getQuestions = async () => {
  const res = await questionnaireApi.get('/questions');
  return res.data;
};

export const getSubmissions = async () => {
  const res = await questionnaireApi.get('/submissions');
  return res.data;
};

export const getClients = async () => {
  const res = await api.get('/clients');
  return res.data;
};

export type CreateClientResponse = {
  user: any;
  temporaryPassword?: string;
};

export const createClient = async (payload: any): Promise<CreateClientResponse> => {
  const res = await api.post('/clients', payload);
  return res.data;
};

export const updateClient = async (id: number | string, payload: any) => {
  const res = await api.put(`/clients/${id}`, payload);
  return res.data;
};

export const deleteClient = async (id: number | string) => {
  const res = await api.delete(`/clients/${id}`);
  return res.status === 204;
};

// Dashboard
export type DashboardSummaryResponse = {
  totalClients: number;
  activeClients: number;
  totalQuestionnaires: number;
  totalAssignments: number;
  pendingAssessments: number;
  completedAssessments: number;
};

export const getDashboardSummary = async (): Promise<DashboardSummaryResponse> => {
  const res = await api.get('/dashboard/summary');
  return res.data;
};

// Helper for tests or login flows to set the token manually
export const setAuthToken = (token: string | null) => {
  if (token) {
    localStorage.setItem('admin_token', token);
  } else {
    localStorage.removeItem('admin_token');
  }
};

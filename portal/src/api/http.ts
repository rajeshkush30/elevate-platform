import axios, { AxiosError, AxiosResponse } from 'axios';
import { emitLogoutRequest } from '../context/authEvents';
import { showGlobalToast } from '../components/ToastProvider';

// Shared axios instance for all authenticated API calls
const http = axios.create({
  withCredentials: true,
});

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers = config.headers || {};
      (config.headers as any).Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

http.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      emitLogoutRequest();
    }
    // Show global toast for unhandled errors (network, 5xx, others without specific handling)
    const status = error.response?.status;
    const isNetwork = !error.response;
    if (isNetwork) {
      showGlobalToast('Network error. Please check your connection.', 'error');
    } else if (status && status >= 500) {
      showGlobalToast('Server error. Please try again later.', 'error');
    }
    return Promise.reject(error);
  }
);

export default http;

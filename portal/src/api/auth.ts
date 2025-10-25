import axios, { AxiosError, AxiosResponse } from 'axios';
import { User } from '../types';
import { emitLogoutRequest } from '../context/authEvents';

// API base URL (matches the proxy configuration in vite.config.ts)
const API_BASE_URL = '/api/v1/auth';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Request interceptor for logging
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('Request:', config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for handling errors
api.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('Response:', response.status, response.config.url);
    return response;
  },
  (error: AxiosError) => {
    console.error('API Error:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data,
    });
    
    // Handle 401 Unauthorized via centralized logout event
    if (error.response?.status === 401) {
      console.warn('Authentication required (401). Emitting logout request.');
      // Let AuthContext handle clearing storage (token, refreshToken, user) and navigation
      emitLogoutRequest();
    }
    
    return Promise.reject(error);
  }
);

// keep a consistent base path for fallback usage
const API_URL = API_BASE_URL;

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export const login = async (email: string, password: string): Promise<LoginResponse> => {
  try {
    // Backend returns AuthenticationResponse with snake_case token fields and flattened user fields
    const response = await api.post('/login', { email, password });
    const r = response.data as any;
    const user: User = {
      id: r.id,
      firstName: r.firstName,
      lastName: r.lastName,
      email: r.email,
      role: r.role,
      emailVerified: r.isEmailVerified ?? r.emailVerified ?? false,
      createdAt: r.createdAt ?? '',
      updatedAt: r.updatedAt ?? ''
    };
    return {
      accessToken: r.access_token ?? r.accessToken,
      refreshToken: r.refresh_token ?? r.refreshToken,
      user,
    };
  } catch (error: any) {
    console.error('Login failed:', error);
    const errorMessage = error.response?.data?.message || 'Login failed. Please check your credentials and try again.';
    throw new Error(errorMessage);
  }
};

export const register = async (userData: {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}): Promise<void> => {
  try {
    await api.post('/register', userData);
  } catch (error) {
    console.error('Registration failed:', error);
    if (axios.isAxiosError(error) && error.response?.status === 400) {
      throw new Error('Email already exists. Please use a different email.');
    }
  }
};

export const refreshToken = async (refreshToken: string): Promise<{ accessToken: string; user: User }> => {
  try {
    const response = await api.post(
      '/refresh-token',
      {},
      {
        headers: { Authorization: `Bearer ${refreshToken}` },
      }
    );
    const r = response.data as any;
    const user: User = {
      id: r.id,
      firstName: r.firstName,
      lastName: r.lastName,
      email: r.email,
      role: r.role,
      emailVerified: r.isEmailVerified ?? r.emailVerified ?? false,
      createdAt: r.createdAt ?? '',
      updatedAt: r.updatedAt ?? ''
    };
    return {
      accessToken: r.access_token ?? r.accessToken,
      user,
    };
  } catch (error) {
    console.error('Token refresh failed:', error);
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      throw new Error('Session expired. Please log in again.');
    }
    throw new Error('Token refresh failed. Please try again later.');
  }
};

export const forgotPassword = async (email: string): Promise<void> => {
  // Server expects email as a request parameter (@RequestParam)
  await api.post(`/forgot-password`, null, { params: { email } });
};

export const resetPassword = async (token: string, newPassword: string): Promise<void> => {
  // Server expects token and newPassword as request parameters (@RequestParam)
  await api.post(`/reset-password`, null, { params: { token, newPassword } });
};

export const verifyEmail = async (token: string): Promise<boolean> => {
  const response = await axios.get(`${API_URL}/verify-email?token=${token}`);
  return response.data;
};

export const getCurrentUser = async (token: string): Promise<User> => {
  const response = await axios.get(`${API_URL}/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  const r = response.data as any;
  const user: User = {
    id: r.id,
    firstName: r.firstName,
    lastName: r.lastName,
    email: r.email,
    role: r.role,
    emailVerified: r.isEmailVerified ?? r.emailVerified ?? false,
    createdAt: r.createdAt ?? '',
    updatedAt: r.updatedAt ?? ''
  };
  return user;
};

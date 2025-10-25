import React, { createContext, useContext, useState, ReactNode } from 'react';
import axios from 'axios';
import { setAuthToken } from '../api/admin';

interface AuthContextType {
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setTokenState] = useState<string | null>(() => localStorage.getItem('admin_token'));

  const login = async (email: string, password: string) => {
    // Call backend auth endpoint
    const res = await axios.post('/api/v1/auth/login', { email, password }, { headers: { 'Content-Type': 'application/json' }, withCredentials: true });
    const data = res.data;

    // Accept multiple possible token field names returned by server (camelCase or snake_case)
    const accessToken = data?.accessToken || data?.access_token || data?.token || data?.authenticationToken;
    const refreshToken = data?.refreshToken || data?.refresh_token || data?.refresh_token;

    if (!accessToken) {
      // Log payload in dev to help debug token naming mismatches
      // eslint-disable-next-line no-console
      console.debug('Auth login response payload:', data);
      // Provide a helpful error with returned payload for debugging
      throw new Error('No access token received from server: ' + JSON.stringify(data));
    }

    // Persist token and update state
    setAuthToken(accessToken);
    setTokenState(accessToken);
    // Optionally store refresh token if present
    if (refreshToken) {
      try {
        localStorage.setItem('admin_refresh_token', refreshToken);
      } catch (e) {
        // ignore storage errors
      }
    }
  };

  const logout = () => {
    setAuthToken(null);
    setTokenState(null);
  };

  return (
    <AuthContext.Provider value={{ token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAdminAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAdminAuth must be used within AuthProvider');
  return ctx;
};

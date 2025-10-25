import { createContext, useContext, useEffect, useState, ReactNode, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import jwtDecode from 'jwt-decode';
import { User } from '../types';
import { login as loginApi, register as registerApi, refreshToken as refreshTokenApi } from "../api/auth";
import { onLogoutRequest } from './authEvents';


// Helper to check if token is expired
const isTokenExpired = (token: string): boolean => {
  try {
    const decoded: any = jwtDecode(token);
    return decoded.exp * 1000 < Date.now();
  } catch (error) {
    return true;
  }
};

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: { firstName: string; lastName: string; email: string; password: string }) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<boolean>;
  loading: boolean;
  error: Error | null;
}

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const navigate = useNavigate();
  const updateState = useCallback((updates: Partial<AuthContextType>) => {
    setUser(prev => 'user' in updates ? updates.user ?? null : prev);
    setToken(prev => 'token' in updates ? updates.token ?? null : prev);
    setLoading(prev => 'loading' in updates ? !!updates.loading : prev);
    setError(prev => 'error' in updates ? updates.error ?? null : prev);
  }, []);

  const logout = useCallback(() => {
    // Clear all auth data
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    
    // Reset state
    updateState({ user: null, token: null, loading: false, error: null });
    
    // Redirect to login
    navigate('/login');
  }, [navigate, updateState]);

  const refreshToken = useCallback(async (): Promise<boolean> => {
    try {
      const storedRefreshToken = localStorage.getItem("refreshToken");
      if (!storedRefreshToken) return false;

      const { accessToken, user } = await refreshTokenApi(storedRefreshToken);

      localStorage.setItem("token", accessToken);
      localStorage.setItem("user", JSON.stringify(user));

      updateState({ user, token: accessToken });
      return true;
    } catch (error) {
      console.error("Token refresh failed:", error);
      logout();
      return false;
    }
  }, [updateState, logout]);

  // Initialize auth state on mount with timeout
  useEffect(() => {
    let isMounted = true;
    let timeoutId: NodeJS.Timeout;
    
    const initializeAuth = async () => {
      try {
        const storedToken = localStorage.getItem('token');
        const storedUser = localStorage.getItem('user');
        
        // Add a timeout to prevent infinite loading
        timeoutId = setTimeout(() => {
          if (isMounted) {
            console.warn('Auth initialization is taking too long. Check your backend connection.');
            updateState({ loading: false });
          }
        }, 5000); // 5 second timeout
        
        // No stored token or user: not an error, just unauthenticated state
        if (!storedToken || !storedUser) {
          if (isMounted) {
            updateState({ loading: false });
          }
          return;
        }
        
        const userData = JSON.parse(storedUser) as User;
        
        // Check if token is expired
        if (isTokenExpired(storedToken)) {
          // Try to refresh token
          const refreshed = await refreshToken();
          if (!refreshed) {
            throw new Error('Session expired');
          }
        } else {
          // Token is valid, update state
          if (isMounted) {
            updateState({
              user: userData,
              token: storedToken,
              loading: false,
            });
          }
        }
      } catch (error) {
        console.warn('Auth initialization warning:', error);
        if (isMounted) {
          // Don't show error to user, just continue as unauthenticated
          logout();
          updateState({ loading: false });
        }
      } finally {
        if (isMounted) {
          clearTimeout(timeoutId);
          // Ensure loading is set to false in case of any errors
          updateState({ loading: false });
        }
      }
    };
    
    initializeAuth();
    
    return () => {
      isMounted = false;
      clearTimeout(timeoutId);
    };
  }, [refreshToken, logout]);

  const login = useCallback(async (email: string, password: string) => {
    try {
      updateState({ loading: true, error: null });
      const { user, accessToken, refreshToken } = await loginApi(email, password);
      
      // Store tokens and user data
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify(user));
      
      // Update state
      updateState({ user, token: accessToken, loading: false });
      
      // Redirect based on role
      if (user.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Login failed');
      updateState({ error, loading: false });
      throw error;
    }
  }, [navigate, updateState]);

  const register = useCallback(async (userData: { 
    firstName: string; 
    lastName: string; 
    email: string; 
    password: string; 
  }) => {
    try {
      updateState({ loading: true, error: null });
      await registerApi(userData);
      // After successful registration, log the user in
      await login(userData.email, userData.password);
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Registration failed');
      updateState({ error, loading: false });
      throw error;
    }
  }, [login, updateState]);


  // Set up token refresh interval
  useEffect(() => {
    const interval = setInterval(() => {
      if (token && isTokenExpired(token)) {
        refreshToken();
      }
    }, 5 * 60 * 1000); // Check every 5 minutes
    
    return () => clearInterval(interval);
  }, [token, refreshToken]);

  // Subscribe to centralized logout requests (e.g., from axios interceptor 401s)
  useEffect(() => {
    const unsubscribe = onLogoutRequest(() => {
      logout();
    });
    return () => {
      unsubscribe();
    };
  }, [logout]);
  
  // Create memoized context value
  const contextValue = useMemo(() => ({
    user,
    token,
    loading,
    error,
    login,
    register,
    logout,
    refreshToken,
  }), [user, token, loading, error, login, register, logout, refreshToken]);
  
  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

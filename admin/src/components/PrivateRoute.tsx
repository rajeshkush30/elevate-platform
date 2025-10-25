import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAdminAuth } from '../context/AuthContext';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token } = useAdminAuth();
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

export default PrivateRoute;

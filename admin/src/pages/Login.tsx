import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Paper, Box, Typography, TextField, Button, Alert } from '@mui/material';
import { useAdminAuth } from '../context/AuthContext';

const Login: React.FC = () => {
  const { login } = useAdminAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate('/');
    } catch (err: any) {
      // Prefer server-provided message, then thrown Error.message, then a generic fallback
      const serverMsg = err?.response?.data?.message;
      const thrownMsg = err?.message;
      setError(serverMsg || thrownMsg || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8 }}>
        <Paper sx={{ p: 4 }}>
          <Typography variant="h5">Admin Login</Typography>
          {error && <Alert severity="error">{error}</Alert>}
          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
            <TextField value={email} onChange={(e) => setEmail(e.target.value)} fullWidth label="Email" sx={{ mb: 2 }} />
            <TextField value={password} onChange={(e) => setPassword(e.target.value)} fullWidth label="Password" type="password" sx={{ mb: 2 }} />
            <Button type="submit" variant="contained" disabled={loading}>{loading ? 'Signing in...' : 'Sign In'}</Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;

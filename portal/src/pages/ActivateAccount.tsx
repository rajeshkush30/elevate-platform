import { useEffect, useState } from 'react';
import { useSearchParams, Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Paper,
  Alert,
  TextField,
  Link,
} from '@mui/material';
import { activateAccount } from '../api/auth';

const ActivateAccount = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [token, setToken] = useState<string | null>(null);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [submitting, setSubmitting] = useState(false);
  const [status, setStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [error, setError] = useState('');

  useEffect(() => {
    const t = searchParams.get('token');
    setToken(t);
  }, [searchParams]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      setStatus('error');
      setError('Activation token is missing.');
      return;
    }
    if (!password || password.length < 8) {
      setStatus('error');
      setError('Password must be at least 8 characters.');
      return;
    }
    if (password !== confirmPassword) {
      setStatus('error');
      setError('Passwords do not match.');
      return;
    }

    try {
      setSubmitting(true);
      setStatus('idle');
      setError('');
      await activateAccount(token, password);
      setStatus('success');
      // Redirect to login after 2.5 seconds
      setTimeout(() => navigate('/login'), 2500);
    } catch (err: any) {
      console.error(err);
      setStatus('error');
      setError(
        err?.response?.data?.message || 'Activation failed. The link may have expired or is invalid.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  const tokenMissing = token === null || token.trim() === '';

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, mt: 3, width: '100%', textAlign: 'center' }}>
          <Typography component="h1" variant="h5" gutterBottom>
            Activate Your Account
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Set your new password to activate your account.
          </Typography>

          {tokenMissing && (
            <Alert severity="error" sx={{ mb: 3 }}>
              Activation token is missing. Please use the link sent to your email.
            </Alert>
          )}

          {status === 'success' && (
            <Alert severity="success" sx={{ mb: 3 }}>
              Your account has been activated. Redirecting to login...
            </Alert>
          )}

          {status === 'error' && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={onSubmit} sx={{ mt: 1, textAlign: 'left' }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="password"
              label="New Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={submitting || status === 'success' || tokenMissing}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="confirmPassword"
              label="Confirm Password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={submitting || status === 'success' || tokenMissing}
              helperText="Password must be at least 8 characters."
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              color="primary"
              sx={{ mt: 2, mb: 2 }}
              disabled={submitting || status === 'success' || tokenMissing}
              startIcon={submitting ? <CircularProgress size={18} /> : null}
            >
              {submitting ? 'Activating...' : 'Activate Account'}
            </Button>

            <Box sx={{ textAlign: 'center' }}>
              <Typography>
                Already activated?{' '}
                <Link component={RouterLink} to="/login">
                  Go to Login
                </Link>
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default ActivateAccount;

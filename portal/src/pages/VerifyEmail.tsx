import { useState, useEffect } from 'react';
import { useSearchParams, Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Paper,
  Alert,
  Link,
} from '@mui/material';
import { verifyEmail } from '../api/auth';

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'verifying' | 'success' | 'error'>('verifying');
  const [error, setError] = useState('');

  useEffect(() => {
    const verifyToken = async () => {
      const token = searchParams.get('token');
      if (!token) {
        setStatus('error');
        setError('Verification token is missing');
        return;
      }

      try {
        const success = await verifyEmail(token);
        if (success) {
          setStatus('success');
          // Redirect to login after 3 seconds
          setTimeout(() => navigate('/login'), 3000);
        } else {
          setStatus('error');
          setError('Email verification failed. The link may have expired or is invalid.');
        }
      } catch (err: any) {
        setStatus('error');
        setError(err.response?.data?.message || 'An error occurred during email verification.');
      }
    };

    verifyToken();
  }, [searchParams, navigate]);

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
          {status === 'verifying' && (
            <>
              <CircularProgress sx={{ mb: 2 }} />
              <Typography component="h1" variant="h5" gutterBottom>
                Verifying your email...
              </Typography>
            </>
          )}

          {status === 'success' && (
            <>
              <Alert severity="success" sx={{ mb: 3 }}>
                <Typography variant="h6" component="div" gutterBottom>
                  Email Verified Successfully!
                </Typography>
                <Typography>You will be redirected to the login page shortly.</Typography>
              </Alert>
              <Button
                component={RouterLink}
                to="/login"
                variant="contained"
                color="primary"
                sx={{ mt: 2 }}
              >
                Go to Login
              </Button>
            </>
          )}

          {status === 'error' && (
            <>
              <Alert severity="error" sx={{ mb: 3 }}>
                <Typography variant="h6" component="div" gutterBottom>
                  Verification Failed
                </Typography>
                <Typography>{error}</Typography>
              </Alert>
              <Box sx={{ mt: 3, display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Button
                  component={RouterLink}
                  to="/login"
                  variant="contained"
                  color="primary"
                >
                  Go to Login
                </Button>
                <Typography>
                  Need help?{' '}
                  <Link component={RouterLink} to="/contact">
                    Contact Support
                  </Link>
                </Typography>
              </Box>
            </>
          )}
        </Paper>
      </Box>
    </Container>
  );
};

export default VerifyEmail;

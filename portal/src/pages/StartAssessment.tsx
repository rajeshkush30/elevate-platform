import { useState } from 'react';
import { Container, Paper, Typography, Box, Button, CircularProgress, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { createAttempt } from '../api/clientAssessment';
import { useToast } from '../components/ToastProvider';

const StartAssessment = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onStart = async () => {
    try {
      setLoading(true);
      setError(null);
      const attemptId = await createAttempt({});
      showToast('Attempt started', 'success');
      navigate(`/assessment/take/${attemptId}`);
    } catch (e: any) {
      setError(e?.message || 'Failed to start assessment');
      showToast('Failed to start assessment', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h5">Start Assessment</Typography>
        <Typography variant="body1" sx={{ mt: 1 }}>
          Begin a new assessment attempt. You can answer questions and submit to get your stage and score.
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>
        )}
        <Box sx={{ mt: 3 }}>
          <Button variant="contained" onClick={onStart} disabled={loading}>
            {loading ? <CircularProgress size={20} /> : 'Start Now'}
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default StartAssessment;

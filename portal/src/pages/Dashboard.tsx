import { Box, Typography, Button, Container, Grid, Card, CardContent, Stack, Chip, Divider } from '@mui/material';
import PlayCircleOutlineIcon from '@mui/icons-material/PlayCircleOutline';
import HistoryIcon from '@mui/icons-material/History';
import SchoolIcon from '@mui/icons-material/School';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import DescriptionIcon from '@mui/icons-material/Description';
import LogoutIcon from '@mui/icons-material/Logout';
import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from 'react';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [hasResult, setHasResult] = useState(false);
  const [stage, setStage] = useState<string | null>(null);
  const [score, setScore] = useState<number | null>(null);
  const initials = `${(user?.firstName || 'U')[0]}${(user?.lastName || '').slice(0,1)}`.toUpperCase();
  const [lastSavedAt, setLastSavedAt] = useState<string | null>(null);

  useEffect(() => {
    try {
      const raw = localStorage.getItem('lastAssessmentResult');
      if (raw) {
        const obj = JSON.parse(raw);
        setHasResult(true);
        if (obj?.stage) setStage(String(obj.stage));
        if (typeof obj?.score === 'number') setScore(obj.score);
        if (obj?.savedAt) setLastSavedAt(String(obj.savedAt));
      }
    } catch {}
  }, []);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Hero / Welcome */}
      <Card
        sx={{
          mb: 3,
          color: 'white',
          borderRadius: 3,
          overflow: 'hidden',
          boxShadow: '0 10px 30px rgba(25, 118, 210, 0.25)'
        }}
      >
        <Box
          sx={{
            px: 3,
            py: 3,
            background: 'linear-gradient(135deg, #1565c0 0%, #42a5f5 100%)',
            position: 'relative'
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              right: -40,
              top: -40,
              width: 200,
              height: 200,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.12)'
            }}
          />
          <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ xs: 'flex-start', sm: 'center' }} spacing={2}>
            <Box
              sx={{
                width: 56,
                height: 56,
                borderRadius: '50%',
                background: 'rgba(255,255,255,0.25)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 700,
                fontSize: 18
              }}
            >
              {initials}
            </Box>
            <Box>
              <Typography variant="h5" fontWeight={700}>
                Welcome, {user?.firstName} {user?.lastName}
              </Typography>
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                {user?.email}
              </Typography>
              <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                <Chip label={user?.role ?? 'CLIENT'} size="small" sx={{ bgcolor: 'rgba(255,255,255,0.25)', color: 'white' }} />
                {hasResult && stage && <Chip label={`Last Stage: ${stage}`} size="small" sx={{ bgcolor: 'rgba(255,255,255,0.25)', color: 'white' }} />}
                {score !== null && <Chip label={`Score: ${score}`} size="small" sx={{ bgcolor: 'rgba(255,255,255,0.25)', color: 'white' }} />}
              </Stack>
            </Box>
            <Box sx={{ flex: 1 }} />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
              <Button startIcon={<PlayCircleOutlineIcon />} variant="contained" color="inherit" href="/assessment/start" sx={{ color: '#1565c0' }}>
                Start Assessment
              </Button>
              {hasResult && (
                <Button startIcon={<HistoryIcon />} variant="outlined" color="inherit" href="/assessment/result" sx={{ borderColor: 'rgba(255,255,255,0.7)', color: 'white' }}>
                  View Last Result
                </Button>
              )}
              <Button startIcon={<LogoutIcon />} variant="text" color="inherit" onClick={logout} sx={{ color: 'white' }}>
                Logout
              </Button>
            </Stack>
          </Stack>
        </Box>
      </Card>

      {/* Content grid */}
      <Grid container spacing={3}>
        {/* Quick Actions */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Quick Actions</Typography>
              <Stack direction="row" spacing={1.5} flexWrap="wrap">
                <Button startIcon={<PlayCircleOutlineIcon />} size="large" variant="contained" href="/assessment/start">New Assessment</Button>
                <Button startIcon={<HistoryIcon />} size="large" variant="outlined" href="/assessment/result" disabled={!hasResult}>Last Result</Button>
                <Button startIcon={<HistoryIcon />} size="large" variant="outlined" href="/assessment/history">History</Button>
                <Button startIcon={<SchoolIcon />} size="large" variant="outlined" href="/training">Training</Button>
                <Button startIcon={<ChecklistIcon />} size="large" variant="outlined" href="/strategy">Strategy Planner</Button>
                <Button startIcon={<ChatBubbleOutlineIcon />} size="large" variant="outlined" href="/consult">AI Consultation</Button>
                <Button startIcon={<DescriptionIcon />} size="large" variant="text" href="/questionnaire">Questionnaire (preview)</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Status / Summary */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Status</Typography>
              {!hasResult ? (
                <Typography color="text.secondary">No assessments yet. Click "New Assessment" to begin.</Typography>
              ) : (
                <Box>
                  <Stack direction="row" spacing={2} alignItems="center">
                    <Chip icon={<ChecklistIcon />} label={`Stage: ${stage ?? '-'}`} color="primary" />
                    <Chip icon={<HistoryIcon />} label={`Score: ${score ?? '-'}`} />
                  </Stack>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="body2" color="text.secondary">
                    Your most recent result is stored locally. For history and detailed insights, go to the Results page.
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Activity */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Recent Activity</Typography>
              {!hasResult || !lastSavedAt ? (
                <Typography color="text.secondary">No recent activity. Results will appear here after your next assessment.</Typography>
              ) : (
                <Stack spacing={1}>
                  <Typography variant="body2">Last assessment saved at: {new Date(lastSavedAt).toLocaleString()}</Typography>
                  <Stack direction="row" spacing={1}>
                    <Chip icon={<ChecklistIcon />} label={`Stage: ${stage ?? '-'}`} size="small" />
                    <Chip icon={<HistoryIcon />} label={`Score: ${score ?? '-'}`} size="small" />
                  </Stack>
                </Stack>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Tips / Placeholder */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Next Steps</Typography>
              <Typography variant="body2" color="text.secondary">
                After completing an assessment, you will get tailored recommendations. Upcoming features will include training assignments and a strategy planner.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;

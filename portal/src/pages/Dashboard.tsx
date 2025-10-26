import { Box, Typography, Button, Container, Grid, Card, CardContent, Stack, Chip, Divider } from '@mui/material';
import AssignmentIcon from '@mui/icons-material/Assignment';
import HistoryIcon from '@mui/icons-material/History';
import SchoolIcon from '@mui/icons-material/School';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import LogoutIcon from '@mui/icons-material/Logout';
import { useAuth } from '../context/AuthContext';
import { useEffect, useMemo, useState } from 'react';
import { listMyAssessments, MyAssessment } from '../api/myAssessments';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [items, setItems] = useState<MyAssessment[]>([]);
  const [loading, setLoading] = useState(true);
  const initials = `${(user?.firstName || 'U')[0]}${(user?.lastName || '').slice(0,1)}`.toUpperCase();
  
  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const data = await listMyAssessments();
        setItems(data);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const stats = useMemo(() => {
    const assigned = items.filter(i => i.status === 'ASSIGNED').length;
    const inProgress = items.filter(i => i.status === 'IN_PROGRESS').length;
    const submitted = items.filter(i => i.status === 'SUBMITTED' || i.status === 'SCORED').length;
    const nextDue = items
      .map(i => i.dueDate ? new Date(i.dueDate) : null)
      .filter((d): d is Date => !!d)
      .sort((a,b) => a.getTime() - b.getTime())[0] || null;
    const recent = items.slice(0, 5);
    const continueItem = items.find(i => i.status === 'IN_PROGRESS') || items.find(i => i.status === 'ASSIGNED') || null;
    return { assigned, inProgress, submitted, nextDue, recent, continueItem };
  }, [items]);

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
              </Stack>
            </Box>
            <Box sx={{ flex: 1 }} />
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
              <Button startIcon={<AssignmentIcon />} variant="contained" color="inherit" onClick={() => navigate('/my-assessments')} sx={{ color: '#1565c0' }}>
                My Assessments
              </Button>
              {stats.continueItem && (
                <Button startIcon={<ChecklistIcon />} variant="outlined" color="inherit" onClick={() => navigate(`/client/assessments/${stats.continueItem!.id}`)} sx={{ borderColor: 'rgba(255,255,255,0.7)', color: 'white' }}>
                  {stats.continueItem.status === 'ASSIGNED' ? 'Start Now' : 'Continue'}
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
                <Button startIcon={<AssignmentIcon />} size="large" variant="contained" href="/my-assessments">My Assessments</Button>
                <Button startIcon={<HistoryIcon />} size="large" variant="outlined" href="/assessment/history">History</Button>
                <Button startIcon={<SchoolIcon />} size="large" variant="outlined" href="/training">Training</Button>
                <Button startIcon={<ChecklistIcon />} size="large" variant="outlined" href="/strategy">Strategy Planner</Button>
                <Button startIcon={<ChatBubbleOutlineIcon />} size="large" variant="outlined" href="/consult">AI Consultation</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Status / Summary */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Status</Typography>
              <Box>
                {loading ? (
                  <Typography color="text.secondary">Loading...</Typography>
                ) : (
                  <Stack spacing={2}>
                    <Stack direction="row" spacing={2}>
                      <Chip label={`Assigned: ${stats.assigned}`} />
                      <Chip label={`In Progress: ${stats.inProgress}`} color="warning" />
                      <Chip label={`Submitted: ${stats.submitted}`} color="success" />
                    </Stack>
                    <Typography variant="body2" color="text.secondary">
                      {stats.nextDue ? `Next due: ${stats.nextDue.toLocaleDateString()}` : 'No upcoming due dates'}
                    </Typography>
                  </Stack>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Activity */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Recent Activity</Typography>
              {loading ? (
                <Typography color="text.secondary">Loading...</Typography>
              ) : stats.recent.length === 0 ? (
                <Typography color="text.secondary">No recent items. Assigned and in-progress assessments will appear here.</Typography>
              ) : (
                <Stack spacing={1}>
                  {stats.recent.map(item => (
                    <Stack key={item.id} direction="row" justifyContent="space-between" alignItems="center">
                      <Typography variant="body2">{item.name}</Typography>
                      <Stack direction="row" spacing={1} alignItems="center">
                        <Chip size="small" label={item.status} />
                        <Button size="small" onClick={() => navigate(`/client/assessments/${item.id}`)}>
                          {item.status === 'ASSIGNED' ? 'Start' : 'Open'}
                        </Button>
                      </Stack>
                    </Stack>
                  ))}
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
                Track your assigned and in-progress assessments here. You can always resume where you left off from the Recent Activity list.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;

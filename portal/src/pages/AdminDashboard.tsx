import { Box, Typography, Container, Paper, Stack, Button, Grid, CircularProgress, Alert, Skeleton, Chip } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import adminDashboardApi, { AdminSummary } from '../api/adminDashboard';
import PeopleOutlineIcon from '@mui/icons-material/PeopleOutline';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import QuizIcon from '@mui/icons-material/Quiz';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import InsightsIcon from '@mui/icons-material/Insights';
import { useToast } from '../components/ToastProvider';

const AdminDashboard = () => {
  const { user, logout } = useAuth();
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<AdminSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { showToast } = useToast();

  useEffect(() => {
    const load = async () => {
      try {
        setError(null);
        const data = await adminDashboardApi.getSummary();
        setSummary(data);
      } catch (e: any) {
        setSummary(null);
        setError('Failed to load dashboard summary');
        showToast('Failed to load admin summary', 'error');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h4">Admin Dashboard</Typography>
        <Button variant="outlined" color="inherit" onClick={logout}>Logout</Button>
      </Stack>

      <Paper elevation={1} sx={{ p: 3 }}>
        <Typography variant="subtitle1" gutterBottom>
          Signed in as: {user?.firstName} {user?.lastName} ({user?.email})
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Role: {user?.role}
        </Typography>
      </Paper>

      <Box sx={{ mt: 3 }}>
        <Typography variant="h6" gutterBottom>Overview</Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        )}
        {loading ? (
          <Grid container spacing={2}>
            {[...Array(3)].map((_, i) => (
              <Grid item xs={12} sm={4} key={i}>
                <Paper sx={{ p: 2 }}>
                  <Skeleton variant="text" width={120} />
                  <Skeleton variant="text" width={80} />
                  <Skeleton variant="text" width={160} />
                </Paper>
              </Grid>
            ))}
          </Grid>
        ) : (
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Stack direction="row" alignItems="center" spacing={1}>
                  <PeopleOutlineIcon color="primary" />
                  <Typography variant="overline" color="text.secondary">Total Clients</Typography>
                </Stack>
                <Typography variant="h4">{summary?.totalClients ?? 0}</Typography>
                <Typography variant="body2" color="text.secondary">Active: {summary?.activeClients ?? 0}</Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                  <Button size="small" component={RouterLink} to="/admin/clients" variant="outlined">Manage</Button>
                  <Button size="small" component={RouterLink} to="/admin/clients" variant="text">Search</Button>
                </Stack>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Stack direction="row" alignItems="center" spacing={1}>
                  <QuizIcon color="primary" />
                  <Typography variant="overline" color="text.secondary">Questionnaires</Typography>
                </Stack>
                <Typography variant="h4">{summary?.totalQuestionnaires ?? 0}</Typography>
                <Typography variant="body2" color="text.secondary">Assignments: {summary?.totalAssignments ?? 0}</Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                  <Button size="small" component={RouterLink} to="/admin/segments" variant="outlined">Segments</Button>
                  <Button size="small" component={RouterLink} to="/admin/questions" variant="text">Questions</Button>
                </Stack>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Stack direction="row" alignItems="center" spacing={1}>
                  <InsightsIcon color="primary" />
                  <Typography variant="overline" color="text.secondary">Assessments</Typography>
                </Stack>
                <Stack direction="row" spacing={1}>
                  <Chip label={`Pending: ${summary?.pendingAssessments ?? 0}`} />
                  <Chip color="success" label={`Completed: ${summary?.completedAssessments ?? 0}`} />
                </Stack>
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                  <Button size="small" component={RouterLink} to="/admin/submissions" variant="outlined">Submissions</Button>
                </Stack>
              </Paper>
            </Grid>
          </Grid>
        )}
      </Box>

      <Box sx={{ mt: 3 }}>
        <Typography variant="h6" gutterBottom>Quick Links</Typography>
        <Stack direction="row" spacing={2}>
          <Button variant="contained" component={RouterLink} to="/admin/clients">Manage Clients</Button>
          <Button variant="contained" color="secondary" component={RouterLink} to="/admin/catalog">Manage Catalog</Button>
          <Button variant="contained" component={RouterLink} to="/admin/submissions">View Submissions</Button>
          <Button variant="contained" component={RouterLink} to="/admin/segments">Manage Segments</Button>
          <Button variant="contained" component={RouterLink} to="/admin/questions">Manage Questions & Options</Button>
          <Button variant="outlined" component={RouterLink} to="/admin/assignments/create">Create Assignment</Button>
        </Stack>
      </Box>
    </Container>
  );
};

export default AdminDashboard;

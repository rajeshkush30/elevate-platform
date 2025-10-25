import React from 'react';
import { Container, Box, Typography, Button, Grid, Paper, CircularProgress, Alert } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useAdminAuth } from '../context/AuthContext';
import { getDashboardSummary, DashboardSummaryResponse } from '../api/admin';

const KpiCard: React.FC<{ title: string; value: number | string }> = ({ title, value }) => (
  <Paper elevation={1} sx={{ p: 2 }}>
    <Typography variant="subtitle2" color="text.secondary">{title}</Typography>
    <Typography variant="h5" sx={{ mt: 0.5 }}>{value}</Typography>
  </Paper>
);

const Dashboard: React.FC = () => {
  const { logout } = useAdminAuth();
  const [data, setData] = React.useState<DashboardSummaryResponse | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        setLoading(true);
        const res = await getDashboardSummary();
        if (mounted) {
          setData(res);
          setError(null);
        }
      } catch (e: any) {
        if (mounted) setError(e?.response?.data?.message || 'Failed to load dashboard summary');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  return (
    <Container>
      <Box sx={{ mt: 4, mb: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h4">Admin Dashboard</Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button component={RouterLink} to="/clients" variant="outlined">Clients</Button>
          <Button component={RouterLink} to="/submissions" variant="outlined">Submissions</Button>
          <Button component={RouterLink} to="/questions" variant="outlined">Questions</Button>
          <Button color="error" onClick={logout}>Logout</Button>
        </Box>
      </Box>

      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
          <CircularProgress />
        </Box>
      )}

      {!loading && error && (
        <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>
      )}

      {!loading && data && (
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6} md={4} lg={3}><KpiCard title="Total Clients" value={data.totalClients} /></Grid>
          <Grid item xs={12} sm={6} md={4} lg={3}><KpiCard title="Active Clients" value={data.activeClients} /></Grid>
          <Grid item xs={12} sm={6} md={4} lg={3}><KpiCard title="Questionnaires" value={data.totalQuestionnaires} /></Grid>
          <Grid item xs={12} sm={6} md={4} lg={3}><KpiCard title="Assignments" value={data.totalAssignments} /></Grid>
          <Grid item xs={12} sm={6} md={4} lg={3}><KpiCard title="Pending Assessments" value={data.pendingAssessments} /></Grid>
          <Grid item xs={12} sm={6} md={4} lg={3}><KpiCard title="Completed Assessments" value={data.completedAssessments} /></Grid>
        </Grid>
      )}
    </Container>
  );
};

export default Dashboard;

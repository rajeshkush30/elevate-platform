import { Alert, Container, Paper, Stack, Typography } from '@mui/material';

const AdminSettings = () => {
  const mode = import.meta.env.MODE;
  const apiBase = '/api';

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>Settings</Typography>
      <Stack spacing={2}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="subtitle1">Roles & Permissions</Typography>
          <Typography variant="body2" color="text.secondary">
            Read-only for now. Admin has full access. Clients have access to their own data and questionnaire.
          </Typography>
        </Paper>
        <Paper sx={{ p: 2 }}>
          <Typography variant="subtitle1">Email Templates</Typography>
          <Alert severity="info">Template editing UI pending backend support. Currently using server-side Thymeleaf templates.</Alert>
        </Paper>
        <Paper sx={{ p: 2 }}>
          <Typography variant="subtitle1">Environment</Typography>
          <Typography variant="body2">Mode: {mode}</Typography>
          <Typography variant="body2">API Base (proxied): {apiBase}</Typography>
        </Paper>
      </Stack>
    </Container>
  );
};

export default AdminSettings;

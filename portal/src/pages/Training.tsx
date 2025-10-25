import { Container, Card, CardContent, Typography, Stack, Button, Chip } from '@mui/material';

const Training = () => {
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>Training Assignments</Typography>
          <Typography variant="body2" color="text.secondary">
            Your assigned modules will appear here. This page will integrate with the LMS and show progress per module.
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
            <Chip label="Coming Soon" color="primary" />
            <Button variant="contained" disabled>View Module</Button>
          </Stack>
        </CardContent>
      </Card>
    </Container>
  );
};

export default Training;

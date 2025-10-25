import { Container, Card, CardContent, Typography, Stack, Button, Chip, TextField } from '@mui/material';

const Strategy = () => {
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>Strategy Planner</Typography>
          <Typography variant="body2" color="text.secondary">
            Draft your post-training strategy. In future, this will save to the backend and support collaboration.
          </Typography>
          <Stack spacing={2} sx={{ mt: 2 }}>
            <TextField label="Goals" placeholder="Define your top 3 goals" multiline rows={3} fullWidth />
            <TextField label="Key Actions" placeholder="List the actions you'll take" multiline rows={3} fullWidth />
            <TextField label="Metrics" placeholder="How will you measure progress?" multiline rows={2} fullWidth />
            <Stack direction="row" spacing={2}>
              <Button variant="contained" disabled>Save Draft</Button>
              <Chip label="Coming Soon" color="primary" />
            </Stack>
          </Stack>
        </CardContent>
      </Card>
    </Container>
  );
};

export default Strategy;

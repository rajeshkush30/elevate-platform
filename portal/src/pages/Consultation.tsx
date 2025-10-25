import { Container, Card, CardContent, Typography, Stack, Button, Chip, TextField } from '@mui/material';

const Consultation = () => {
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>AI Consultation</Typography>
          <Typography variant="body2" color="text.secondary">
            Chat with an AI assistant about your business assessment and next steps. Integration with the NLP engine will be added here.
          </Typography>
          <Stack spacing={2} sx={{ mt: 2 }}>
            <TextField label="Ask a question" placeholder="e.g., What should I prioritize next?" fullWidth />
            <Stack direction="row" spacing={2}>
              <Button variant="contained" disabled>Send</Button>
              <Chip label="Coming Soon" color="primary" />
            </Stack>
          </Stack>
        </CardContent>
      </Card>
    </Container>
  );
};

export default Consultation;

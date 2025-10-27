import { Container, Paper, Typography, Box, Button, CircularProgress } from '@mui/material';
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getStage, type StageView } from '../api/assessmentStage';

const AssessmentResult = () => {
  const { clientAssessmentId } = useParams();
  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<StageView | null>(null);

  useEffect(() => {
    (async () => {
      if (!clientAssessmentId) { setLoading(false); return; }
      try {
        const data = await getStage(clientAssessmentId);
        setResult(data);
      } finally {
        setLoading(false);
      }
    })();
  }, [clientAssessmentId]);

  if (loading) {
    return (
      <Container maxWidth="md">
        <Paper sx={{ p: 3, mt: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
          <CircularProgress size={20} />
          <Typography>Loading result...</Typography>
        </Paper>
      </Container>
    );
  }

  if (!result) {
    return (
      <Container maxWidth="md">
        <Paper sx={{ p: 3, mt: 3 }}>
          <Typography variant="h6">No result found</Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            Please take the assessment to see your result.
          </Typography>
          <Box sx={{ mt: 2 }}>
            <Button variant="contained" href="/questionnaire">
              Take Assessment
            </Button>
          </Box>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h5">Assessment Result</Typography>
        <Box sx={{ mt: 2 }}>
          <Typography variant="h6">Stage: {result.stage ?? '-'}</Typography>
          <Typography variant="body1" sx={{ mt: 1 }}>Score: {result.score ?? '-'}</Typography>
          <Typography variant="body1" sx={{ mt: 2 }}>{result.summary ?? 'Summary will appear once available.'}</Typography>
        </Box>
        <Box sx={{ mt: 3 }}>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            Next: Proceed to payment via Zoho to unlock your training for this stage.
          </Typography>
          <Typography variant="body2" color="text.secondary">
            You will receive a payment link by email from Zoho. After payment, click Go to Training and use the Start button. If payment is not reflected yet, try again in a moment.
          </Typography>
        </Box>
        <Box sx={{ mt: 3, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <Button variant="contained" href="/training">
            Go to Training
          </Button>
          <Button variant="outlined" href="/dashboard">
            Back to Dashboard
          </Button>
          <Button variant="text" href="/questionnaire">
            Retake Assessment
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default AssessmentResult;

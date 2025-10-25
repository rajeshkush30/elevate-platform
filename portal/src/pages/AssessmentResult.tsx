import { Container, Paper, Typography, Box, Button } from '@mui/material';
import { useEffect, useState } from 'react';

interface Result {
  submissionId: string;
  stage: string;
  score: number;
  summary: string;
}

const AssessmentResult = () => {
  const [result, setResult] = useState<Result | null>(null);

  useEffect(() => {
    try {
      const raw = localStorage.getItem('lastAssessmentResult');
      if (raw) setResult(JSON.parse(raw));
    } catch {}
  }, []);

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
        <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 1 }}>
          Submission ID: {result.submissionId}
        </Typography>
        <Box sx={{ mt: 2 }}>
          <Typography variant="h6">Stage: {result.stage}</Typography>
          <Typography variant="body1" sx={{ mt: 1 }}>Score: {result.score}</Typography>
          <Typography variant="body1" sx={{ mt: 2 }}>{result.summary}</Typography>
        </Box>
        <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
          <Button variant="contained" href="/dashboard">
            Back to Dashboard
          </Button>
          <Button variant="outlined" href="/questionnaire">
            Retake Assessment
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default AssessmentResult;

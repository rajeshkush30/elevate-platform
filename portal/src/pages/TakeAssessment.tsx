import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import {
  Container,
  Box,
  Typography,
  Button,
  Paper,
  RadioGroup,
  FormControlLabel,
  Radio,
  CircularProgress,
  Alert,
  Skeleton,
} from '@mui/material';
import questionnaireApi, { Question, Segment, QuestionnaireResponse } from '../api/questionnaire';
import { submitAnswers, finalizeAttempt } from '../api/clientAssessment';
import { useToast } from '../components/ToastProvider';

type FormValues = Record<string, string>;

const TakeAssessment: React.FC = () => {
  const { attemptId } = useParams();
  const navigate = useNavigate();
  const { control, handleSubmit } = useForm<FormValues>({ mode: 'onChange' });
  const [loading, setLoading] = useState(true);
  const [segments, setSegments] = useState<Segment[]>([]);
  const [error, setError] = useState<string | null>(null);
  const { showToast } = useToast();

  useEffect(() => {
    let mounted = true;
    (async () => {
      setLoading(true);
      try {
        const segs = await questionnaireApi.getSegments();
        if (mounted) setSegments(segs);
      } catch (e) {
        if (mounted) setError('Failed to load questions');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  const onSubmit = async (data: FormValues) => {
    if (!attemptId) return;
    setError(null);
    try {
      const answers = Object.keys(data).map((questionId) => ({ questionId, value: data[questionId] }));
      await submitAnswers(Number(attemptId), { answers });
      const result = await finalizeAttempt(Number(attemptId));
      try { localStorage.setItem('lastAssessmentResult', JSON.stringify({
        submissionId: String(result.attemptId),
        stage: result.stage,
        score: result.score,
        summary: result.summary,
        savedAt: new Date().toISOString(),
      })); } catch {}
      showToast('Assessment submitted successfully', 'success');
      navigate('/assessment/result');
    } catch (e: any) {
      setError(e?.message || 'Submission failed');
      showToast('Submission failed', 'error');
    }
  };

  if (loading) {
    return (
      <Container maxWidth="md">
        <Paper sx={{ p: 3, mt: 3 }}>
          <Skeleton variant="rectangular" height={28} width={200} />
          <Box sx={{ mt: 2 }}>
            {[...Array(3)].map((_, i) => (
              <Box key={i} sx={{ mb: 2 }}>
                <Skeleton variant="text" width="80%" />
                <Skeleton variant="rounded" height={56} />
              </Box>
            ))}
          </Box>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h5">Assessment</Typography>
        {error && (
          <Alert severity="error" sx={{ mt: 1 }}>{error}</Alert>
        )}
        <form onSubmit={handleSubmit(onSubmit)}>
          {segments.map((seg) => (
            <Box key={seg.id} sx={{ mt: 3 }}>
              <Typography variant="h6" sx={{ mb: 1 }}>{seg.name}</Typography>
              {seg.questions.map((q: Question) => (
                <Box key={q.id} sx={{ mt: 2, p: 2, borderRadius: 1, backgroundColor: '#fafafa' }}>
                  <Typography variant="subtitle1">{q.text}</Typography>
                  <Controller
                    name={q.id}
                    control={control}
                    defaultValue={q.options?.[0]?.value ?? '0'}
                    render={({ field }) => (
                      <RadioGroup {...field}>
                        {q.options?.map((opt) => (
                          <FormControlLabel key={opt.value} value={opt.value} control={<Radio />} label={opt.label} />
                        ))}
                      </RadioGroup>
                    )}
                  />
                </Box>
              ))}
            </Box>
          ))}

          <Box sx={{ mt: 3 }}>
            <Button type="submit" variant="contained" color="primary">
              Submit Assessment
            </Button>
          </Box>
        </form>
      </Paper>
    </Container>
  );
};

export default TakeAssessment;

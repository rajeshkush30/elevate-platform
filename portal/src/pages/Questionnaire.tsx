import React, { useEffect, useState } from 'react';
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
} from '@mui/material';
import questionnaireApi, { Question, Segment, QuestionnaireResponse } from '../api/questionnaire';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../components/ToastProvider';

type FormValues = Record<string, string>;

const Questionnaire: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { control, handleSubmit } = useForm<FormValues>({ mode: 'onChange' });
  const [loading, setLoading] = useState(true);
  const [segments, setSegments] = useState<Segment[]>([]);
  const [result, setResult] = useState<QuestionnaireResponse | null>(null);
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
        console.error('Failed to load segments; falling back to questions', e);
        if (mounted) setError('Failed to load from server. Using local questionnaire.');
        const qs = await questionnaireApi.getQuestions();
        if (mounted) setSegments([{ id: 'default', name: 'Assessment', order: 1, questions: qs }]);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const onSubmit = async (data: FormValues) => {
    setError(null);
    try {
      const answers = Object.keys(data).map((questionId) => ({ questionId, value: data[questionId] }));
      const resp = await questionnaireApi.submitAnswers({ userId: String(user?.id ?? ''), answers });
      setResult(resp);
      try {
        localStorage.setItem('lastAssessmentResult', JSON.stringify({
          ...resp,
          savedAt: new Date().toISOString(),
        }));
        // Append to local history for now
        const raw = localStorage.getItem('assessmentHistory');
        const arr = raw ? JSON.parse(raw) : [];
        arr.unshift({ ...resp, savedAt: new Date().toISOString() });
        localStorage.setItem('assessmentHistory', JSON.stringify(arr.slice(0, 20)));
      } catch {}
      showToast('Assessment submitted successfully', 'success');
      navigate('/assessment/result');
    } catch (e: any) {
      console.error(e);
      setError(e.message || 'Submission failed');
      showToast('Submission failed', 'error');
    }
  };

  if (loading) {
    return (
      <Container>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (result) {
    return (
      <Container maxWidth="md">
        <Paper sx={{ p: 3, mt: 3 }}>
          <Typography variant="h5">Assessment Result</Typography>
          <Typography variant="subtitle1" sx={{ mt: 1 }}>
            Stage: {result.stage}
          </Typography>
          <Typography variant="body1" sx={{ mt: 2 }}>
            {result.summary}
          </Typography>
          <Box sx={{ mt: 3 }}>
            <Button variant="contained" href="/dashboard">
              Back to Dashboard
            </Button>
          </Box>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h5">Business Assessment Questionnaire</Typography>
        {error && (
          <Typography color="error" sx={{ mt: 1 }}>
            {error}
          </Typography>
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

export default Questionnaire;

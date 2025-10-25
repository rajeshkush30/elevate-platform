import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Paper, Typography, Stack, Button, RadioGroup, FormControlLabel, Radio, TextField, CircularProgress, Alert } from '@mui/material';
import { getAssessmentDetails, saveAnswers, AssessmentQuestion } from '../api/myAssessments';
import questionnaireApi from '../api/questionnaire';
import { useToast } from '../components/ToastProvider';

export default function ClientAssessmentFill() {
  const { clientAssessmentId } = useParams();
  const { showToast } = useToast();
  const [questions, setQuestions] = useState<AssessmentQuestion[]>([]);
  const [answers, setAnswers] = useState<Record<number, { answerText?: string; optionId?: number }>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    (async () => {
      if (!clientAssessmentId) return;
      setLoading(true);
      setError(null);
      try {
        // Try dedicated details API first
        const qs = await getAssessmentDetails(clientAssessmentId);
        if (mounted && qs.length > 0) {
          setQuestions(qs);
          // Prefill answers from existingAnswer
          const initial: Record<number, { answerText?: string; optionId?: number }> = {};
          qs.forEach(q => {
            if (q.existingAnswer) {
              if (q.existingAnswer.optionIds && q.existingAnswer.optionIds.length > 0) {
                initial[q.id] = { optionId: Number(q.existingAnswer.optionIds[0]) };
              } else if (q.existingAnswer.answerText) {
                initial[q.id] = { answerText: q.existingAnswer.answerText };
              }
            }
          });
          setAnswers(initial);
          return;
        }
        // Fallback: use generic questionnaire segments API and flatten
        const segs = await questionnaireApi.getSegments();
        const flattened: AssessmentQuestion[] = [];
        segs.forEach(seg => {
          seg.questions.forEach(q => {
            flattened.push({
              id: Number(q.id),
              text: q.text,
              options: (q.options || []).map(o => ({ id: Number(o.value ?? 0), text: o.label })),
            });
          });
        });
        if (mounted) setQuestions(flattened);
      } catch (e: any) {
        if (mounted) setError(e?.message || 'Failed to load assessment');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, [clientAssessmentId]);

  const onSave = async (submit: boolean) => {
    if (!clientAssessmentId) return;
    try {
      const items = questions.map(q => {
        const a = answers[q.id] || {};
        const optionIds = a.optionId != null ? [a.optionId] : undefined;
        return { questionId: q.id, answerText: a.answerText, optionIds };
      });
      await saveAnswers(clientAssessmentId, items, submit);
      showToast(submit ? 'Submitted' : 'Saved', 'success');
    } catch (e: any) {
      showToast(e?.message || 'Save failed', 'error');
    }
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Stack direction="row" spacing={2} alignItems="center"><CircularProgress size={20} /><Typography>Loading...</Typography></Stack>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>Assessment</Typography>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Stack spacing={3}>
          {questions.map(q => (
            <div key={q.id}>
              <Typography variant="subtitle1" sx={{ mb: 1 }}>{q.text}</Typography>
              {q.options && q.options.length > 0 ? (
                <RadioGroup
                  value={answers[q.id]?.optionId ?? ''}
                  onChange={(e) => setAnswers(prev => ({ ...prev, [q.id]: { ...prev[q.id], optionId: Number(e.target.value) } }))}
                >
                  {q.options.map(o => (
                    <FormControlLabel key={o.id} value={o.id} control={<Radio />} label={o.text} />
                  ))}
                </RadioGroup>
              ) : (
                <TextField fullWidth value={answers[q.id]?.answerText ?? ''} onChange={(e) => setAnswers(prev => ({ ...prev, [q.id]: { ...prev[q.id], answerText: e.target.value } }))} />
              )}
            </div>
          ))}
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <Button variant="outlined" onClick={() => onSave(false)}>Save</Button>
            <Button variant="contained" onClick={() => onSave(true)}>Submit</Button>
          </Stack>
        </Stack>
      </Paper>
    </Container>
  );
}

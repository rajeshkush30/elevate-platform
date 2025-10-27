import { Container, Card, CardContent, Typography, Stack, Button, Chip, TextField, Checkbox, FormControlLabel, FormGroup, CircularProgress } from '@mui/material';
import { useEffect, useMemo, useState } from 'react';
import { getForm, submitAnswers, type StrategyForm } from '../api/strategy';

const Strategy = () => {
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState<StrategyForm | null>(null);
  const [answers, setAnswers] = useState<Record<number, { text?: string; optionIds?: number[] }>>({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const f = await getForm();
        setForm(f);
        // init answers map
        const init: Record<number, { text?: string; optionIds?: number[] }> = {};
        f.questions.forEach(q => { init[q.id] = { text: '', optionIds: [] }; });
        setAnswers(init);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const payload = useMemo(() => ({
    answers: Object.entries(answers).map(([qid, val]) => ({
      questionId: Number(qid),
      answerText: val.text && val.text.trim() ? val.text : undefined,
      optionIds: val.optionIds && val.optionIds.length ? val.optionIds : undefined,
    }))
  }), [answers]);

  const onSubmit = async () => {
    setSaving(true);
    try {
      await submitAnswers(payload);
      // minimal UX
      alert('Strategy saved');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return (
    <Container maxWidth="lg" sx={{ py: 4 }}><CircularProgress /></Container>
  );

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>Strategy Planner</Typography>
          <Typography variant="body2" color="text.secondary">
            Complete the post-training strategy form. Your inputs will be used to generate a consultation summary.
          </Typography>
          <Stack spacing={3} sx={{ mt: 2 }}>
            {form?.questions.map(q => (
              <Stack key={q.id} spacing={1}>
                <Typography variant="subtitle1">{q.text}</Typography>
                {q.type === 'TEXT' && (
                  <TextField
                    multiline rows={3}
                    value={answers[q.id]?.text ?? ''}
                    onChange={(e)=> setAnswers(prev => ({ ...prev, [q.id]: { ...prev[q.id], text: e.target.value } }))}
                    fullWidth
                  />
                )}
                {(q.type === 'MCQ' || q.type === 'SCALE') && (
                  <FormGroup>
                    {q.options?.map(opt => (
                      <FormControlLabel key={opt.id}
                        control={<Checkbox
                          checked={(answers[q.id]?.optionIds ?? []).includes(opt.id)}
                          onChange={(e)=> setAnswers(prev => {
                            const curr = new Set(prev[q.id]?.optionIds ?? []);
                            if (e.target.checked) { curr.clear(); curr.add(opt.id); } else { curr.delete(opt.id); }
                            return { ...prev, [q.id]: { ...prev[q.id], optionIds: Array.from(curr) } };
                          })}
                        />}
                        label={opt.label}
                      />
                    ))}
                  </FormGroup>
                )}
                {q.type === 'MCQ_MULTI' && (
                  <FormGroup>
                    {q.options?.map(opt => (
                      <FormControlLabel key={opt.id}
                        control={<Checkbox
                          checked={(answers[q.id]?.optionIds ?? []).includes(opt.id)}
                          onChange={(e)=> setAnswers(prev => {
                            const curr = new Set(prev[q.id]?.optionIds ?? []);
                            if (e.target.checked) curr.add(opt.id); else curr.delete(opt.id);
                            return { ...prev, [q.id]: { ...prev[q.id], optionIds: Array.from(curr) } };
                          })}
                        />}
                        label={opt.label}
                      />
                    ))}
                  </FormGroup>
                )}
              </Stack>
            ))}
            <Stack direction="row" spacing={2}>
              <Button variant="contained" onClick={onSubmit} disabled={saving}>{saving ? 'Saving...' : 'Save'}</Button>
              <Chip label={`Form ${form?.version ?? ''}`} />
            </Stack>
          </Stack>
        </CardContent>
      </Card>
    </Container>
  );
};

export default Strategy;

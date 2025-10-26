import { useEffect, useMemo, useState } from 'react';
import { Box, Button, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField, Typography, IconButton } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useNavigate, useParams } from 'react-router-dom';
import adminQuestionnaireCore from '../api/adminQuestionnaireCore';
import adminQuestionnaireCrud, { listOptionsByQuestion, createOption, deleteOption, AdminOption } from '../api/adminQuestionnaireCrud';
import catalog from '../api/catalog';

export default function AdminQuestionsManager() {
  const { questionnaireId } = useParams();
  const navigate = useNavigate();

  const [qInfo, setQInfo] = useState<{ id: string; name: string; version?: string } | null>(null);
  const [loading, setLoading] = useState(false);
  const [questions, setQuestions] = useState<Array<{ id: string; text: string; type?: string; weight?: number; segmentId?: string }>>([]);
  const [segments, setSegments] = useState<Array<{ id: string; name: string }>>([]);
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});
  const [optionsMap, setOptionsMap] = useState<Record<string, AdminOption[]>>({});
  const [optInputs, setOptInputs] = useState<Record<string, { label: string; value?: string; order?: number | '' }>>({});
  const [optLoading, setOptLoading] = useState<Record<string, boolean>>({});

  // Filters
  const [segmentFilter, setSegmentFilter] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('');

  // Create dialog
  const [open, setOpen] = useState(false);
  const [text, setText] = useState('');
  const [type, setType] = useState<'MCQ' | 'MCQ_MULTI' | 'SCALE' | 'TEXT'>('MCQ');
  const [weight, setWeight] = useState<number | ''>('');
  const [segmentId, setSegmentId] = useState<string>('');
  // interactive options builder for MCQ/SCALE
  const [newOptions, setNewOptions] = useState<Array<{ label: string; value?: string; order?: number | '' }>>([]);

  const load = async () => {
    if (!questionnaireId) return;
    setLoading(true);
    try {
      const [info, qs, tree] = await Promise.all([
        adminQuestionnaireCore.getQuestionnaire(questionnaireId),
        adminQuestionnaireCrud.listQuestionsByQuestionnaire(questionnaireId),
        catalog.getModuleTree(),
      ]);
      setQInfo(info);
      setQuestions(qs);
      const segs: Array<{ id: string; name: string }> = [];
      (tree || []).forEach((m: any) => (m.segments || []).forEach((s: any) => segs.push({ id: String(s.id), name: String(s.name ?? '') })));
      setSegments(segs);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [questionnaireId]);

  const create = async () => {
    if (!questionnaireId || !text.trim()) return;
    // build optionsJson if MCQ/MCQ_MULTI/SCALE and options provided via builder
    let optionsJson: string | undefined = undefined;
    if ((type === 'MCQ' || type === 'MCQ_MULTI' || type === 'SCALE') && newOptions.length > 0) {
      const arr = newOptions
        .filter(o => (o.label || '').trim())
        .map(o => ({ label: o.label.trim(), value: o.value ? String(o.value) : undefined, order: o.order === '' ? undefined : o.order }));
      if (arr.length > 0) optionsJson = JSON.stringify(arr);
      // guard: require at least one option label when MCQ/MCQ_MULTI/SCALE
      if (arr.length === 0) {
        alert('Please add at least one option (Label) for choice-type questions (MCQ / MCQ_MULTI / SCALE).');
        return;
      }
    }
    const payload = {
      questionnaireId,
      text: text.trim(),
      type,
      weight: weight === '' ? undefined : Number(weight),
      segmentId: segmentId || undefined,
      optionsJson,
    } as const;
    const newQuestionId = await adminQuestionnaireCrud.createQuestion(payload);
    // If options were entered in the builder, also persist them as option entities so they appear immediately
    if ((type === 'MCQ' || type === 'MCQ_MULTI' || type === 'SCALE') && newOptions.length > 0 && newQuestionId) {
      const items = newOptions
        .filter(o => (o.label || '').trim())
        .map(o => ({ label: o.label.trim(), value: o.value, order: o.order === '' ? undefined : o.order }));
      for (const opt of items) {
        try { await createOption(newQuestionId, opt); } catch { /* ignore single option errors */ }
      }
    }
    setOpen(false);
    setText(''); setType('MCQ'); setWeight(''); setSegmentId(''); setNewOptions([]);
    await load();
  };

  const title = useMemo(() => qInfo ? `${qInfo.name} (${qInfo.version || 'v1'})` : 'Questionnaire', [qInfo]);

  const filtered = useMemo(() => {
    return questions.filter(q => (
      (!segmentFilter || q.segmentId === segmentFilter) &&
      (!typeFilter || (q.type || '').toUpperCase() === typeFilter)
    ));
  }, [questions, segmentFilter, typeFilter]);

  return (
    <Box p={3}>
      <Stack spacing={0.5} mb={2}>
        <Typography variant="h5">Manage Questions</Typography>
        <Typography variant="subtitle2" color="text.secondary">{title}</Typography>
      </Stack>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ xs: 'stretch', sm: 'center' }} mb={2}>
        <Button variant="outlined" onClick={() => navigate('/admin/questionnaires')}>Back</Button>
        <Button variant="contained" onClick={() => { setNewOptions([{ label: '', value: '', order: '' }]); setOpen(true); }}>Add Question</Button>
        <TextField select size="small" label="Filter by segment" value={segmentFilter} onChange={(e)=>setSegmentFilter(String(e.target.value))} sx={{ minWidth: 220 }}>
          <MenuItem value="">All segments</MenuItem>
          {segments.map(s => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
        </TextField>
        <TextField select size="small" label="Filter by type" value={typeFilter} onChange={(e)=>setTypeFilter(String(e.target.value))} sx={{ minWidth: 220 }}>
          <MenuItem value="">All types</MenuItem>
          <MenuItem value="MCQ">MCQ</MenuItem>
          <MenuItem value="SCALE">SCALE</MenuItem>
          <MenuItem value="TEXT">TEXT</MenuItem>
        </TextField>
      </Stack>

      {loading ? (
        <Typography>Loading...</Typography>
      ) : (
        <Stack spacing={1}>
          {filtered.map(q => (
            <Card key={q.id} variant="outlined">
              <CardContent>
                <Stack spacing={1}>
                  <Typography fontWeight={600}>{q.text}</Typography>
                  <Typography variant="body2" color="text.secondary">Type: {q.type || '-'} · Weight: {q.weight ?? '-'} · Segment: {segments.find(s => s.id === q.segmentId)?.name || '-'}</Typography>
                  <Stack direction="row" spacing={1}>
                    <Button size="small" variant="text" onClick={async () => {
                      setExpanded(prev => ({ ...prev, [q.id]: !prev[q.id] }));
                      const nowExpanded = !expanded[q.id];
                      if (!nowExpanded) return;
                      if (optionsMap[q.id]) return;
                      setOptLoading(prev => ({ ...prev, [q.id]: true }));
                      try {
                        const opts = await listOptionsByQuestion(q.id);
                        setOptionsMap(prev => ({ ...prev, [q.id]: opts }));
                      } finally {
                        setOptLoading(prev => ({ ...prev, [q.id]: false }));
                      }
                    }}>{expanded[q.id] ? 'Hide options' : 'Manage options'}</Button>
                  </Stack>
                  {expanded[q.id] && (
                    <Stack spacing={1} sx={{ p: 1, border: '1px dashed', borderColor: 'divider', borderRadius: 1 }}>
                      {optLoading[q.id] ? (
                        <Typography variant="body2" color="text.secondary">Loading options...</Typography>
                      ) : (
                        <>
                          <Stack spacing={0.5}>
                            {(optionsMap[q.id] || []).map(o => (
                              <Stack key={o.id} direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ xs: 'stretch', sm: 'center' }}>
                                <Typography sx={{ minWidth: 220 }}>{o.label}</Typography>
                                <Typography variant="body2" color="text.secondary">Value: {o.value || '-'}</Typography>
                                <Typography variant="body2" color="text.secondary">Order: {o.order ?? '-'}</Typography>
                                <Button size="small" color="error" onClick={async () => {
                                  await deleteOption(o.id);
                                  setOptionsMap(prev => ({ ...prev, [q.id]: (prev[q.id] || []).filter(x => x.id !== o.id) }));
                                }}>Delete</Button>
                              </Stack>
                            ))}
                            {(optionsMap[q.id] || []).length === 0 && (
                              <Typography variant="body2" color="text.secondary">No options yet.</Typography>
                            )}
                          </Stack>
                          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ xs: 'stretch', sm: 'center' }}>
                            <TextField size="small" label="Label" value={optInputs[q.id]?.label || ''} onChange={(e)=> setOptInputs(prev => ({ ...prev, [q.id]: { ...prev[q.id], label: e.target.value } }))} sx={{ minWidth: 220 }} />
                            <TextField size="small" label="Value (optional)" value={optInputs[q.id]?.value || ''} onChange={(e)=> setOptInputs(prev => ({ ...prev, [q.id]: { ...prev[q.id], value: e.target.value } }))} />
                            <TextField size="small" label="Order (optional)" type="number" value={optInputs[q.id]?.order ?? ''} onChange={(e)=> setOptInputs(prev => ({ ...prev, [q.id]: { ...prev[q.id], order: e.target.value === '' ? '' : Number(e.target.value) } }))} sx={{ maxWidth: 160 }} />
                            <Button size="small" variant="contained" onClick={async () => {
                              const input = optInputs[q.id] || { label: '' };
                              if (!input.label?.trim()) return;
                              const created = await createOption(q.id, { label: input.label.trim(), value: input.value, order: input.order === '' ? undefined : input.order });
                              setOptionsMap(prev => ({ ...prev, [q.id]: [...(prev[q.id] || []), created] }));
                              setOptInputs(prev => ({ ...prev, [q.id]: { label: '', value: '', order: '' } }));
                            }}>Add option</Button>
                          </Stack>
                        </>
                      )}
                    </Stack>
                  )}
                </Stack>
              </CardContent>
            </Card>
          ))}
          {filtered.length === 0 && <Typography color="text.secondary">No questions found for the applied filters.</Typography>}
        </Stack>
      )}

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Add Question</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField label="Text" value={text} onChange={e=>setText(e.target.value)} fullWidth />
            <TextField label="Type" select value={type} onChange={e=>setType(e.target.value as any)}>
              <MenuItem value="MCQ">MCQ (single)</MenuItem>
              <MenuItem value="MCQ_MULTI">MCQ (multi-select)</MenuItem>
              <MenuItem value="SCALE">SCALE</MenuItem>
              <MenuItem value="TEXT">TEXT</MenuItem>
            </TextField>
            <TextField label="Weight" type="number" value={weight} onChange={e=>setWeight(e.target.value === '' ? '' : Number(e.target.value))} />
            <TextField label="Segment" select value={segmentId} onChange={e=>setSegmentId(String(e.target.value))} fullWidth>
              <MenuItem value="">(none)</MenuItem>
              {segments.map(s => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
            </TextField>
            {(type === 'MCQ' || type === 'MCQ_MULTI' || type === 'SCALE') && (
              <Box>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>Options</Typography>
                <Stack spacing={1}>
                  {newOptions.map((o, idx) => (
                    <Stack key={idx} direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ xs: 'stretch', sm: 'center' }}>
                      <TextField size="small" label="Label" value={o.label} onChange={(e)=> setNewOptions(prev => prev.map((x,i)=> i===idx? { ...x, label: e.target.value }: x))} sx={{ minWidth: 220 }} />
                      <TextField size="small" label="Value (optional)" value={o.value || ''} onChange={(e)=> setNewOptions(prev => prev.map((x,i)=> i===idx? { ...x, value: e.target.value }: x))} />
                      <TextField size="small" label="Order (optional)" type="number" value={o.order ?? ''} onChange={(e)=> setNewOptions(prev => prev.map((x,i)=> i===idx? { ...x, order: (e.target.value === '' ? '' : Number(e.target.value)) }: x))} sx={{ maxWidth: 160 }} />
                      <IconButton size="small" color="error" onClick={()=> setNewOptions(prev => prev.filter((_,i)=> i!==idx))}><DeleteIcon fontSize="small" /></IconButton>
                    </Stack>
                  ))}
                  <Button size="small" variant="outlined" onClick={()=> setNewOptions(prev => [...prev, { label: '', value: '', order: '' }])}>Add option</Button>
                </Stack>
              </Box>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={()=>setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={create}>Create</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

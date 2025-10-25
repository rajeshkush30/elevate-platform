import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Box,
  Button,
  Container,
  IconButton,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import DeleteIcon from '@mui/icons-material/Delete';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import DownloadIcon from '@mui/icons-material/Download';
import { Snackbar, Alert, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';
import adminApi, {
  SegmentSummary,
  QuestionSummary,
  OptionSummary,
} from '../api/adminQuestionnaire';
import { Link as RouterLink } from 'react-router-dom';

const AdminQuestions = () => {
  const [segments, setSegments] = useState<SegmentSummary[]>([]);
  const [activeSegmentId, setActiveSegmentId] = useState<string>('');
  const [questions, setQuestions] = useState<QuestionSummary[]>([]);
  const [options, setOptions] = useState<Record<string, OptionSummary[]>>({});
  const [editingQ, setEditingQ] = useState<Record<string, { text: string; weight: number | ''; order: number | '' }>>({});
  const [editingO, setEditingO] = useState<Record<string, { label: string; value: string; order: number | '' }>>({});
  const [confirm, setConfirm] = useState<{ type: 'question' | 'option' | null; id?: string; parentId?: string; name?: string }>({ type: null });
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });

  // new question form
  const [qText, setQText] = useState('');
  const [qWeight, setQWeight] = useState<number | ''>('');
  const [qOrder, setQOrder] = useState<number | ''>('');

  // new option forms per question
  const [newOpt, setNewOpt] = useState<Record<string, { label: string; value: string; order: number | '' }>>({});
  // DnD refs
  const dragQIndexRef = useRef<number | null>(null);
  const dragOptIndexRef = useRef<Record<string, number | null>>({});
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const loadSegments = async () => {
    const segs = await adminApi.listSegments();
    setSegments(segs);
    if (!activeSegmentId && segs.length) {
      setActiveSegmentId(segs[0].id);
    }
  };

  // DnD handlers for questions
  const onQDragStart = (index: number) => () => { dragQIndexRef.current = index; };
  const onDragOver = (e: React.DragEvent) => { e.preventDefault(); };
  const onQDrop = (index: number) => async (e: React.DragEvent) => {
    e.preventDefault();
    if (!activeSegmentId) return;
    const from = dragQIndexRef.current; dragQIndexRef.current = null;
    if (from == null || from === index) return;
    const arr = [...sortedQs];
    const [moved] = arr.splice(from, 1);
    arr.splice(index, 0, moved);
    const items = arr.map((q, i) => ({ id: q.id, order: i + 1 }));
    await adminApi.reorderQuestions(activeSegmentId, items);
    await loadQuestions(activeSegmentId);
    setToast({ open: true, message: 'Questions reordered', severity: 'success' });
  };

  // DnD handlers for options per question
  const onOptDragStart = (qid: string, index: number) => () => { dragOptIndexRef.current[qid] = index; };
  const onOptDrop = (qid: string, index: number) => async (e: React.DragEvent) => {
    e.preventDefault();
    const from = dragOptIndexRef.current[qid]; dragOptIndexRef.current[qid] = null;
    const list = options[qid] || [];
    if (from == null || from === index || list.length === 0) return;
    const arr = [...list];
    const [moved] = arr.splice(from, 1);
    arr.splice(index, 0, moved);
    const items = arr.map((o, i) => ({ id: o.id, order: i + 1 }));
    await adminApi.reorderOptions(qid, items);
    await loadOptionsFor(qid);
    setToast({ open: true, message: 'Options reordered', severity: 'success' });
  };

  // Export/Import JSON for current segment
  const exportJson = async () => {
    if (!activeSegmentId) return;
    // Ensure options are loaded for all questions
    const qs = sortedQs;
    const map: Record<string, OptionSummary[]> = {};
    for (const q of qs) {
      if (!options[q.id]) {
        await loadOptionsFor(q.id);
      }
      map[q.id] = options[q.id] || [];
    }
    const payload = {
      segmentId: activeSegmentId,
      segmentName: activeSegment?.name,
      questions: qs.map(q => ({
        text: q.text,
        weight: q.weight ?? null,
        order: q.order ?? null,
        options: (map[q.id] || []).map(o => ({ label: o.label, value: o.value, order: o.order ?? null }))
      }))
    };
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `questions_${activeSegment?.name || activeSegmentId}_${new Date().toISOString()}.json`; a.click();
    URL.revokeObjectURL(url);
  };

  const onImportFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file || !activeSegmentId) return;
    try {
      const text = await file.text();
      const json = JSON.parse(text);
      const arr: Array<{ text: string; weight?: number; order?: number; options?: Array<{ label: string; value: string; order?: number }> }> = json.questions || json || [];
      if (!Array.isArray(arr)) throw new Error('Invalid JSON');
      // Create missing questions
      await loadQuestions(activeSegmentId);
      const existingQs = await adminApi.listQuestions(activeSegmentId);
      const byText: Record<string, QuestionSummary> = {};
      existingQs.forEach(q => { byText[q.text.trim().toLowerCase()] = q; });
      for (const q of arr) {
        const key = (q.text || '').trim().toLowerCase();
        if (!key) continue;
        if (!byText[key]) {
          const created = await adminApi.createQuestion(activeSegmentId, { text: q.text, weight: q.weight, order: q.order });
          byText[key] = created;
        }
      }
      // Reorder questions by given order
      const refreshed = await adminApi.listQuestions(activeSegmentId);
      const orderItems = arr
        .filter(q => q.text && refreshed.find(r => r.text.trim().toLowerCase() === q.text.trim().toLowerCase()))
        .sort((a, b) => (a.order ?? 1e9) - (b.order ?? 1e9))
        .map((q, idx) => {
          const r = refreshed.find(r => r.text.trim().toLowerCase() === q.text.trim().toLowerCase())!;
          return { id: r.id, order: idx + 1 };
        });
      if (orderItems.length > 0) await adminApi.reorderQuestions(activeSegmentId, orderItems);

      // Create/reorder options per question
      const finalQs = await adminApi.listQuestions(activeSegmentId);
      for (const q of arr) {
        const fq = finalQs.find(r => r.text.trim().toLowerCase() === (q.text || '').trim().toLowerCase());
        if (!fq) continue;
        const opts = await adminApi.listOptions(fq.id);
        const optByLabel: Record<string, OptionSummary> = {};
        opts.forEach(o => { optByLabel[o.label.trim().toLowerCase()] = o; });
        for (const o of (q.options || [])) {
          const key = (o.label || '').trim().toLowerCase();
          if (!key) continue;
          if (!optByLabel[key]) {
            await adminApi.createOption(fq.id, { label: o.label, value: String(o.value ?? ''), order: o.order });
          }
        }
        // reorder
        const fopts = await adminApi.listOptions(fq.id);
        const orderOptItems = (q.options || [])
          .filter(o => o.label && fopts.find(x => x.label.trim().toLowerCase() === o.label.trim().toLowerCase()))
          .sort((a, b) => (a.order ?? 1e9) - (b.order ?? 1e9))
          .map((o, idx) => {
            const x = fopts.find(x => x.label.trim().toLowerCase() === o.label.trim().toLowerCase())!;
            return { id: x.id, order: idx + 1 };
          });
        if (orderOptItems.length > 0) await adminApi.reorderOptions(fq.id, orderOptItems);
      }

      await loadQuestions(activeSegmentId);
      setToast({ open: true, message: 'Questions imported', severity: 'success' });
    } catch (err) {
      setToast({ open: true, message: 'Failed to import questions', severity: 'error' });
    }
  };

  const loadQuestions = async (segmentId: string) => {
    if (!segmentId) { setQuestions([]); return; }
    const qs = await adminApi.listQuestions(segmentId);
    setQuestions(qs);
  };

  const loadOptionsFor = async (questionId: string) => {
    const opts = await adminApi.listOptions(questionId);
    setOptions(prev => ({ ...prev, [questionId]: opts }));
  };

  useEffect(() => { loadSegments(); }, []);
  useEffect(() => { if (activeSegmentId) loadQuestions(activeSegmentId); }, [activeSegmentId]);

  const activeSegment = useMemo(() => segments.find(s => s.id === activeSegmentId), [segments, activeSegmentId]);

  const addQuestion = async () => {
    if (!activeSegmentId || !qText.trim()) return;
    await adminApi.createQuestion(activeSegmentId, { text: qText.trim(), weight: qWeight === '' ? undefined : Number(qWeight), order: qOrder === '' ? undefined : Number(qOrder) });
    setQText(''); setQWeight(''); setQOrder('');
    await loadQuestions(activeSegmentId);
  };

  const removeQuestion = async (id: string) => {
    await adminApi.deleteQuestion(id);
    const segId = activeSegmentId;
    await loadQuestions(segId);
    setToast({ open: true, message: 'Question deleted', severity: 'success' });
  };

  const addOption = async (questionId: string) => {
    const form = newOpt[questionId] || { label: '', value: '', order: '' };
    if (!form.label.trim()) return;
    await adminApi.createOption(questionId, { label: form.label.trim(), value: String(form.value ?? '').trim(), order: form.order === '' ? undefined : Number(form.order) });
    // reset input for that question
    setNewOpt(prev => ({ ...prev, [questionId]: { label: '', value: '', order: '' } }));
    await loadOptionsFor(questionId);
    setToast({ open: true, message: 'Option added', severity: 'success' });
  };

  const removeOption = async (questionId: string, optionId: string) => {
    await adminApi.deleteOption(optionId);
    await loadOptionsFor(questionId);
    setToast({ open: true, message: 'Option deleted', severity: 'success' });
  };

  const startEditQ = (q: QuestionSummary) => {
    setEditingQ(prev => ({ ...prev, [q.id]: { text: q.text, weight: q.weight ?? '', order: q.order ?? '' } }));
  };

  const saveEditQ = async (q: QuestionSummary) => {
    const form = editingQ[q.id];
    if (!form) return;
    await adminApi.updateQuestion(q.id, { text: form.text.trim(), weight: form.weight === '' ? undefined : Number(form.weight), order: form.order === '' ? undefined : Number(form.order) });
    setEditingQ(prev => { const p = { ...prev }; delete p[q.id]; return p; });
    await loadQuestions(activeSegmentId);
    setToast({ open: true, message: 'Question updated', severity: 'success' });
  };

  const startEditO = (o: OptionSummary) => {
    setEditingO(prev => ({ ...prev, [o.id]: { label: o.label, value: o.value, order: o.order ?? '' } }));
  };

  const saveEditO = async (o: OptionSummary) => {
    const form = editingO[o.id];
    if (!form) return;
    await adminApi.updateOption(o.id, { label: form.label.trim(), value: String(form.value ?? '').trim(), order: form.order === '' ? undefined : Number(form.order) });
    setEditingO(prev => { const p = { ...prev }; delete p[o.id]; return p; });
    await loadOptionsFor(o.questionId);
    setToast({ open: true, message: 'Option updated', severity: 'success' });
  };

  const sortedQs = useMemo(() => [...questions].sort((a, b) => (a.order ?? 1e9) - (b.order ?? 1e9)), [questions]);

  const moveQ = async (id: string, direction: 'up' | 'down') => {
    const idx = sortedQs.findIndex(q => q.id === id);
    if (idx === -1) return;
    const swapIdx = direction === 'up' ? idx - 1 : idx + 1;
    if (swapIdx < 0 || swapIdx >= sortedQs.length) return;
    const a = sortedQs[idx];
    const b = sortedQs[swapIdx];
    const aOrder = a.order ?? (idx + 1);
    const bOrder = b.order ?? (swapIdx + 1);
    await adminApi.updateQuestion(a.id, { text: a.text, weight: a.weight, order: bOrder });
    await adminApi.updateQuestion(b.id, { text: b.text, weight: b.weight, order: aOrder });
    await loadQuestions(activeSegmentId);
    setToast({ open: true, message: 'Question order updated', severity: 'success' });
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'stretch', sm: 'center' }} sx={{ mb: 3 }} spacing={2}>
        <Typography variant="h5">Manage Questions & Options</Typography>
        <Stack direction="row" spacing={2}>
          <Select
            size="small"
            value={activeSegmentId}
            onChange={(e) => setActiveSegmentId(String(e.target.value))}
            displayEmpty
            sx={{ minWidth: 240 }}
          >
            <MenuItem value="" disabled>Select segment...</MenuItem>
            {segments.map(seg => (
              <MenuItem key={seg.id} value={seg.id}>{seg.name}</MenuItem>
            ))}
          </Select>
        </Stack>
      </Stack>

      {segments.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}
          action={
            <Button color="inherit" size="small" component={RouterLink} to="/admin/segments">
              Create segment
            </Button>
          }
        >
          No segments found. Create a segment first to add questions.
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>Add Question {activeSegment ? `to "${activeSegment.name}"` : ''}</Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField label="Question text" fullWidth value={qText} onChange={(e) => setQText(e.target.value)} />
          <TextField label="Weight" type="number" value={qWeight} onChange={(e) => setQWeight(e.target.value === '' ? '' : Number(e.target.value))} sx={{ width: 140 }} />
          <TextField label="Order" type="number" value={qOrder} onChange={(e) => setQOrder(e.target.value === '' ? '' : Number(e.target.value))} sx={{ width: 140 }} />
          <Button startIcon={<AddIcon />} variant="contained" onClick={addQuestion} disabled={segments.length === 0 || !activeSegmentId || !qText.trim()}>Add</Button>
          <input ref={fileInputRef} type="file" accept="application/json" hidden onChange={onImportFile} />
          <Button startIcon={<DownloadIcon />} disabled={!activeSegmentId || sortedQs.length===0} onClick={exportJson}>Export JSON</Button>
          <Button startIcon={<UploadFileIcon />} disabled={!activeSegmentId} onClick={() => fileInputRef.current?.click()}>Import JSON</Button>
        </Stack>
      </Paper>

      {sortedQs.map((q, idx) => (
        <Paper key={q.id} sx={{ p: 2, mb: 2 }} draggable={!editingQ[q.id]} onDragStart={onQDragStart(idx)} onDragOver={onDragOver} onDrop={onQDrop(idx)}>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
              {editingQ[q.id] ? (
                <>
                  <TextField size="small" label="Text" value={editingQ[q.id].text} onChange={(e) => setEditingQ(prev => ({ ...prev, [q.id]: { ...prev[q.id], text: e.target.value } }))} sx={{ minWidth: 320 }} />
                  <TextField size="small" label="Weight" type="number" value={editingQ[q.id].weight} onChange={(e) => setEditingQ(prev => ({ ...prev, [q.id]: { ...prev[q.id], weight: e.target.value === '' ? '' : Number(e.target.value) } }))} sx={{ width: 120 }} />
                  <TextField size="small" label="Order" type="number" value={editingQ[q.id].order} onChange={(e) => setEditingQ(prev => ({ ...prev, [q.id]: { ...prev[q.id], order: e.target.value === '' ? '' : Number(e.target.value) } }))} sx={{ width: 120 }} />
                </>
              ) : (
                <>
                  <Typography variant="subtitle1">{q.text}</Typography>
                  <Typography variant="body2" color="text.secondary">Weight: {q.weight ?? '-'} · Order: {q.order ?? '-'}</Typography>
                </>
              )}
            </Box>
            <Stack direction="row" spacing={1}>
              {!editingQ[q.id] ? (
                <>
                  <IconButton disabled={idx === 0} onClick={() => moveQ(q.id, 'up')}><ArrowUpwardIcon /></IconButton>
                  <IconButton disabled={idx === sortedQs.length - 1} onClick={() => moveQ(q.id, 'down')}><ArrowDownwardIcon /></IconButton>
                  <IconButton onClick={() => startEditQ(q)}><EditIcon /></IconButton>
                  <IconButton color="error" onClick={() => setConfirm({ type: 'question', id: q.id, name: q.text })}><DeleteIcon /></IconButton>
                </>
              ) : (
                <>
                  <IconButton color="primary" onClick={() => saveEditQ(q)}><SaveIcon /></IconButton>
                  <Button onClick={() => setEditingQ(prev => { const p = { ...prev }; delete p[q.id]; return p; })}>Cancel</Button>
                </>
              )}
            </Stack>
          </Stack>

          <Box sx={{ mt: 2 }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Options</Typography>
            <Stack direction="row" flexWrap="wrap" gap={1}>
              {(options[q.id] || []).map((o, oidx) => (
                <Stack key={o.id} direction="row" alignItems="center" spacing={1} sx={{ p: 1, bgcolor: '#f7f7f7', borderRadius: 1 }} draggable={!editingO[o.id]} onDragStart={onOptDragStart(q.id, oidx)} onDragOver={onDragOver} onDrop={onOptDrop(q.id, oidx)}>
                  {editingO[o.id] ? (
                    <>
                      <TextField size="small" label="Label" value={editingO[o.id].label} onChange={(e) => setEditingO(prev => ({ ...prev, [o.id]: { ...prev[o.id], label: e.target.value } }))} />
                      <TextField size="small" label="Value" value={editingO[o.id].value} onChange={(e) => setEditingO(prev => ({ ...prev, [o.id]: { ...prev[o.id], value: e.target.value } }))} />
                      <TextField size="small" label="Order" type="number" value={editingO[o.id].order} onChange={(e) => setEditingO(prev => ({ ...prev, [o.id]: { ...prev[o.id], order: e.target.value === '' ? '' : Number(e.target.value) } }))} sx={{ width: 110 }} />
                      <IconButton size="small" color="primary" onClick={() => saveEditO(o)}><SaveIcon fontSize="small" /></IconButton>
                      <Button size="small" onClick={() => setEditingO(prev => { const p = { ...prev }; delete p[o.id]; return p; })}>Cancel</Button>
                    </>
                  ) : (
                    <>
                      <Typography variant="body2">{o.label} ({o.value}) • {o.order ?? '-'}</Typography>
                      <IconButton size="small" onClick={() => startEditO(o)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton size="small" color="error" onClick={() => setConfirm({ type: 'option', id: o.id, parentId: q.id, name: o.label })}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </>
                  )}
                </Stack>
              ))}
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mt: 1 }}>
              <TextField size="small" label="Label" value={newOpt[q.id]?.label ?? ''} onChange={(e) => setNewOpt(prev => ({ ...prev, [q.id]: { ...(prev[q.id] || { label: '', value: '', order: '' }), label: e.target.value } }))} />
              <TextField size="small" label="Value" value={newOpt[q.id]?.value ?? ''} onChange={(e) => setNewOpt(prev => ({ ...prev, [q.id]: { ...(prev[q.id] || { label: '', value: '', order: '' }), value: e.target.value } }))} />
              <TextField size="small" label="Order" type="number" value={newOpt[q.id]?.order ?? ''} onChange={(e) => setNewOpt(prev => ({ ...prev, [q.id]: { ...(prev[q.id] || { label: '', value: '', order: '' }), order: e.target.value === '' ? '' : Number(e.target.value) } }))} />
              <Button size="small" variant="outlined" onClick={() => addOption(q.id)}>Add Option</Button>
              <Button size="small" variant="text" onClick={() => loadOptionsFor(q.id)}>Refresh</Button>
            </Stack>
          </Box>
        </Paper>
      ))}

      {questions.length === 0 && (
        <Typography color="text.secondary">{activeSegment ? 'No questions yet for this segment.' : 'Select a segment to manage questions.'}</Typography>
      )}

      <Dialog open={!!confirm.type} onClose={() => setConfirm({ type: null })}>
        <DialogTitle>Delete {confirm.type === 'question' ? 'Question' : 'Option'}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete {confirm.type === 'question' ? 'question' : 'option'} "{confirm.name}"? This cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm({ type: null })}>Cancel</Button>
          <Button color="error" onClick={async () => {
            if (confirm.type === 'question' && confirm.id) { await removeQuestion(confirm.id); }
            if (confirm.type === 'option' && confirm.id && confirm.parentId) { await removeOption(confirm.parentId, confirm.id); }
            setConfirm({ type: null });
          }}>Delete</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={toast.open} autoHideDuration={3000} onClose={() => setToast(t => ({ ...t, open: false }))}>
        <Alert onClose={() => setToast(t => ({ ...t, open: false }))} severity={toast.severity} sx={{ width: '100%' }}>
          {toast.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default AdminQuestions;

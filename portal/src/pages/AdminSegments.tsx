import { useEffect, useMemo, useRef, useState } from 'react';
import { Box, Button, Container, Paper, Stack, TextField, Typography, IconButton, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Snackbar, Alert, Tooltip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import adminQuestionnaireApi, { SegmentSummary } from '../api/adminQuestionnaire';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import DownloadIcon from '@mui/icons-material/Download';

const AdminSegments = () => {
  const [segments, setSegments] = useState<SegmentSummary[]>([]);
  const [name, setName] = useState('');
  const [order, setOrder] = useState<number | ''>('');
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Record<string, { name: string; order: number | '' }>>({});
  const [confirm, setConfirm] = useState<{ id: string | null; name?: string }>({ id: null });
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>(() => ({ open: false, message: '', severity: 'success' }));
  const dragIndexRef = useRef<number | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const data = await adminQuestionnaireApi.listSegments();
      setSegments(data);
    } finally {
      setLoading(false);
    }
  };

  // DnD handlers
  const onDragStart = (index: number) => () => { dragIndexRef.current = index; };
  const onDragOver = (e: React.DragEvent) => { e.preventDefault(); };
  const onDrop = (index: number) => async (e: React.DragEvent) => {
    e.preventDefault();
    const from = dragIndexRef.current;
    dragIndexRef.current = null;
    if (from == null || from === index) return;
    const newList = [...sorted];
    const [moved] = newList.splice(from, 1);
    newList.splice(index, 0, moved);
    // compute new orders as 1..n
    const items = newList.map((s, i) => ({ id: s.id, order: i + 1 }));
    await adminQuestionnaireApi.reorderSegments(items);
    await load();
    setToast({ open: true, message: 'Segments reordered', severity: 'success' });
  };

  // Export/Import
  const exportJson = () => {
    const data = sorted.map(s => ({ id: s.id, name: s.name, order: s.order ?? null }));
    const blob = new Blob([JSON.stringify({ segments: data }, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `segments_${new Date().toISOString()}.json`; a.click();
    URL.revokeObjectURL(url);
  };

  const onImportFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    try {
      const text = await file.text();
      const json = JSON.parse(text);
      const arr: Array<{ name: string; order?: number }> = json.segments || json || [];
      if (!Array.isArray(arr)) throw new Error('Invalid JSON');
      // Create segments that don't exist by name
      for (const item of arr) {
        const exists = segments.find(s => s.name.trim().toLowerCase() === (item.name || '').trim().toLowerCase());
        if (!exists && item.name) {
          await adminQuestionnaireApi.createSegment({ name: item.name, order: item.order });
        }
      }
      // Reload and then reorder according to provided order
      const fresh = await adminQuestionnaireApi.listSegments();
      const byName: Record<string, string> = {};
      fresh.forEach(s => { byName[s.name.trim().toLowerCase()] = s.id; });
      const items = arr
        .filter(i => i.name && byName[i.name.trim().toLowerCase()])
        .sort((a, b) => (a.order ?? 1e9) - (b.order ?? 1e9))
        .map((i, idx) => ({ id: byName[i.name.trim().toLowerCase()], order: idx + 1 }));
      if (items.length > 0) await adminQuestionnaireApi.reorderSegments(items);
      await load();
      setToast({ open: true, message: 'Segments imported', severity: 'success' });
    } catch (err) {
      setToast({ open: true, message: 'Failed to import segments', severity: 'error' });
    }
  };

  useEffect(() => {
    load();
  }, []);

  const create = async () => {
    if (!name.trim()) return;
    await adminQuestionnaireApi.createSegment({ name: name.trim(), order: order === '' ? undefined : Number(order) });
    setName('');
    setOrder('');
    await load();
  };

  const remove = async (id: string) => {
    await adminQuestionnaireApi.deleteSegment(id);
    await load();
    setToast({ open: true, message: 'Segment deleted', severity: 'success' });
  };

  const startEdit = (s: SegmentSummary) => {
    setEditing((prev) => ({ ...prev, [s.id]: { name: s.name, order: s.order ?? '' } }));
  };

  const saveEdit = async (s: SegmentSummary) => {
    const form = editing[s.id];
    if (!form) return;
    await adminQuestionnaireApi.updateSegment(s.id, { name: form.name.trim(), order: form.order === '' ? undefined : Number(form.order) });
    setEditing((prev) => { const p = { ...prev }; delete p[s.id]; return p; });
    await load();
    setToast({ open: true, message: 'Segment updated', severity: 'success' });
  };

  const sorted = useMemo(() => {
    return [...segments].sort((a, b) => (a.order ?? 1e9) - (b.order ?? 1e9));
  }, [segments]);

  const move = async (id: string, direction: 'up' | 'down') => {
    const idx = sorted.findIndex((s) => s.id === id);
    if (idx === -1) return;
    const swapIdx = direction === 'up' ? idx - 1 : idx + 1;
    if (swapIdx < 0 || swapIdx >= sorted.length) return;
    const a = sorted[idx];
    const b = sorted[swapIdx];
    const aOrder = a.order ?? (idx + 1);
    const bOrder = b.order ?? (swapIdx + 1);
    await adminQuestionnaireApi.updateSegment(a.id, { name: a.name, order: bOrder });
    await adminQuestionnaireApi.updateSegment(b.id, { name: b.name, order: aOrder });
    await load();
    setToast({ open: true, message: 'Order updated', severity: 'success' });
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>Manage Segments</Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField label="Name" fullWidth value={name} onChange={(e) => setName(e.target.value)} />
          <TextField label="Order" type="number" value={order} onChange={(e) => setOrder(e.target.value === '' ? '' : Number(e.target.value))} />
          <Button variant="contained" onClick={create} disabled={loading || !name.trim()}>Add</Button>
          <Tooltip title="Export segments as JSON"><span><Button startIcon={<DownloadIcon />} onClick={exportJson} disabled={sorted.length===0}>Export</Button></span></Tooltip>
          <input ref={fileInputRef} type="file" accept="application/json" hidden onChange={onImportFile} />
          <Button startIcon={<UploadFileIcon />} onClick={() => fileInputRef.current?.click()}>Import</Button>
        </Stack>
      </Paper>

      <Paper sx={{ p: 2 }}>
        {sorted.map((s, i) => {
          const e = editing[s.id];
          return (
            <Stack key={s.id} direction="row" alignItems="center" justifyContent="space-between" sx={{ py: 1, borderBottom: '1px solid #eee' }}
              draggable={!e}
              onDragStart={onDragStart(i)}
              onDragOver={onDragOver}
              onDrop={onDrop(i)}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flex: 1 }}>
                {e ? (
                  <>
                    <TextField size="small" label="Name" value={e.name} onChange={(ev) => setEditing(prev => ({ ...prev, [s.id]: { ...prev[s.id], name: ev.target.value } }))} />
                    <TextField size="small" label="Order" type="number" value={e.order} onChange={(ev) => setEditing(prev => ({ ...prev, [s.id]: { ...prev[s.id], order: ev.target.value === '' ? '' : Number(ev.target.value) } }))} sx={{ width: 120 }} />
                  </>
                ) : (
                  <Box>
                    <Typography variant="subtitle1">{s.name}</Typography>
                    <Typography variant="body2" color="text.secondary">Order: {s.order ?? '-'}</Typography>
                  </Box>
                )}
              </Box>
              <Stack direction="row" spacing={1}>
                {!e ? (
                  <>
                    <IconButton disabled={i === 0} onClick={() => move(s.id, 'up')}><ArrowUpwardIcon /></IconButton>
                    <IconButton disabled={i === sorted.length - 1} onClick={() => move(s.id, 'down')}><ArrowDownwardIcon /></IconButton>
                    <IconButton onClick={() => startEdit(s)}><EditIcon /></IconButton>
                    <IconButton color="error" onClick={() => setConfirm({ id: s.id, name: s.name })}><DeleteIcon /></IconButton>
                  </>
                ) : (
                  <>
                    <IconButton color="primary" onClick={() => saveEdit(s)}><SaveIcon /></IconButton>
                    <Button onClick={() => setEditing(prev => { const p = { ...prev }; delete p[s.id]; return p; })}>Cancel</Button>
                  </>
                )}
              </Stack>
            </Stack>
          );
        })}
        {sorted.length === 0 && <Typography color="text.secondary">No segments yet.</Typography>}
      </Paper>

      <Dialog open={!!confirm.id} onClose={() => setConfirm({ id: null })}>
        <DialogTitle>Delete Segment</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete segment "{confirm.name}"? This cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm({ id: null })}>Cancel</Button>
          <Button color="error" onClick={async () => { if (confirm.id) { await remove(confirm.id); setConfirm({ id: null }); } }}>Delete</Button>
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

export default AdminSegments;

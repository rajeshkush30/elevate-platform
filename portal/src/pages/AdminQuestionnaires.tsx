import { useEffect, useState } from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, Snackbar, Alert, Stack, TextField, Typography, Card, CardContent, CardActions, Tooltip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { Link, useNavigate } from 'react-router-dom';
import adminQuestionnaireCore from '../api/adminQuestionnaireCore';
import adminQuestionnaireCrud from '../api/adminQuestionnaireCrud';

export default function AdminQuestionnaires() {
  const [items, setItems] = useState<Array<{id:string; name:string; version?:string}>>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [version, setVersion] = useState('v1');
  const [query, setQuery] = useState('');
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const list = await adminQuestionnaireCore.listQuestionnaires();
      setItems(list);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const create = async () => {
    if (!name.trim()) return;
    const id = await adminQuestionnaireCore.createQuestionnaire({ name: name.trim(), version: version.trim() || undefined });
    setOpen(false);
    setName('');
    setVersion('v1');
    await load();
    navigate(`/admin/questionnaires/${id}/questions`);
  };

  const remove = async (id: string) => {
    await adminQuestionnaireCore.deleteQuestionnaire(id);
    setToast({ open: true, message: 'Questionnaire deleted', severity: 'success' });
    await load();
  };

  const duplicate = async (id: string) => {
    try {
      const source = items.find(x => x.id === id);
      if (!source) return;
      const newId = await adminQuestionnaireCore.createQuestionnaire({ name: `${source.name} Copy`, version: source.version });
      // copy questions
      const qs = await adminQuestionnaireCrud.listQuestionsByQuestionnaire(id);
      for (const q of qs) {
        await adminQuestionnaireCrud.createQuestion({ questionnaireId: newId, text: q.text, type: q.type, weight: q.weight, segmentId: q.segmentId, optionsJson: undefined });
      }
      setToast({ open: true, message: 'Questionnaire duplicated', severity: 'success' });
      await load();
    } catch (e) {
      setToast({ open: true, message: 'Failed to duplicate questionnaire', severity: 'error' });
    }
  };

  const filtered = items.filter(q => `${q.name} ${q.version || ''}`.toLowerCase().includes(query.trim().toLowerCase()));

  return (
    <Box p={3}>
      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'stretch', sm: 'center' }} mb={2} gap={1}>
        <Typography variant="h5">Questionnaires</Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} gap={1} alignItems={{ xs: 'stretch', sm: 'center' }}>
          <TextField size="small" placeholder="Search..." value={query} onChange={(e)=>setQuery(e.target.value)} sx={{ minWidth: 240 }} />
          <Button variant="contained" onClick={() => setOpen(true)}>New Questionnaire</Button>
        </Stack>
      </Stack>

      {loading ? (
        <Typography>Loading...</Typography>
      ) : (
        <Grid container spacing={2}>
          {filtered.map(q => (
            <Grid key={q.id} item xs={12} sm={6} md={4} lg={3}>
              <Card variant="outlined" sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ pb: 1 }}>
                  <Typography fontWeight={700} gutterBottom>{q.name}</Typography>
                  <Typography variant="body2" color="text.secondary">Version: {q.version || '-'}</Typography>
                </CardContent>
                <CardActions sx={{ mt: 'auto', justifyContent: 'space-between' }}>
                  <Button size="small" component={Link} to={`/admin/questionnaires/${q.id}/questions`} endIcon={<ArrowForwardIcon fontSize="small" />}>Manage</Button>
                  <Box>
                    <Tooltip title="Duplicate"><IconButton size="small" onClick={() => duplicate(q.id)}><ContentCopyIcon fontSize="small" /></IconButton></Tooltip>
                    <Tooltip title="Delete"><IconButton size="small" color="error" onClick={() => remove(q.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
                  </Box>
                </CardActions>
              </Card>
            </Grid>
          ))}
          {filtered.length === 0 && (
            <Grid item xs={12}><Typography color="text.secondary">No questionnaires found.</Typography></Grid>
          )}
        </Grid>
      )}

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Create Questionnaire</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField label="Name" value={name} onChange={e=>setName(e.target.value)} fullWidth />
            <TextField label="Version" value={version} onChange={e=>setVersion(e.target.value)} fullWidth />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={()=>setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={create}>Create</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={toast.open} autoHideDuration={3000} onClose={() => setToast(t => ({ ...t, open: false }))}>
        <Alert onClose={() => setToast(t => ({ ...t, open: false }))} severity={toast.severity} sx={{ width: '100%' }}>
          {toast.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}

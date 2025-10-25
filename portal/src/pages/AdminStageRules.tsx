import { useEffect, useMemo, useState } from 'react';
import { Container, Paper, Typography, Stack, Button, TextField, Grid, IconButton, Tooltip, MenuItem } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useToast } from '../components/ToastProvider';
import api, { StageRule, listStageRules, createStageRule, updateStageRule, deleteStageRule } from '../api/adminStageRules';
import { getModuleTree, flattenStages } from '../api/modules';

const emptyForm = { questionnaireId: '', minScore: '', maxScore: '', priority: '', targetStageId: '' } as any;

const AdminStageRules = () => {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(true);
  const [rules, setRules] = useState<StageRule[]>([]);
  const [form, setForm] = useState<any>({ ...emptyForm });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [stages, setStages] = useState<Array<{ id: string; name: string; moduleId?: string }>>([]);
  const [modules, setModules] = useState<Array<{ id: string; name: string }>>([]);
  const [moduleFilter, setModuleFilter] = useState<string>('');

  const load = async () => {
    setLoading(true);
    try {
      const data = await listStageRules();
      setRules(data);
    } catch (e) {
      showToast('Failed to load stage rules', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const tree = await getModuleTree();
        const flatStages = flattenStages(tree);
        if (active) {
          // Attempt to tag stages with moduleId when possible via traversal
          // flattenStages returns only id/name, so also create modules list
          const mods = tree.map(m => ({ id: m.id, name: m.name }));
          setModules(mods);
          setStages(flatStages);
        }
      } catch {
        if (active) setStages([]);
      }
    })();
    return () => { active = false; };
  }, []);

  const onSubmit = async () => {
    try {
      // Basic validation
      const min = Number(form.minScore);
      const max = Number(form.maxScore);
      const pri = Number(form.priority);
      if (!form.targetStageId) { showToast('Please select a target stage', 'warning'); return; }
      if (Number.isNaN(min) || Number.isNaN(max) || Number.isNaN(pri)) { showToast('Please enter valid numbers', 'warning'); return; }
      if (min > max) { showToast('Min score cannot be greater than Max score', 'warning'); return; }
      if (pri <= 0) { showToast('Priority must be greater than 0', 'warning'); return; }
      if (editingId) {
        await updateStageRule(editingId, {
          questionnaireId: form.questionnaireId || null,
          minScore: Number(form.minScore),
          maxScore: Number(form.maxScore),
          priority: Number(form.priority),
          targetStageId: String(form.targetStageId),
        });
        showToast('Rule updated', 'success');
      } else {
        await createStageRule({
          questionnaireId: form.questionnaireId || null,
          minScore: Number(form.minScore),
          maxScore: Number(form.maxScore),
          priority: Number(form.priority),
          targetStageId: String(form.targetStageId),
        });
        showToast('Rule created', 'success');
      }
      setForm({ ...emptyForm });
      setEditingId(null);
      await load();
    } catch (e) {
      showToast('Save failed', 'error');
    }
  };

  const onEdit = (r: StageRule) => {
    setEditingId(r.id);
    setForm({
      questionnaireId: r.questionnaireId || '',
      minScore: String(r.minScore),
      maxScore: String(r.maxScore),
      priority: String(r.priority),
      targetStageId: String(r.targetStageId),
    });
  };

  const onDelete = async (id: string) => {
    try {
      await deleteStageRule(id);
      showToast('Rule deleted', 'success');
      await load();
    } catch (e) {
      showToast('Delete failed', 'error');
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>Stage Rules</Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>{editingId ? 'Edit Rule' : 'Create Rule'}</Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6} md={3}>
            <TextField label="Questionnaire ID (optional)" value={form.questionnaireId} onChange={(e) => setForm({ ...form, questionnaireId: e.target.value })} fullWidth />
          </Grid>
          <Grid item xs={6} md={3}>
            <TextField label="Min Score" type="number" value={form.minScore} onChange={(e) => setForm({ ...form, minScore: e.target.value })} fullWidth />
          </Grid>
          <Grid item xs={6} md={3}>
            <TextField label="Max Score" type="number" value={form.maxScore} onChange={(e) => setForm({ ...form, maxScore: e.target.value })} fullWidth />
          </Grid>
          <Grid item xs={6} md={2}>
            <TextField label="Priority" type="number" value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })} fullWidth />
          </Grid>
          <Grid item xs={6} md={4}>
            <TextField select SelectProps={{ native: true }} label="Module (optional filter)" value={moduleFilter} onChange={(e) => {
              const v = e.target.value;
              setModuleFilter(v);
              // If current target stage not in filtered set, clear it
              const filtered = stagesForFilter(v);
              if (!filtered.some(s => s.id === form.targetStageId)) {
                setForm({ ...form, targetStageId: '' });
              }
            }} fullWidth>
              <option value="">All modules</option>
              {modules.map(m => (
                <option key={m.id} value={m.id}>{m.name}</option>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={6} md={4}>
            <TextField select SelectProps={{ native: true }} label="Target Stage" value={form.targetStageId} onChange={(e) => setForm({ ...form, targetStageId: e.target.value })} fullWidth>
              <option value="">Select stage</option>
              {stagesForFilter(moduleFilter).map(s => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <Stack direction="row" spacing={2}>
              <Button startIcon={<AddIcon />} variant="contained" onClick={onSubmit}>{editingId ? 'Update' : 'Create'}</Button>
              {editingId && <Button variant="text" onClick={() => { setEditingId(null); setForm({ ...emptyForm }); }}>Cancel</Button>}
            </Stack>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>Existing Rules</Typography>
        {rules.length === 0 ? (
          <Typography variant="body2" color="text.secondary">No rules yet.</Typography>
        ) : (
          <Grid container spacing={1}>
            {rules.map((r) => (
              <Grid key={r.id} item xs={12}>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ xs: 'flex-start', sm: 'center' }} justifyContent="space-between">
                  <Typography variant="body2">#{r.priority} • Score {r.minScore} - {r.maxScore} → Stage {r.targetStageName ?? r.targetStageId} {r.questionnaireId ? `(Q:${r.questionnaireId})` : '(Global)'}</Typography>
                  <Stack direction="row" spacing={1}>
                    <Tooltip title="Edit"><IconButton size="small" onClick={() => onEdit(r)}><EditIcon fontSize="small" /></IconButton></Tooltip>
                    <Tooltip title="Delete"><IconButton size="small" onClick={() => onDelete(r.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
                  </Stack>
                </Stack>
              </Grid>
            ))}
          </Grid>
        )}
      </Paper>
    </Container>
  );
};

export default AdminStageRules;

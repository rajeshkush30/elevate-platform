import { useEffect, useState } from 'react';
import { Container, Paper, Typography, Grid, TextField, Button, Stack, IconButton, Tooltip, List, ListItem, ListItemText, Divider, MenuItem, Checkbox, FormControlLabel } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import { useToast } from '../components/ToastProvider';
import { getModuleTree, createModule, updateModule, deleteModule, createSegment, updateSegment, deleteSegment, createStage, updateStage, deleteStage, ModuleTreeNode, reorderItems, StageType } from '../api/catalog';
import adminAssessmentsApi from '../api/adminAssessments';
import adminClientsApi, { Client } from '../api/adminClients';

const AdminCatalog = () => {
  const { showToast } = useToast();
  const [tree, setTree] = useState<ModuleTreeNode[]>([]);
  const [loading, setLoading] = useState(true);

  // selection for assessments tab
  const [selected, setSelected] = useState<{ moduleId?: string; segmentId?: string; stageId?: string; stageName?: string }>({});
  const [assessments, setAssessments] = useState<Array<{ id: string; name: string; description?: string }>>([]);
  const [assLoading, setAssLoading] = useState(false);
  const [assForm, setAssForm] = useState<{ id?: string; name: string; description?: string; questionnaireId?: string }>({ name: '' });
  const [assignForm, setAssignForm] = useState<{ assessmentId?: string; clientIds: string[]; dueDate?: string }>({ clientIds: [] });
  const [clients, setClients] = useState<Client[]>([]);

  // simple forms
  const [moduleForm, setModuleForm] = useState({ id: '', name: '' });
  const [segmentForm, setSegmentForm] = useState({ id: '', name: '', moduleId: '' });
  const [stageForm, setStageForm] = useState({ id: '', name: '', moduleId: '', segmentId: '', type: '' as '' | StageType });

  const load = async () => {
    setLoading(true);
    try {
      const data = await getModuleTree();
      setTree(data);
      // also prefetch clients for assignment picker
      const cs = await adminClientsApi.listClients();
      setClients(cs);
    } catch (e) {
      showToast('Failed to load catalog', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadAssessments = async (stageId?: string) => {
    if (!stageId) { setAssessments([]); return; }
    setAssLoading(true);
    try {
      const list = await adminAssessmentsApi.listAssessmentsByStage(stageId);
      setAssessments(list);
    } catch {
      showToast('Failed to load assessments', 'error');
    } finally {
      setAssLoading(false);
    }
  };

  // Reorder helpers
  const moveModule = async (index: number, dir: -1 | 1) => {
    const newIndex = index + dir;
    if (newIndex < 0 || newIndex >= tree.length) return;
    const copy = [...tree];
    const [item] = copy.splice(index, 1);
    copy.splice(newIndex, 0, item);
    setTree(copy);
    try {
      await reorderItems(copy.map((m, i) => ({ type: 'modules', id: m.id, orderIndex: i + 1 })));
      showToast('Modules reordered', 'success');
    } catch { showToast('Reorder failed', 'error'); }
  };

  const moveSegmentsWithinModule = async (moduleId: string, index: number, dir: -1 | 1) => {
    const mod = tree.find(m => m.id === moduleId);
    if (!mod) return;
    const arr = mod.segments || [];
    const newIndex = index + dir;
    if (newIndex < 0 || newIndex >= arr.length) return;
    const copyItems = [...arr];
    const [item] = copyItems.splice(index, 1);
    copyItems.splice(newIndex, 0, item);
    const newTree = tree.map(m => m.id === moduleId ? { ...m, segments: copyItems } as ModuleTreeNode : m);
    setTree(newTree);
    try {
      await reorderItems(copyItems.map((s, i) => ({ type: 'segments', id: s.id, orderIndex: i + 1 })));
      showToast('Segments reordered', 'success');
    } catch { showToast('Reorder failed', 'error'); }
  };

  const moveStagesWithinSegment = async (moduleId: string, segmentId: string, index: number, dir: -1 | 1) => {
    const seg = (tree.find(m => m.id === moduleId)?.segments || []).find(s => s.id === segmentId);
    if (!seg) return;
    const arr = seg.stages || [];
    const newIndex = index + dir;
    if (newIndex < 0 || newIndex >= arr.length) return;
    const copyItems = [...arr];
    const [item] = copyItems.splice(index, 1);
    copyItems.splice(newIndex, 0, item);
    const newTree = tree.map(m => m.id !== moduleId ? m : ({
      ...m,
      segments: (m.segments || []).map(s => s.id === segmentId ? { ...s, stages: copyItems } : s)
    } as ModuleTreeNode));
    setTree(newTree);
    try {
      await reorderItems(copyItems.map((t, i) => ({ type: 'stages', id: t.id, orderIndex: i + 1 })));
      showToast('Stages reordered', 'success');
    } catch { showToast('Reorder failed', 'error'); }
  };

  useEffect(() => { 
    console.log('Component mounted, loading modules...');
    load().catch(error => {
      console.error('Failed to load modules:', error);
      showToast('Failed to load catalog', 'error');
    });
  }, []);

  // Ensure consistent ID types by converting to string
  const moduleOptions = tree.map(m => ({ 
    id: String(m.id),  // Ensure ID is string for consistency
    name: m.name 
  }));
  
  const segmentOptionsFor = (moduleId: string | undefined) => {
    if (!moduleId) return [];
    return (tree.find(m => String(m.id) === String(moduleId))?.segments ?? [])
      .map(s => ({
        id: String(s.id),
        name: s.name
      }));
  };
      
  // Debug logging
  console.log('Module Tree:', tree);
  console.log('Module Options:', moduleOptions);

  const submitModule = async () => {
    try {
      if (!moduleForm.name) { 
        showToast('Module name required', 'warning'); 
        return; 
      }
      
      console.log('Submitting module:', moduleForm);
      
      if (moduleForm.id) {
        await updateModule(moduleForm.id, { name: moduleForm.name });
        showToast('Module updated', 'success');
      } else {
        await createModule({ name: moduleForm.name });
        showToast('Module created', 'success');
      }
      
      // Reset form and reload data
      setModuleForm({ id: '', name: '' });
      await load();
    } catch (error) {
      console.error('Error saving module:', error);
      showToast('Save failed: ' + (error as Error).message, 'error');
    }
  };

  const submitSegment = async () => {
    try {
      // Validate segment name
      if (!segmentForm.name?.trim()) {
        showToast('Segment name is required', 'warning');
        return;
      }
      
      // Validate module selection
      if (!segmentForm.moduleId) {
        showToast('Please select a module first', 'warning');
        return;
      }
      if (segmentForm.id) {
        await updateSegment(segmentForm.id, { name: segmentForm.name, moduleId: segmentForm.moduleId || undefined });
        showToast('Segment updated', 'success');
      } else {
        await createSegment({ name: segmentForm.name, moduleId: segmentForm.moduleId });
        showToast('Segment created', 'success');
      }
      setSegmentForm({ id: '', name: '', moduleId: '' });
      await load();
    } catch { showToast('Save failed', 'error'); }
  };

  const submitStage = async () => {
    try {
      if (!stageForm.name) { showToast('Stage name required', 'warning'); return; }
      if (!stageForm.segmentId) { showToast('Select a Segment for this Stage', 'warning'); return; }
      if (!stageForm.id && !stageForm.type) { showToast('Select Stage Type', 'warning'); return; }
      if (stageForm.id) {
        await updateStage(stageForm.id, { name: stageForm.name, segmentId: stageForm.segmentId || undefined, type: stageForm.type || undefined });
        showToast('Stage updated', 'success');
      } else {
        await createStage({ name: stageForm.name, segmentId: stageForm.segmentId, type: stageForm.type as StageType });
        showToast('Stage created', 'success');
      }
      setStageForm({ id: '', name: '', moduleId: '', segmentId: '', type: '' });
      await load();
    } catch { showToast('Save failed', 'error'); }
  };

  const onDelete = async (type: 'module'|'segment'|'stage', id: string) => {
    try {
      if (type === 'module') await deleteModule(id);
      if (type === 'segment') await deleteSegment(id);
      if (type === 'stage') await deleteStage(id);
      showToast('Deleted', 'success');
      await load();
    } catch { showToast('Delete failed', 'error'); }
  };

  // When clicking on a Stage name, select it and load assessments
  const onSelectStage = (moduleId: string, segmentId: string, stageId: string, stageName: string) => {
    const sel = { moduleId: String(moduleId), segmentId: String(segmentId), stageId: String(stageId), stageName };
    setSelected(sel);
    setAssignForm({ assessmentId: undefined, clientIds: [], dueDate: undefined });
    setAssForm({ id: undefined, name: '' });
    loadAssessments(sel.stageId);
  };

  // Prepare Structure content without nested ternary
  let structureContent: JSX.Element;
  if (loading) {
    structureContent = (
      <Typography color="text.secondary">Loading...</Typography>
    );
  } else if (tree.length === 0) {
    structureContent = (
      <Typography color="text.secondary">No modules yet.</Typography>
    );
  } else {
    structureContent = (
      <List dense>
        {tree.map((m, idx) => (
          <div key={m.id}>
            <ListItem secondaryAction={
              <Stack direction="row" spacing={1}>
                <Tooltip title="Move up"><IconButton size="small" onClick={() => moveModule(idx, -1)}><ArrowUpwardIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Move down"><IconButton size="small" onClick={() => moveModule(idx, 1)}><ArrowDownwardIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Edit"><IconButton size="small" onClick={() => setModuleForm({ id: m.id, name: m.name })}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Delete"><IconButton size="small" onClick={() => onDelete('module', m.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Stack>
            }>
              <ListItemText primary={m.name} secondary={`Module #${m.id}`} />
            </ListItem>
            <List sx={{ pl: 3 }} dense>
              {(m.segments || []).map((s, sIdx) => (
                <div key={s.id}>
                  <ListItem secondaryAction={
                    <Stack direction="row" spacing={1}>
                      <Tooltip title="Up"><IconButton size="small" onClick={() => moveSegmentsWithinModule(m.id, sIdx, -1)}><ArrowUpwardIcon fontSize="small" /></IconButton></Tooltip>
                      <Tooltip title="Down"><IconButton size="small" onClick={() => moveSegmentsWithinModule(m.id, sIdx, 1)}><ArrowDownwardIcon fontSize="small" /></IconButton></Tooltip>
                      <Tooltip title="Edit"><IconButton size="small" onClick={() => setSegmentForm({ id: s.id, name: s.name, moduleId: m.id })}><EditIcon fontSize="small" /></IconButton></Tooltip>
                      <Tooltip title="Delete"><IconButton size="small" onClick={() => onDelete('segment', s.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
                    </Stack>
                  }>
                    <ListItemText primary={s.name} secondary={`Segment #${s.id}`} />
                  </ListItem>
                  <List sx={{ pl: 3 }} dense>
                    {(s.stages || []).map((st, tIdx) => (
                      <ListItem key={st.id} secondaryAction={
                        <Stack direction="row" spacing={1}>
                          <Tooltip title="Up"><IconButton size="small" onClick={() => moveStagesWithinSegment(m.id, s.id, tIdx, -1)}><ArrowUpwardIcon fontSize="small" /></IconButton></Tooltip>
                          <Tooltip title="Down"><IconButton size="small" onClick={() => moveStagesWithinSegment(m.id, s.id, tIdx, 1)}><ArrowDownwardIcon fontSize="small" /></IconButton></Tooltip>
                          <Tooltip title="Edit"><IconButton size="small" onClick={() => setStageForm({ id: st.id, name: st.name, moduleId: m.id, segmentId: s.id, type: '' })}><EditIcon fontSize="small" /></IconButton></Tooltip>
                          <Tooltip title="Delete"><IconButton size="small" onClick={() => onDelete('stage', st.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
                        </Stack>
                      }>
                        <ListItemText primary={st.name} secondary={`Stage #${st.id}`} onClick={() => onSelectStage(m.id, s.id, st.id, st.name)} sx={{ cursor: 'pointer' }} />
                      </ListItem>
                    ))}
                  </List>
                </div>
              ))}
            </List>
            <Divider sx={{ my: 1 }} />
          </div>
        ))}
      </List>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5">Catalog</Typography>
        <Tooltip title="Refresh"><IconButton onClick={load}><RefreshIcon /></IconButton></Tooltip>
      </Stack>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Structure</Typography>
            {structureContent}
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, mb: 2 }}>
            <Typography variant="subtitle1" gutterBottom>{moduleForm.id ? 'Edit Module' : 'Create Module'}</Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField label="Name" value={moduleForm.name} onChange={(e) => setModuleForm({ ...moduleForm, name: e.target.value })} fullWidth />
              <Button startIcon={<AddIcon />} variant="contained" onClick={submitModule}>{moduleForm.id ? 'Update' : 'Create'}</Button>
              {moduleForm.id && <Button variant="text" onClick={() => setModuleForm({ id: '', name: '' })}>Cancel</Button>}
            </Stack>
          </Paper>
          <Paper sx={{ p: 2, mb: 2 }}>
            <Typography variant="subtitle1" gutterBottom>{segmentForm.id ? 'Edit Segment' : 'Create Segment'}</Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField label="Name" value={segmentForm.name} onChange={(e) => setSegmentForm({ ...segmentForm, name: e.target.value })} fullWidth />
              <TextField
                select
                label="Module"
                value={segmentForm.moduleId || ''}
                onChange={(e) => setSegmentForm({...segmentForm, moduleId: e.target.value})}
                fullWidth
                margin="normal"
                variant="outlined"
                required
                error={!segmentForm.moduleId}
                helperText={!segmentForm.moduleId ? 'Please select a module' : ''}
              >
                <MenuItem value="">Select module</MenuItem>
                {moduleOptions.map(m => <MenuItem key={m.id} value={m.id}>{m.name}</MenuItem>)}
              </TextField>
              {/* Reorder for segments is available in the Structure list above */}
              <Button startIcon={<AddIcon />} variant="contained" onClick={submitSegment} disabled={!segmentForm.moduleId}>
                {segmentForm.id ? 'Update' : 'Create'}
              </Button>
              {segmentForm.id && (
                <Button variant="text" onClick={() => setSegmentForm({ id: '', name: '', moduleId: '' })}>
                  Cancel
                </Button>
              )}
            </Stack>
          </Paper>

          <Paper sx={{ p: 2, mt: 2 }}>
            <Typography variant="subtitle1" gutterBottom>{stageForm.id ? 'Edit Stage' : 'Create Stage'}</Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField label="Name" value={stageForm.name} onChange={(e) => setStageForm({ ...stageForm, name: e.target.value })} fullWidth />
              <TextField select label="Module" value={stageForm.moduleId} onChange={(e) => { setStageForm({ ...stageForm, moduleId: e.target.value, segmentId: '' }); }} sx={{ minWidth: 220 }}>
                <MenuItem value="">Select module</MenuItem>
                {moduleOptions.map(m => <MenuItem key={m.id} value={m.id}>{m.name}</MenuItem>)}
              </TextField>
              <TextField select label="Segment" value={stageForm.segmentId} onChange={(e) => setStageForm({ ...stageForm, segmentId: e.target.value })} sx={{ minWidth: 220 }}>
                <MenuItem value="">Select segment</MenuItem>
                {segmentOptionsFor(stageForm.moduleId).map(s => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
              </TextField>
              <TextField select label="Type" value={stageForm.type} onChange={(e) => setStageForm({ ...stageForm, type: e.target.value as StageType })} sx={{ minWidth: 200 }}>
                <MenuItem value="">Select type</MenuItem>
                <MenuItem value="TRAINING">TRAINING</MenuItem>
                <MenuItem value="ASSESSMENT">ASSESSMENT</MenuItem>
                <MenuItem value="CONSULTATION">CONSULTATION</MenuItem>
                <MenuItem value="SUMMARY">SUMMARY</MenuItem>
              </TextField>
              <Button startIcon={<AddIcon />} variant="contained" onClick={submitStage}>{stageForm.id ? 'Update' : 'Create'}</Button>
              {stageForm.id && <Button variant="text" onClick={() => setStageForm({ id: '', name: '', moduleId: '', segmentId: '', type: '' })}>Cancel</Button>}
            </Stack>
          </Paper>

          {/* Assessments panel */}
          <Paper sx={{ p: 2, mt: 2 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center">
              <Typography variant="subtitle1">Assessments {selected.stageName ? `for: ${selected.stageName}` : ''}</Typography>
              <Button size="small" onClick={() => loadAssessments(selected.stageId)} disabled={!selected.stageId}>Refresh</Button>
            </Stack>
            {!selected.stageId ? (
              <Typography color="text.secondary" sx={{ mt: 1 }}>Select a Stage from the left to manage its assessments.</Typography>
            ) : (
              <Stack spacing={2} sx={{ mt: 2 }}>
                <Stack spacing={1}>
                  <Typography variant="body2" color="text.secondary">{assForm.id ? 'Edit Assessment' : 'Create Assessment'}</Typography>
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                    <TextField label="Name" value={assForm.name} onChange={(e) => setAssForm({ ...assForm, name: e.target.value })} fullWidth />
                    <TextField label="Description" value={assForm.description || ''} onChange={(e) => setAssForm({ ...assForm, description: e.target.value })} fullWidth />
                  </Stack>
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                    <TextField label="Questionnaire ID (optional)" value={assForm.questionnaireId || ''} onChange={(e) => setAssForm({ ...assForm, questionnaireId: e.target.value })} sx={{ minWidth: 220 }} />
                    {!assForm.id ? (
                      <Button variant="contained" onClick={async () => {
                        if (!assForm.name) { showToast('Name required', 'warning'); return; }
                        try {
                          await adminAssessmentsApi.createAssessment({ stageId: selected.stageId!, name: assForm.name, description: assForm.description, questionnaireId: assForm.questionnaireId });
                          showToast('Assessment created', 'success');
                          setAssForm({ id: undefined, name: '' });
                          await loadAssessments(selected.stageId);
                        } catch (e) { showToast('Create failed', 'error'); }
                      }}>Create</Button>
                    ) : (
                      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                        <Button variant="contained" onClick={async () => {
                          try {
                            await adminAssessmentsApi.updateAssessment(assForm.id!, { name: assForm.name, description: assForm.description, questionnaireId: assForm.questionnaireId });
                            showToast('Assessment updated', 'success');
                            setAssForm({ id: undefined, name: '' });
                            await loadAssessments(selected.stageId);
                          } catch { showToast('Update failed', 'error'); }
                        }}>Save</Button>
                        <Button variant="text" onClick={() => setAssForm({ id: undefined, name: '' })}>Cancel</Button>
                      </Stack>
                    )}
                  </Stack>
                </Stack>

                <Divider />

                <Stack spacing={1}>
                  <Typography variant="body2" color="text.secondary">Existing</Typography>
                  {assLoading ? (
                    <Typography color="text.secondary">Loading...</Typography>
                  ) : assessments.length === 0 ? (
                    <Typography color="text.secondary">No assessments.</Typography>
                  ) : (
                    <List dense>
                      {assessments.map(a => (
                        <ListItem key={a.id}
                          secondaryAction={
                            <Stack direction="row" spacing={1}>
                              <Button size="small" onClick={() => setAssForm({ id: a.id, name: a.name, description: a.description })}>Edit</Button>
                              <Button size="small" color="error" onClick={async () => { try { await adminAssessmentsApi.deleteAssessment(a.id); showToast('Deleted', 'success'); await loadAssessments(selected.stageId); } catch { showToast('Delete failed', 'error'); } }}>Delete</Button>
                            </Stack>
                          }
                          onClick={() => setAssignForm(f => ({ ...f, assessmentId: a.id }))}
                          sx={{ cursor: 'pointer' }}
                        >
                          <ListItemText primary={a.name} secondary={a.description} />
                        </ListItem>
                      ))}
                    </List>
                  )}
                </Stack>

                <Divider />

                <Stack spacing={1}>
                  <Typography variant="body2" color="text.secondary">Assign to Clients</Typography>
                  {!assignForm.assessmentId && <Typography color="text.secondary">Select an assessment from the list above.</Typography>}
                  {assignForm.assessmentId && (
                    <>
                      <TextField type="date" label="Due date" InputLabelProps={{ shrink: true }} value={assignForm.dueDate || ''} onChange={(e) => setAssignForm({ ...assignForm, dueDate: e.target.value })} sx={{ maxWidth: 240 }} />
                      <List dense sx={{ maxHeight: 200, overflow: 'auto', border: '1px solid #eee' }}>
                        {clients.map(c => {
                          const checked = assignForm.clientIds.includes(c.id);
                          return (
                            <ListItem key={c.id} sx={{ py: 0 }}>
                              <FormControlLabel control={<Checkbox size="small" checked={checked} onChange={(e) => {
                                setAssignForm(prev => ({
                                  ...prev,
                                  clientIds: e.target.checked ? [...prev.clientIds, c.id] : prev.clientIds.filter(id => id !== c.id)
                                }));
                              }} />} label={`${c.firstName ?? ''} ${c.lastName ?? ''} (${c.email})`} />
                            </ListItem>
                          );
                        })}
                      </List>
                      <Button variant="contained" onClick={async () => {
                        try {
                          await adminAssessmentsApi.assignAssessment(assignForm.assessmentId!, { clientIds: assignForm.clientIds, dueDate: assignForm.dueDate });
                          showToast('Assigned', 'success');
                          setAssignForm({ assessmentId: assignForm.assessmentId, clientIds: [], dueDate: assignForm.dueDate });
                        } catch { showToast('Assign failed', 'error'); }
                      }} disabled={assignForm.clientIds.length === 0}>Assign</Button>
                    </>
                  )}
                </Stack>
              </Stack>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default AdminCatalog;

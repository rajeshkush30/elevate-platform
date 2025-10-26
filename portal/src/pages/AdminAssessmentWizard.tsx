import { useEffect, useMemo, useState } from 'react';
import { Box, Button, Card, CardContent, Checkbox, FormControlLabel, Stack, TextField, Typography, Stepper, Step, StepLabel, Snackbar, Alert } from '@mui/material';
import catalog from '../api/catalog';
import adminQuestionnaireCore from '../api/adminQuestionnaireCore';
import { listClients, Client } from '../api/adminClients';
import { createAssessment, assignAssessment } from '../api/adminAssessments';
import { useNavigate, useLocation } from 'react-router-dom';

export default function AdminAssessmentWizard() {
  const navigate = useNavigate();
  const location = useLocation();
  const [step, setStep] = useState(1);

  // Step 1: pick stage (from module tree)
  const [tree, setTree] = useState<any[]>([]);
  const [stageId, setStageId] = useState<string>('');
  const [stageQuery, setStageQuery] = useState('');

  // Step 2: pick questionnaire
  const [questionnaires, setQuestionnaires] = useState<Array<{ id: string; name: string; version?: string }>>([]);
  const [questionnaireId, setQuestionnaireId] = useState<string>('');
  const [qQuery, setQQuery] = useState('');

  // Step 3: basic details
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  // Step 4: assign to clients
  const [clients, setClients] = useState<Client[]>([]);
  const [selected, setSelected] = useState<Record<string, boolean>>({});
  const [dueDate, setDueDate] = useState<string>('');
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });
  const [clientQuery, setClientQuery] = useState('');
  const [dateError, setDateError] = useState<string | null>(null);

  const load = async () => {
    const [t, qs, cls] = await Promise.all([
      catalog.getModuleTree(),
      adminQuestionnaireCore.listQuestionnaires(),
      listClients(),
    ]);
    setTree(Array.isArray(t) ? t : []);
    setQuestionnaires(qs);
    setClients(cls);
  };

  useEffect(() => { load(); }, []);

  // Prefill stage/questionnaire from URL params
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const sid = params.get('stageId');
    const qid = params.get('questionnaireId');
    if (sid) setStageId(String(Number(sid)));
    if (qid) setQuestionnaireId(String(Number(qid)));
  }, [location.search]);

  // Auto-advance to next step when stageId is preselected
  useEffect(() => {
    if (step === 1 && stageId) setStep(2);
  }, [stageId]);

  const stages: Array<{ id: string; name: string; segment?: string; module?: string }> = useMemo(() => {
    const arr: Array<{ id: string; name: string; segment?: string; module?: string }> = [];
    (tree || []).forEach((m: any) => {
      (m.segments || []).forEach((s: any) => {
        (s.stages || []).forEach((st: any) => arr.push({ id: String(st.id), name: String(st.name ?? ''), segment: String(s.name ?? ''), module: String(m.name ?? '') }));
      });
    });
    const q = stageQuery.trim().toLowerCase();
    if (!q) return arr;
    return arr.filter(x => `${x.name} ${x.segment} ${x.module}`.toLowerCase().includes(q));
  }, [tree, stageQuery]);

  const canNext1 = !!stageId;
  const canNext2 = !!questionnaireId;
  const canNext3 = !!name.trim();
  const selectedClientIds = useMemo(() => Object.entries(selected).filter(([, v]) => v).map(([id]) => id), [selected]);
  const canFinish = selectedClientIds.length > 0;

  const next = () => setStep(s => s + 1);
  const back = () => setStep(s => s - 1);

  const finish = async () => {
    // validate date
    if (!dueDate) { setDateError('Due date is required'); setStep(4); return; }
    const today = new Date(); today.setHours(0,0,0,0);
    const d = new Date(dueDate);
    if (isNaN(d.getTime()) || d < today) { setDateError('Due date cannot be in the past'); setStep(4); return; }
    // 1) create assessment with questionnaireId
    try {
      const created = await createAssessment({ stageId, name: name.trim(), description: description.trim() || undefined, questionnaireId });
      // 2) assign to selected clients with due date
      await assignAssessment(created.id, { clientIds: selectedClientIds, dueDate: dueDate || undefined });
      setToast({ open: true, message: 'Assessment created and assigned successfully', severity: 'success' });
      setTimeout(() => navigate('/admin'), 600);
    } catch (e: any) {
      setToast({ open: true, message: 'Failed to create/assign assessment', severity: 'error' });
    }
  };

  return (
    <Box p={3}>
      <Typography variant="h5" gutterBottom>Assessment Wizard</Typography>
      <Stepper activeStep={step - 1} alternativeLabel sx={{ mb: 3 }}>
        {['Stage', 'Questionnaire', 'Details', 'Assign'].map((label) => (
          <Step key={label}><StepLabel>{label}</StepLabel></Step>
        ))}
      </Stepper>

      {step === 1 && (
        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>Select Stage</Typography>
          <TextField size="small" placeholder="Search stage..." value={stageQuery} onChange={(e)=>setStageQuery(e.target.value)} sx={{ mb: 2, maxWidth: 420 }} />
          <Stack spacing={1}>
            {stages.map(st => (
              <Card key={st.id} variant="outlined" onClick={() => setStageId(st.id)} sx={{ cursor: 'pointer', borderColor: stageId === st.id ? 'primary.main' : 'divider', boxShadow: stageId === st.id ? 3 : 0 }}>
                <CardContent>
                  <Typography fontWeight={600}>{st.name}</Typography>
                  <Typography variant="body2" color="text.secondary">{st.module} • {st.segment}</Typography>
                </CardContent>
              </Card>
            ))}
            {stages.length === 0 && <Typography color="text.secondary">No stages found. Create module/segment/stage first.</Typography>}
          </Stack>
          <Stack direction="row" spacing={1} mt={2}>
            <Button variant="contained" disabled={!canNext1} onClick={next}>Next</Button>
          </Stack>
        </Box>
      )}

      {step === 2 && (
        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>Select Questionnaire</Typography>
          <TextField size="small" placeholder="Search questionnaire..." value={qQuery} onChange={(e)=>setQQuery(e.target.value)} sx={{ mb: 2, maxWidth: 420 }} />
          <Stack spacing={1}>
            {questionnaires.filter(q => `${q.name} ${q.version || ''}`.toLowerCase().includes(qQuery.trim().toLowerCase())).map(q => (
              <Card key={q.id} variant="outlined" onClick={() => setQuestionnaireId(q.id)} sx={{ cursor: 'pointer', borderColor: questionnaireId === q.id ? 'primary.main' : 'divider', boxShadow: questionnaireId === q.id ? 3 : 0 }}>
                <CardContent>
                  <Typography fontWeight={600}>{q.name}</Typography>
                  <Typography variant="body2" color="text.secondary">Version: {q.version || '-'}</Typography>
                </CardContent>
              </Card>
            ))}
            {questionnaires.length === 0 && <Typography color="text.secondary">No questionnaires found. Create one first.</Typography>}
          </Stack>
          <Stack direction="row" spacing={1} mt={2}>
            <Button onClick={back}>Back</Button>
            <Button variant="contained" disabled={!canNext2} onClick={next}>Next</Button>
          </Stack>
        </Box>
      )}

      {step === 3 && (
        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>Assessment Details</Typography>
          <Stack spacing={2} maxWidth={520}>
            <TextField label="Name" value={name} onChange={e=>setName(e.target.value)} />
            <TextField label="Description" value={description} onChange={e=>setDescription(e.target.value)} multiline minRows={2} />
          </Stack>
          <Stack direction="row" spacing={1} mt={2}>
            <Button onClick={back}>Back</Button>
            <Button variant="contained" disabled={!canNext3} onClick={next}>Next</Button>
          </Stack>
        </Box>
      )}

      {step === 4 && (
        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>Assign to Clients</Typography>
          <Stack spacing={1}>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ xs: 'stretch', sm: 'center' }}>
              <TextField size="small" placeholder="Search clients..." value={clientQuery} onChange={(e)=>setClientQuery(e.target.value)} sx={{ maxWidth: 420 }} />
              <Typography variant="body2" color="text.secondary">Selected: {selectedClientIds.length} / {clients.length}</Typography>
              <Button size="small" onClick={() => {
                if (selectedClientIds.length === clients.length) {
                  setSelected({});
                } else {
                  const all: Record<string, boolean> = {};
                  clients.forEach(c => { all[c.id] = true; });
                  setSelected(all);
                }
              }}>{selectedClientIds.length === clients.length ? 'Clear all' : 'Select all'}</Button>
            </Stack>
            {clients.map(c => (
              (!clientQuery.trim() || `${c.firstName || ''} ${c.lastName || ''} ${c.email}`.toLowerCase().includes(clientQuery.trim().toLowerCase())) && (
              <FormControlLabel key={c.id}
                control={<Checkbox checked={!!selected[c.id]} onChange={(e)=> setSelected(prev => ({ ...prev, [c.id]: e.target.checked }))} />}
                label={`${c.firstName || ''} ${c.lastName || ''} <${c.email}>`}
              />)
            ))}
            {clients.length === 0 && <Typography color="text.secondary">No clients found. Create clients first.</Typography>}
          </Stack>
          <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ xs: 'stretch', sm: 'center' }} spacing={2} mt={2}>
            <TextField
              label="Due date"
              type="date"
              value={dueDate}
              onChange={(e)=>setDueDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
              error={!!dateError}
              helperText={dateError || 'Select a due date for the assignment'}
            />
          </Stack>
          <Stack direction="row" spacing={1} mt={2}>
            <Button onClick={back}>Back</Button>
            <Button variant="contained" disabled={!canFinish} onClick={finish}>Finish</Button>
          </Stack>
        </Box>
      )}
      <Snackbar open={toast.open} autoHideDuration={3000} onClose={() => setToast(t => ({ ...t, open: false }))}>
        <Alert onClose={() => setToast(t => ({ ...t, open: false }))} severity={toast.severity} sx={{ width: '100%' }}>
          {toast.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}

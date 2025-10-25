import { useEffect, useMemo, useState } from 'react';
import { Container, Paper, Typography, Grid, TextField, Button, Stack, Autocomplete, CircularProgress } from '@mui/material';
import { useToast } from '../components/ToastProvider';
import { createAssignment } from '../api/adminAssignments';
import modulesApi, { flattenModules, getModuleTree } from '../api/modules';
import { searchClients, Client, Page } from '../api/adminClients';

const AdminAssignmentsCreate = () => {
  const { showToast } = useToast();
  const [form, setForm] = useState({ userId: '', moduleId: '', dueAt: '' });
  const [submitting, setSubmitting] = useState(false);
  const [userQuery, setUserQuery] = useState('');
  const [userOptions, setUserOptions] = useState<Client[]>([]);
  const [userLoading, setUserLoading] = useState(false);

  const [modules, setModules] = useState<Array<{ id: string; name: string }>>([]);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const tree = await getModuleTree();
        const flat = flattenModules(tree);
        if (active) setModules(flat);
      } catch {
        if (active) setModules([]);
      }
    })();
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    const fetch = async () => {
      try {
        setUserLoading(true);
        const res: Page<Client> = await searchClients({ query: userQuery, page: 0, size: 10 });
        if (active) setUserOptions(res.content);
      } catch {
        if (active) setUserOptions([]);
      } finally {
        if (active) setUserLoading(false);
      }
    };
    // debounce
    const t = setTimeout(fetch, 300);
    return () => { active = false; clearTimeout(t); };
  }, [userQuery]);

  const onSubmit = async () => {
    try {
      setSubmitting(true);
      if (!form.userId || !form.moduleId) {
        showToast('User ID and Module ID are required', 'warning');
        return;
      }
      await createAssignment({ userId: form.userId, moduleId: form.moduleId, dueAt: form.dueAt || undefined });
      showToast('Assignment created', 'success');
      setForm({ userId: '', moduleId: '', dueAt: '' });
    } catch (e: any) {
      showToast('Failed to create assignment', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h5" gutterBottom>Create Training Assignment</Typography>
      <Paper sx={{ p: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={5}>
            <Autocomplete
              loading={userLoading}
              options={userOptions}
              getOptionLabel={(o) => `${o.firstName ?? ''} ${o.lastName ?? ''} (${o.email})`.trim()}
              onInputChange={(_, v) => setUserQuery(v)}
              onChange={(_, v) => setForm({ ...form, userId: v?.id || '' })}
              renderInput={(params) => (
                <TextField {...params} label="Select User" placeholder="Search by name or email" fullWidth InputProps={{
                  ...params.InputProps,
                  endAdornment: (
                    <>
                      {userLoading ? <CircularProgress color="inherit" size={16} /> : null}
                      {params.InputProps.endAdornment}
                    </>
                  ),
                }} />
              )}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField select SelectProps={{ native: true }} label="Module" value={form.moduleId} onChange={(e) => setForm({ ...form, moduleId: e.target.value })} fullWidth>
              <option value="">Select module</option>
              {modules.map(m => (
                <option key={m.id} value={m.id}>{m.name}</option>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField label="Due At" type="datetime-local" InputLabelProps={{ shrink: true }} value={form.dueAt} onChange={(e) => setForm({ ...form, dueAt: e.target.value })} fullWidth />
          </Grid>
          <Grid item xs={12}>
            <Stack direction="row" spacing={2}>
              <Button variant="contained" onClick={onSubmit} disabled={submitting}>Create</Button>
              <Button variant="text" onClick={() => setForm({ userId: '', moduleId: '', dueAt: '' })}>Clear</Button>
            </Stack>
          </Grid>
        </Grid>
      </Paper>
    </Container>
  );
};

export default AdminAssignmentsCreate;

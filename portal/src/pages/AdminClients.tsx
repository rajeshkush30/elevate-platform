import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Container,
  LinearProgress,
  Pagination,
  Checkbox,
  Tooltip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  Paper,
  Snackbar,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EditIcon from '@mui/icons-material/Edit';
import MarkEmailUnreadIcon from '@mui/icons-material/MarkEmailUnread';
import DownloadIcon from '@mui/icons-material/Download';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import * as api from '../api/adminClients';

const AdminClients = () => {
  const [clients, setClients] = useState<api.Client[]>([]);
  const [q, setQ] = useState('');
  const [page, setPage] = useState(1); // 1-based for UI
  const [size, setSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });
  const [confirm, setConfirm] = useState<{ id?: string; name?: string } | null>(null);
  const [view, setView] = useState<api.Client | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState({ firstName: '', lastName: '', email: '' });
  const [editOpen, setEditOpen] = useState(false);
  const [editForm, setEditForm] = useState<api.Client | null>(null);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [sortField, setSortField] = useState<'createdAt' | 'firstName' | 'email'>('createdAt');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');

  const load = async (params?: { resetPage?: boolean }) => {
    setLoading(true);
    try {
      const p = await api.searchClients({ query: q, page: (params?.resetPage ? 0 : page - 1), size, sort: `${sortField},${sortDir}` });
      setClients(p.content);
      setTotal(p.totalElements);
      if (params?.resetPage) setPage(1);
      setSelected(new Set());
    } catch (e: any) {
      setToast({ open: true, message: `Failed to load clients: ${e?.response?.status || ''}`.trim(), severity: 'error' });
      setClients([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  const openEdit = async (id: string) => {
    const data = await api.getClient(id);
    setEditForm(data);
    setEditOpen(true);
  };

  const saveEdit = async () => {
    if (!editForm) return;
    try {
      const updated = await api.updateClient(editForm.id, {
        id: editForm.id,
        firstName: (editForm.firstName || '').trim() || undefined,
        lastName: (editForm.lastName || '').trim() || undefined,
        email: (editForm.email || '').trim(),
        role: (editForm.role || '').trim() || undefined,
        isActive: !!editForm.isActive,
        isEmailVerified: !!editForm.isEmailVerified,
      });
      setEditOpen(false);
      setEditForm(null);
      // Update local list
      setClients(prev => prev.map(x => x.id === (updated.id) ? { ...x, ...updated } : x));
      setToast({ open: true, message: 'Client updated', severity: 'success' });
    } catch (e: any) {
      setToast({ open: true, message: 'Failed to update client', severity: 'error' });
    }
  };

  useEffect(() => { load(); }, [page, size, sortField, sortDir]);

  // Debounce search
  useEffect(() => {
    const t = setTimeout(() => {
      load({ resetPage: true });
    }, 300);
    return () => clearTimeout(t);
  }, [q]);
  const filtered = clients; // server-side filtered

  const toggleActive = async (c: api.Client) => {
    try {
      // Some backends require full payload on PUT. Fetch latest, then update.
      const current = await api.getClient(c.id);
      const updated = await api.updateClient(c.id, {
        id: current.id,
        firstName: current.firstName,
        lastName: current.lastName,
        email: current.email,
        role: current.role,
        isActive: !current.isActive,
        isEmailVerified: current.isEmailVerified,
      });
      // Optimistically update local list without refetch
      setClients(prev => prev.map(x => x.id === c.id ? { ...x, ...updated } : x));
      setToast({ open: true, message: `Client ${!c.isActive ? 'activated' : 'deactivated'}`, severity: 'success' });
    } catch (e: any) {
      const status = e?.response?.status;
      const msg = status === 409 ? 'Conflict updating client (possibly email conflict or state). Refresh and try again.' : 'Failed to update status';
      setToast({ open: true, message: msg, severity: 'error' });
    }
  };

  const remove = async (id: string) => {
    try {
      await api.deleteClient(id);
      await load();
      setToast({ open: true, message: 'Client deleted', severity: 'success' });
    } catch (e: any) {
      setToast({ open: true, message: 'Failed to delete client', severity: 'error' });
    }
  };

  const create = async () => {
    if (!createForm.email.trim()) return;
    try {
      const res = await api.createClient({
        firstName: createForm.firstName.trim() || undefined,
        lastName: createForm.lastName.trim() || undefined,
        email: createForm.email.trim(),
      });
      setCreateOpen(false);
      setCreateForm({ firstName: '', lastName: '', email: '' });
      await load({ resetPage: true });
      setToast({ open: true, message: res.temporaryPassword ? `Client invited. Temp password: ${res.temporaryPassword}` : 'Client created', severity: 'success' });
    } catch (e: any) {
      setToast({ open: true, message: 'Failed to create client', severity: 'error' });
    }
  };

  const resendInvite = async (id: string) => {
    try {
      await api.resendInvite(id);
      setToast({ open: true, message: 'Invite email sent', severity: 'success' });
    } catch (e: any) {
      const status = e?.response?.status;
      const msg = status === 409 ? 'Invite already sent recently. Please try again later.' : 'Failed to send invite';
      setToast({ open: true, message: msg, severity: 'error' });
    }
  };

  const toggleAll = (checked: boolean) => {
    if (checked) setSelected(new Set(filtered.map(c => c.id)));
    else setSelected(new Set());
  };

  const toggleOne = (id: string) => {
    setSelected(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const bulkActivate = async (value: boolean) => {
    setLoading(true);
    try {
      const ids = Array.from(selected);
      const updates: Record<string, api.Client> = {};
      for (const id of ids) {
        try {
          const current = await api.getClient(id);
          const updated = await api.updateClient(id, {
            id: current.id,
            firstName: current.firstName,
            lastName: current.lastName,
            email: current.email,
            role: current.role,
            isActive: value,
            isEmailVerified: current.isEmailVerified,
          });
          updates[id] = updated;
        } catch (inner) {
          // Continue others
        }
      }
      // Merge updates locally
      setClients(prev => prev.map(x => updates[x.id] ? { ...x, ...updates[x.id] } : x));
      setToast({ open: true, message: `Selected clients ${value ? 'activated' : 'deactivated'}`, severity: 'success' });
    } finally {
      setLoading(false);
    }
  };

  const bulkDelete = async () => {
    setLoading(true);
    try {
      await Promise.all(Array.from(selected).map(id => api.deleteClient(id)));
      await load();
      setToast({ open: true, message: 'Selected clients deleted', severity: 'success' });
    } finally {
      setLoading(false);
    }
  };

  const exportCsv = () => {
    const rows = filtered.map(c => ({
      id: c.id,
      firstName: c.firstName || '',
      lastName: c.lastName || '',
      email: c.email,
      role: c.role || '',
      isActive: c.isActive ? 'true' : 'false',
      isEmailVerified: c.isEmailVerified ? 'true' : 'false',
      createdAt: c.createdAt || '',
      updatedAt: c.updatedAt || '',
    }));
    const headers = Object.keys(rows[0] || { id: '', firstName: '', lastName: '', email: '', role: '', isActive: '', isEmailVerified: '', createdAt: '', updatedAt: '' });
    const csv = [headers.join(','), ...rows.map(r => headers.map(h => String((r as any)[h]).replace(/"/g, '""')).map(v => /[,"]/.test(v) ? `"${v}"` : v).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `clients_${new Date().toISOString()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const onSortClick = (field: 'createdAt' | 'firstName' | 'email') => {
    if (sortField === field) {
      setSortDir(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDir('asc');
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'stretch', sm: 'center' }} sx={{ mb: 2 }} spacing={2}>
        <Typography variant="h5">Manage Clients</Typography>
        <Stack direction="row" spacing={1}>
          <TextField size="small" placeholder="Search by name or email" value={q} onChange={(e) => setQ(e.target.value)} />
          <Button startIcon={<PersonAddIcon />} variant="contained" onClick={() => setCreateOpen(true)}>Invite/Create</Button>
          <Tooltip title="Export current results as CSV"><span><Button startIcon={<DownloadIcon />} onClick={exportCsv} disabled={filtered.length === 0}>Export CSV</Button></span></Tooltip>
        </Stack>
      </Stack>

      {loading && <LinearProgress sx={{ mb: 1 }} />}
      <Paper sx={{ p: 2 }}>
        {/* Header row with select all and sorting */}
        <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ py: 1, borderBottom: '1px solid #eee' }}>
          <Stack direction="row" alignItems="center" spacing={1} sx={{ minWidth: 280 }}>
            <Checkbox
              checked={selected.size > 0 && selected.size === filtered.length}
              indeterminate={selected.size > 0 && selected.size < filtered.length}
              onChange={(e) => toggleAll(e.target.checked)}
            />
            <Button size="small" onClick={() => onSortClick('firstName')} endIcon={sortField==='firstName' ? (sortDir==='asc'? <ArrowUpwardIcon fontSize="inherit"/>:<ArrowDownwardIcon fontSize="inherit"/>) : undefined}>
              Name
            </Button>
            <Button size="small" onClick={() => onSortClick('email')} endIcon={sortField==='email' ? (sortDir==='asc'? <ArrowUpwardIcon fontSize="inherit"/>:<ArrowDownwardIcon fontSize="inherit"/>) : undefined}>
              Email
            </Button>
          </Stack>
          <Stack direction="row" spacing={1}>
            <Button size="small" disabled={selected.size===0 || loading} onClick={() => bulkActivate(true)}>Activate</Button>
            <Button size="small" disabled={selected.size===0 || loading} onClick={() => bulkActivate(false)}>Deactivate</Button>
            <Button size="small" color="error" disabled={selected.size===0 || loading} onClick={bulkDelete}>Delete</Button>
          </Stack>
        </Stack>
        {filtered.map((c) => (
          <Stack key={c.id} direction="row" alignItems="center" justifyContent="space-between" sx={{ py: 1, borderBottom: '1px solid #eee' }}>
            <Stack direction="row" alignItems="center" spacing={1} sx={{ minWidth: 280 }}>
              <Checkbox checked={selected.has(c.id)} onChange={() => toggleOne(c.id)} />
              <Box>
                <Typography variant="subtitle1">{c.firstName} {c.lastName} <Chip size="small" label={c.role || 'CLIENT'} sx={{ ml: 1 }} /></Typography>
                <Typography variant="body2" color="text.secondary">{c.email}</Typography>
              </Box>
            </Stack>
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="body2">{c.isActive ? 'Active' : 'Inactive'}</Typography>
              <Switch checked={!!c.isActive} onChange={() => toggleActive(c)} disabled={loading} />
              <IconButton onClick={async () => setView(await api.getClient(c.id))} disabled={loading}><VisibilityIcon /></IconButton>
              <IconButton title="Resend invite" onClick={() => resendInvite(c.id)} disabled={loading}><MarkEmailUnreadIcon /></IconButton>
              <IconButton onClick={() => openEdit(c.id)} disabled={loading}><EditIcon /></IconButton>
              <IconButton color="error" onClick={() => setConfirm({ id: c.id, name: (c.firstName + ' ' + c.lastName).trim() || c.email })} disabled={loading}><DeleteIcon /></IconButton>
            </Stack>
          </Stack>
        ))}
        {filtered.length === 0 && (
          <Alert severity="info">No clients found.</Alert>
        )}
      </Paper>

      <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ xs: 'stretch', sm: 'center' }} justifyContent="space-between" sx={{ mt: 2 }} spacing={1}>
        <Typography variant="body2">Total: {total}</Typography>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            label="Page size"
            type="number"
            size="small"
            sx={{ width: 120 }}
            value={size}
            onChange={(e) => setSize(Math.max(1, Number(e.target.value) || 10))}
          />
          <Pagination
            count={Math.max(1, Math.ceil(total / size))}
            page={page}
            onChange={(_, p) => setPage(p)}
            color="primary"
          />
        </Stack>
      </Stack>

      {/* View dialog */}
      <Dialog open={!!view} onClose={() => setView(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Client Details</DialogTitle>
        <DialogContent>
          {view && (
            <Stack spacing={1} sx={{ mt: 1 }}>
              <Typography><b>Name:</b> {view.firstName} {view.lastName}</Typography>
              <Typography><b>Email:</b> {view.email}</Typography>
              <Typography><b>Role:</b> {view.role}</Typography>
              <Typography><b>Active:</b> {String(view.isActive)}</Typography>
              <Typography><b>Verified:</b> {String(view.isEmailVerified)}</Typography>
              <Typography><b>Created:</b> {view.createdAt || '-'}</Typography>
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setView(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Edit dialog */}
      <Dialog open={editOpen} onClose={() => setEditOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Client</DialogTitle>
        <DialogContent>
          {editForm && (
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField label="First name" value={editForm.firstName || ''} onChange={(e) => setEditForm(f => f ? { ...f, firstName: e.target.value } : f)} />
              <TextField label="Last name" value={editForm.lastName || ''} onChange={(e) => setEditForm(f => f ? { ...f, lastName: e.target.value } : f)} />
              <TextField label="Email" type="email" required value={editForm.email} onChange={(e) => setEditForm(f => f ? { ...f, email: e.target.value } : f)} />
              <TextField label="Role" value={editForm.role || ''} onChange={(e) => setEditForm(f => f ? { ...f, role: e.target.value } : f)} />
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography variant="body2">Active</Typography>
                <Switch checked={!!editForm.isActive} onChange={(e) => setEditForm(f => f ? { ...f, isActive: e.target.checked } : f)} />
              </Stack>
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={saveEdit} disabled={!editForm?.email?.trim()}>Save</Button>
        </DialogActions>
      </Dialog>

      {/* Create/Invite dialog */}
      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Invite/Create Client</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="First name" value={createForm.firstName} onChange={(e) => setCreateForm(f => ({ ...f, firstName: e.target.value }))} />
            <TextField label="Last name" value={createForm.lastName} onChange={(e) => setCreateForm(f => ({ ...f, lastName: e.target.value }))} />
            <TextField label="Email" type="email" required value={createForm.email} onChange={(e) => setCreateForm(f => ({ ...f, email: e.target.value }))} />
            <Alert severity="info">An email with a password reset link will be sent to the client.</Alert>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={create} disabled={!createForm.email.trim()}>Create</Button>
        </DialogActions>
      </Dialog>

      {/* Delete confirm */}
      <Dialog open={!!confirm} onClose={() => setConfirm(null)}>
        <DialogTitle>Delete Client</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete "{confirm?.name}"?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm(null)}>Cancel</Button>
          <Button color="error" onClick={async () => { if (confirm?.id) { await remove(confirm.id); setConfirm(null); } }}>Delete</Button>
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

export default AdminClients;

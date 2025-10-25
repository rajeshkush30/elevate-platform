import React, { useEffect, useState } from 'react';
import { Container, Typography, Paper, Box, Table, TableHead, TableRow, TableCell, TableBody, Button, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { getClients, createClient, updateClient, deleteClient } from '../api/admin';
import ClientForm from '../components/ClientForm';

const Clients: React.FC = () => {
  const [clients, setClients] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const data = await getClients();
      setClients(data || []);
    } catch (e) {
      console.error('Failed to load clients', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreateOpen = () => { setEditing(null); setFormOpen(true); };
  const handleEditOpen = (c: any) => { setEditing(c); setFormOpen(true); };
  const handleCloseForm = () => { setFormOpen(false); setEditing(null); };

  const handleSave = async (payload: any) => {
    if (editing) {
      const updated = await updateClient(editing.id, { ...editing, ...payload });
      setClients((s) => s.map((x) => (x.id === updated.id ? updated : x)));
    } else {
      const createdResp = await createClient({ ...payload });
      // createdResp is { user, temporaryPassword }
      const created = createdResp.user || createdResp;
      setClients((s) => [created, ...s]);
      if (createdResp.temporaryPassword) {
        // Show temp password to admin so they can share with client
        window.alert(`Client created. Temporary password: ${createdResp.temporaryPassword}`);
      } else {
        window.alert('Client created. Please tell the client to use the password reset flow to set a password.');
      }
    }
  };

  const handleDelete = async (c: any) => {
    if (!window.confirm(`Delete ${c.email}?`)) return;
    try {
      await deleteClient(c.id);
      setClients((s) => s.filter((x) => x.id !== c.id));
    } catch (e) {
      console.error('Delete failed', e);
      alert('Delete failed');
    }
  };

  return (
    <Container>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h5">Clients</Typography>
        <Paper sx={{ mt: 2, p: 2 }}>
          <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Button variant="contained" onClick={handleCreateOpen}>Create New Client</Button>
            <Button onClick={load} disabled={loading}>Refresh</Button>
          </Box>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Created At</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {clients.map((c) => (
                <TableRow key={c.id}>
                  <TableCell>{c.id}</TableCell>
                  <TableCell>{c.firstName || c.fullName || c.companyName}</TableCell>
                  <TableCell>{c.email}</TableCell>
                  <TableCell>{c.createdAt}</TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => handleEditOpen(c)} sx={{ mr: 1 }}><EditIcon fontSize="small" /></IconButton>
                    <IconButton size="small" color="error" onClick={() => handleDelete(c)}><DeleteIcon fontSize="small" /></IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      </Box>

      <ClientForm open={formOpen} onClose={handleCloseForm} onSave={handleSave} initial={editing} />
    </Container>
  );
};

export default Clients;

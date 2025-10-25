import { useEffect, useMemo, useState } from 'react';
import {
  Container,
  Typography,
  Paper,
  Grid,
  TextField,
  MenuItem,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TablePagination,
  Chip,
  Button,
  Stack,
} from '@mui/material';
import { useToast } from '../components/ToastProvider';
import { listAssignments, AssignmentItem, PageResponse } from '../api/adminAssignments';

// Backend wired list with graceful fallback

const AdminAssignmentsList = () => {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState<PageResponse<AssignmentItem>>({ content: [], totalElements: 0, totalPages: 0, number: 0, size: rowsPerPage });

  const load = async () => {
    try {
      setLoading(true);
      const res = await listAssignments({ page, size: rowsPerPage, query, status });
      setData(res);
    } catch (e) {
      showToast('Failed to load assignments', 'error');
      setData({ content: [], totalElements: 0, totalPages: 0, number: 0, size: rowsPerPage });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [page, rowsPerPage]);
  // debounce filter changes
  useEffect(() => {
    const t = setTimeout(() => { setPage(0); load(); }, 300);
    return () => clearTimeout(t);
  }, [query, status]);

  const rows = useMemo(() => data.content.map(r => ({
    id: r.id,
    user: r.userEmail,
    module: r.moduleName,
    dueAt: r.dueAt,
    status: r.status,
    createdAt: r.createdAt,
  })), [data]);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <Typography variant="h5">Assignments</Typography>
        <Button variant="contained" href="/admin/assignments/create">Create Assignment</Button>
      </Stack>

      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <TextField label="Search (user/module/id)" value={query} onChange={(e) => setQuery(e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField select label="Status" value={status} onChange={(e) => setStatus(e.target.value)} fullWidth>
              <MenuItem value="">All</MenuItem>
              <MenuItem value="ASSIGNED">ASSIGNED</MenuItem>
              <MenuItem value="IN_PROGRESS">IN_PROGRESS</MenuItem>
              <MenuItem value="COMPLETED">COMPLETED</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} md={3}>
            <Button variant="outlined" fullWidth onClick={() => { setQuery(''); setStatus(''); }}>Clear</Button>
          </Grid>
        </Grid>
      </Paper>

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>User</TableCell>
              <TableCell>Module</TableCell>
              <TableCell>Due</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6}>Loading...</TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6}>No data</TableCell>
              </TableRow>
            ) : rows.map((r) => (
              <TableRow key={r.id} hover>
                <TableCell>{r.id}</TableCell>
                <TableCell>{r.user}</TableCell>
                <TableCell>{r.module}</TableCell>
                <TableCell>{r.dueAt ? new Date(r.dueAt).toLocaleString() : '-'}</TableCell>
                <TableCell><Chip size="small" label={r.status} color={r.status === 'COMPLETED' ? 'success' : r.status === 'IN_PROGRESS' ? 'warning' : 'default'} /></TableCell>
                <TableCell>{new Date(r.createdAt).toLocaleString()}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={data.totalElements}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
        />
      </Paper>
    </Container>
  );
};

export default AdminAssignmentsList;

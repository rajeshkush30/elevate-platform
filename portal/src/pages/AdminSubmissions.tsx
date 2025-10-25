import { useEffect, useMemo, useState } from 'react';
import {
  Container,
  Typography,
  Paper,
  Grid,
  TextField,
  MenuItem,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TablePagination,
  Stack,
  Chip,
  Skeleton,
} from '@mui/material';

type Submission = {
  id: string;
  user: string;
  stage: string;
  score: number;
  status: 'PENDING' | 'COMPLETED';
  createdAt: string;
};

const MOCK: Submission[] = Array.from({ length: 57 }).map((_, i) => ({
  id: String(1000 + i),
  user: `user${(i % 9) + 1}@demo.com`,
  stage: ['EARLY', 'GROWTH', 'SCALE'][i % 3],
  score: Math.floor(Math.random() * 100),
  status: i % 4 === 0 ? 'PENDING' : 'COMPLETED',
  createdAt: new Date(Date.now() - i * 86400000).toISOString(),
}));

const AdminSubmissions = () => {
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState('');
  const [stage, setStage] = useState('');
  const [status, setStatus] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Filter mock data (replace with server call later)
  const filtered = useMemo(() => {
    let arr = MOCK;
    if (query) arr = arr.filter(x => x.user.toLowerCase().includes(query.toLowerCase()) || x.id.includes(query));
    if (stage) arr = arr.filter(x => x.stage === stage);
    if (status) arr = arr.filter(x => x.status === status);
    if (dateFrom) arr = arr.filter(x => new Date(x.createdAt) >= new Date(dateFrom));
    if (dateTo) arr = arr.filter(x => new Date(x.createdAt) <= new Date(dateTo));
    return arr;
  }, [query, stage, status, dateFrom, dateTo]);

  useEffect(() => {
    setLoading(true);
    const t = setTimeout(() => setLoading(false), 500); // simulate
    return () => clearTimeout(t);
  }, [query, stage, status, dateFrom, dateTo, page, rowsPerPage]);

  const paged = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>Submissions</Typography>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={3}>
            <TextField label="Search (user or ID)" value={query} onChange={(e) => setQuery(e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={6} md={2}>
            <TextField select label="Stage" value={stage} onChange={(e) => setStage(e.target.value)} fullWidth>
              <MenuItem value="">All</MenuItem>
              <MenuItem value="EARLY">EARLY</MenuItem>
              <MenuItem value="GROWTH">GROWTH</MenuItem>
              <MenuItem value="SCALE">SCALE</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={6} md={2}>
            <TextField select label="Status" value={status} onChange={(e) => setStatus(e.target.value)} fullWidth>
              <MenuItem value="">All</MenuItem>
              <MenuItem value="PENDING">PENDING</MenuItem>
              <MenuItem value="COMPLETED">COMPLETED</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={6} md={2}>
            <TextField label="From" type="date" InputLabelProps={{ shrink: true }} value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={6} md={2}>
            <TextField label="To" type="date" InputLabelProps={{ shrink: true }} value={dateTo} onChange={(e) => setDateTo(e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={12} md={1}>
            <Button fullWidth variant="outlined" onClick={() => { setQuery(''); setStage(''); setStatus(''); setDateFrom(''); setDateTo(''); }}>Clear</Button>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 0 }}>
        {loading ? (
          <Stack sx={{ p: 2 }} spacing={1}>
            <Skeleton variant="text" />
            <Skeleton variant="rectangular" height={180} />
          </Stack>
        ) : (
          <>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>User</TableCell>
                  <TableCell>Stage</TableCell>
                  <TableCell>Score</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Date</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paged.map((s) => (
                  <TableRow key={s.id} hover>
                    <TableCell>{s.id}</TableCell>
                    <TableCell>{s.user}</TableCell>
                    <TableCell><Chip label={s.stage} color="primary" size="small" /></TableCell>
                    <TableCell>{s.score}</TableCell>
                    <TableCell>
                      <Chip label={s.status} color={s.status === 'COMPLETED' ? 'success' : 'default'} size="small" />
                    </TableCell>
                    <TableCell>{new Date(s.createdAt).toLocaleDateString()}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={filtered.length}
              page={page}
              onPageChange={(_, p) => setPage(p)}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
              rowsPerPageOptions={[10, 20, 50]}
            />
          </>
        )}
      </Paper>
    </Container>
  );
};

export default AdminSubmissions;

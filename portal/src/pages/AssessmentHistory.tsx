import { useEffect, useState } from 'react';
import { Container, Paper, Typography, Table, TableHead, TableRow, TableCell, TableBody, Chip, Stack, Button } from '@mui/material';

interface HistoryItem {
  submissionId?: string;
  stage?: string;
  score?: number;
  summary?: string;
  savedAt?: string;
}

const AssessmentHistory = () => {
  const [items, setItems] = useState<HistoryItem[]>([]);

  useEffect(() => {
    try {
      const raw = localStorage.getItem('assessmentHistory');
      const arr = raw ? JSON.parse(raw) : [];
      // Include lastAssessmentResult if not already present
      const lastRaw = localStorage.getItem('lastAssessmentResult');
      if (lastRaw) {
        const last = JSON.parse(lastRaw);
        const exists = arr.some((x: HistoryItem) => x.savedAt === last.savedAt);
        if (!exists) arr.unshift(last);
      }
      setItems(arr);
    } catch {
      setItems([]);
    }
  }, []);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h5" gutterBottom>Assessment History</Typography>
      <Paper sx={{ p: 2 }}>
        {items.length === 0 ? (
          <Typography color="text.secondary">No history yet. Complete an assessment to see it here.</Typography>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Date</TableCell>
                <TableCell>Stage</TableCell>
                <TableCell>Score</TableCell>
                <TableCell>Summary</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.map((it, idx) => (
                <TableRow key={idx} hover>
                  <TableCell>{it.savedAt ? new Date(it.savedAt).toLocaleString() : '-'}</TableCell>
                  <TableCell>
                    <Chip label={it.stage ?? '-'} color="primary" size="small" />
                  </TableCell>
                  <TableCell>{typeof it.score === 'number' ? it.score : '-'}</TableCell>
                  <TableCell>{it.summary ?? '-'}</TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                      <Button size="small" variant="outlined" href="/assessment/result">View</Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Paper>
    </Container>
  );
};

export default AssessmentHistory;

import React, { useEffect, useState } from 'react';
import { Container, Typography, Paper, Box, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import { getSubmissions } from '../api/admin';

const Submissions: React.FC = () => {
  const [submissions, setSubmissions] = useState<any[]>([]);

  useEffect(() => {
    (async () => {
      try {
        const data = await getSubmissions();
        setSubmissions(data);
      } catch (e) {
        console.error('Failed to load submissions', e);
      }
    })();
  }, []);

  return (
    <Container>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h5">Questionnaire Submissions</Typography>
        <Paper sx={{ mt: 2, p: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>User ID</TableCell>
                <TableCell>Score</TableCell>
                <TableCell>Stage</TableCell>
                <TableCell>Created At</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {submissions.map((s) => (
                <TableRow key={s.id}>
                  <TableCell>{s.id}</TableCell>
                  <TableCell>{s.userId}</TableCell>
                  <TableCell>{s.score}</TableCell>
                  <TableCell>{s.stage}</TableCell>
                  <TableCell>{s.createdAt}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      </Box>
    </Container>
  );
};

export default Submissions;

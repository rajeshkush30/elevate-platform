import { useEffect, useState } from 'react';
import { Container, Paper, Typography, List, ListItem, ListItemText, Chip, Stack, Button } from '@mui/material';
import { listMyAssessments, MyAssessment } from '../api/myAssessments';
import { useNavigate } from 'react-router-dom';

const statusColor = (s: MyAssessment['status']) => {
  switch (s) {
    case 'ASSIGNED': return 'default';
    case 'IN_PROGRESS': return 'warning';
    case 'SUBMITTED': return 'info';
    case 'SCORED': return 'success';
    default: return 'default';
  }
};

export default function MyAssessments() {
  const [items, setItems] = useState<MyAssessment[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const data = await listMyAssessments();
      setItems(data as any);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>My Assessments</Typography>
      <Paper sx={{ p: 2 }}>
        {loading ? (
          <Typography color="text.secondary">Loading...</Typography>
        ) : items.length === 0 ? (
          <Typography color="text.secondary">No assessments yet.</Typography>
        ) : (
          <List>
            {items.map((a: MyAssessment) => (
              <ListItem key={a.id}
                secondaryAction={
                  <Stack direction="row" spacing={1}>
                    <Chip label={a.status} color={statusColor(a.status) as any} size="small" />
                    <Button variant="contained" size="small" onClick={() => navigate(`/client/assessments/${a.id}`)}>
                      {a.status === 'ASSIGNED' ? 'Start' : 'Open'}
                    </Button>
                  </Stack>
                }
              >
                <ListItemText primary={a.name}
                  secondary={(a.dueDate ? `Due: ${a.dueDate}` : '') + (a.score != null ? `  Score: ${a.score}` : '')} />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Container>
  );
}

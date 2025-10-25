import React, { useEffect, useState } from 'react';
import { Container, Typography, Paper, Box, List, ListItem, ListItemText } from '@mui/material';
import { getQuestions } from '../api/admin';

const Questions: React.FC = () => {
  const [questions, setQuestions] = useState<any[]>([]);

  useEffect(() => {
    (async () => {
      try {
        const data = await getQuestions();
        setQuestions(data);
      } catch (e) {
        console.error('Failed to load questions', e);
      }
    })();
  }, []);

  return (
    <Container>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h5">Questions</Typography>
        <Paper sx={{ mt: 2, p: 2 }}>
          <List>
            {questions.map((q) => (
              <ListItem key={q.id} divider>
                <ListItemText primary={q.text} secondary={q.options} />
              </ListItem>
            ))}
          </List>
        </Paper>
      </Box>
    </Container>
  );
};

export default Questions;

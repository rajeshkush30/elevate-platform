import { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, CircularProgress, Stack, TextField, Typography } from '@mui/material';
import { listPrompts, updatePrompt, AIPrompt } from '../api/adminPrompts';

const AdminAIPrompts = () => {
  const [loading, setLoading] = useState(true);
  const [prompts, setPrompts] = useState<AIPrompt[]>([]);
  const [dirty, setDirty] = useState<Record<string, string>>({});

  const load = async () => {
    setLoading(true);
    try {
      const data = await listPrompts();
      setPrompts(data);
      setDirty(Object.fromEntries(data.map(p => [p.key, p.text])));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const save = async (key: string) => {
    const text = dirty[key] ?? '';
    const saved = await updatePrompt(key, text);
    setPrompts(prev => prev.map(p => p.key === key ? saved : p));
  };

  if (loading) return (
    <Box p={3}><CircularProgress /></Box>
  );

  return (
    <Box p={3}>
      <Typography variant="h5" gutterBottom>AI Prompts</Typography>
      <Stack spacing={2}>
        {prompts.map(p => (
          <Card key={p.key}>
            <CardContent>
              <Stack spacing={1}>
                <Typography variant="subtitle1">{p.key}</Typography>
                <TextField
                  multiline
                  minRows={4}
                  value={dirty[p.key] ?? ''}
                  onChange={(e)=> setDirty(prev => ({ ...prev, [p.key]: e.target.value }))}
                />
                <Stack direction="row" justifyContent="flex-end">
                  <Button variant="contained" onClick={()=> save(p.key)}>Save</Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Box>
  );
};

export default AdminAIPrompts;

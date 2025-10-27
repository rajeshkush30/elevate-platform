import { useEffect, useMemo, useState } from 'react';
import { Container, Card, CardContent, Typography, Stack, Button, Alert, CircularProgress } from '@mui/material';
import { getAssignedTree, startStage, completeStage, type AssignedTree } from '../api/training';
import { useToast } from '../components/ToastProvider';

const Training = () => {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(true);
  const [tree, setTree] = useState<AssignedTree>([]);
  const [error, setError] = useState<string | null>(null);
  const [starting, setStarting] = useState<number | null>(null);

  useEffect(() => {
    let mounted = true;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const t = await getAssignedTree();
        if (mounted) setTree(t || []);
      } catch (e: any) {
        if (mounted) setError(e?.message || 'Failed to load training');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  const stages = useMemo(() => {
    const arr: { id?: number; name: string; module: string }[] = [];
    tree.forEach(m => m.segments.forEach(seg => seg.stages.forEach(st => arr.push({ id: st.id, name: st.name, module: m.name }))));
    return arr;
  }, [tree]);

  const onStart = async (stageId?: number) => {
    if (!stageId) return;
    try {
      setStarting(stageId);
      const res = await startStage(stageId);
      showToast('Training started', 'success');
      if (res?.launchUrl) {
        window.open(res.launchUrl, '_blank');
      }
    } catch (e: any) {
      if (e?.response?.status === 402) {
        showToast('Payment required via Zoho. Please complete payment and try again.', 'warning');
      } else {
        showToast(e?.message || 'Failed to start stage', 'error');
      }
    } finally {
      setStarting(null);
    }
  };

  const onComplete = async (stageId?: number) => {
    if (!stageId) return;
    try {
      await completeStage(stageId, {});
      showToast('Stage completion submitted', 'success');
    } catch (e: any) {
      showToast(e?.message || 'Failed to complete stage', 'error');
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h5" gutterBottom>Training Assignments</Typography>
      {loading && <Stack direction="row" spacing={2} alignItems="center"><CircularProgress size={20} /><Typography>Loading...</Typography></Stack>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {!loading && stages.length === 0 && (
        <Alert severity="info">No assignments yet. After payment via Zoho and entitlement grant, stages will be available here.</Alert>
      )}
      <Stack spacing={2} sx={{ mt: 2 }}>
        {stages.map((s, idx) => (
          <Card key={`${s.id}-${idx}`}>
            <CardContent>
              <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'start', sm: 'center' }} spacing={2}>
                <div>
                  <Typography variant="subtitle1">{s.name}</Typography>
                  <Typography variant="body2" color="text.secondary">Module: {s.module}</Typography>
                </div>
                <Stack direction="row" spacing={1}>
                  <Button variant="contained" onClick={() => onStart(s.id)} disabled={starting === s.id}>Start</Button>
                  <Button variant="outlined" onClick={() => onComplete(s.id)}>Complete</Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Container>
  );
};

export default Training;

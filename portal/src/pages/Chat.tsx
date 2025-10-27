import { useMemo, useState } from 'react';
import { Box, Paper, Typography, TextField, IconButton, List, ListItem, ListItemText, Button, Stack, Alert } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import { leadIntake, LeadIntakeRequest } from '../api/chat';

// Simple public chatbot per doc: capture inquiry and create CRM lead (mock)
export default function Chat() {
  type Msg = { role: 'bot' | 'user'; text: string };
  const [messages, setMessages] = useState<Msg[]>([
    { role: 'bot', text: "Hello! I'm Elevate's assistant. What's your name?" },
  ]);
  const [input, setInput] = useState('');
  const [step, setStep] = useState<0 | 1 | 2 | 3 | 4>(0);
  const [lead, setLead] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const payloadRef: Partial<LeadIntakeRequest> = {};

  // Lightweight analytics helper (console + GTM dataLayer if available)
  const emitEvent = (name: string, data?: Record<string, any>) => {
    try {
      // eslint-disable-next-line no-console
      console.log('[analytics]', name, data || {});
      const w = window as any;
      if (Array.isArray(w.dataLayer)) {
        w.dataLayer.push({ event: name, ...(data || {}) });
      }
    } catch {
      // ignore
    }
  };

  // Stage hinting / quick pre-score from inquiry text
  const detectStageHint = (text: string): { stageHint?: string; preScore?: number } => {
    const t = text.toLowerCase();
    // Very rough heuristics; refine with admin prompts / rule engine later
    if (/(mvp|first customer|no customer|survival|launch|prototype)/.test(t)) {
      return { stageHint: 'StartUp', preScore: 1 };
    }
    if (/(cash flow|ops|hiring|repeatable|stability|process)/.test(t)) {
      return { stageHint: 'Grow', preScore: 2 };
    }
    if (/(scale|systems|automation|multi-location|expansion)/.test(t)) {
      return { stageHint: 'Scale', preScore: 3 };
    }
    if (/(governance|board|succession|risk|controls)/.test(t)) {
      return { stageHint: 'Endurance', preScore: 4 };
    }
    if (/(innovation|evolution|transformation|new markets)/.test(t)) {
      return { stageHint: 'Evolution', preScore: 5 };
    }
    return {};
  };

  const push = (role: 'bot' | 'user', text: string) => setMessages((prev) => [...prev, { role, text }]);

  const handleSend = async () => {
    const text = input.trim();
    if (!text) return;
    setInput('');
    setError(null);
    push('user', text);


    try {
      if (step === 0) {
        payloadRef.name = text;
        push('bot', 'Thanks! What\'s your email?');
        setStep(1);
        return;
      }
      if (step === 1) {
        // basic email check
        if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(text)) {
          push('bot', 'Please enter a valid email address.');
          return;
        }
        payloadRef.email = text;
        push('bot', 'Your company name? (optional)');
        setStep(2);
        return;
      }
      if (step === 2) {
        if (text && text.length > 0) payloadRef.company = text;
        push('bot', 'Please type your question or inquiry.');
        setStep(3);
        return;
      }
      if (step === 3) {
        // Create lead using simple intake (no external extraction)
        // Ensure we have an email; if missing, ask for it first to avoid 400
        if (!payloadRef.email || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(String(payloadRef.email))) {
          push('bot', 'Please share your email address to proceed.');
          setStep(1);
          return;
        }
        setLoading(true);
        payloadRef.source = 'website-chatbot';
        try {
          const res = await leadIntake({
            name: String(payloadRef.name || ''),
            email: String(payloadRef.email || ''),
            company: payloadRef.company as string | undefined,
            notes: text,
          });
          setLead(res.leadId);
          emitEvent('lead_captured', { leadId: res.leadId, source: payloadRef.source, stageHint: res.stageHint, preScore: res.preScore });
          push('bot', `Thank you! Your details are recorded. Lead ID: ${res.leadId}. We will contact you shortly.`);
          push('bot', 'You can create an account to start the assessment.');
          setStep(4);
        } catch (err: any) {
          // If backend still returns 400, request email explicitly
          if (err?.response?.status === 400) {
            push('bot', 'I couldn\'t find a valid email. Please provide your email address.');
            setStep(1);
          } else if (err?.response?.status === 429) {
            push('bot', 'I\'m getting too many requests right now. Please wait a moment and try again.');
          } else {
            setError(err?.message || 'Failed to create lead');
            push('bot', 'Sorry, I could not create your lead right now. Please try again later.');
          }
        } finally {
          setLoading(false);
        }
        return;
      }
      if (step === 4) {
        push('bot', 'You can go to /register to create an account, or /login to sign in.');
        return;
      }
    } catch (e: any) {
      setLoading(false);
      setError(e?.message || 'Something went wrong. Please try again.');
      push('bot', "Sorry, we couldn't process your request right now. Please try again later.");
    }
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="flex-start" minHeight="calc(100vh - 64px)" p={2} bgcolor="#f7f7f7">
      <Paper elevation={2} sx={{ width: '100%', maxWidth: 720, p: 2 }}>
        <Typography variant="h5" gutterBottom>
          Elevate Chatbot (Lead Capture)
        </Typography>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          This public chatbot captures your basic details and creates a lead in CRM. Currently running in mock mode.
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <Paper variant="outlined" sx={{ p: 1, height: 420, overflowY: 'auto', mb: 2, bgcolor: '#fff' }}>
          <List>
            {messages.map((m, i) => (
              <ListItem key={i} alignItems="flex-start" sx={{ py: 0.5 }}>
                <ListItemText
                  primary={m.role === 'bot' ? 'Assistant' : 'You'}
                  secondary={m.text}
                  primaryTypographyProps={{ fontWeight: m.role === 'bot' ? 600 : 500 }}
                />
              </ListItem>
            ))}
          </List>
        </Paper>

        <Stack direction="row" spacing={1}>
          <TextField
            fullWidth
            size="small"
            placeholder={loading ? 'Please wait...' : 'Type your message...'}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleSend();
            }}
            disabled={loading}
          />
          <IconButton color="primary" onClick={handleSend} disabled={loading}>
            <SendIcon />
          </IconButton>
        </Stack>

        {lead && (
          <Stack direction="row" spacing={1} mt={2}>
            <Button
              variant="contained"
              href={payloadRef.email ? `/register?email=${encodeURIComponent(String(payloadRef.email))}` : '/register'}
              onClick={() => emitEvent('cta_register_click', { leadId: lead })}
            >
              Create Account
            </Button>
            <Button variant="outlined" href="/login" onClick={() => emitEvent('cta_login_click', { leadId: lead })}>Login</Button>
          </Stack>
        )}
      </Paper>
    </Box>
  );
}

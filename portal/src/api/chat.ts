import http from './http';

export type ChatResponse = { reply: string };

export async function sendChat(message: string): Promise<ChatResponse> {
  const { data } = await http.post('/api/v1/chat', { message });
  return data;
}

// Matches server controller: LeadCaptureController.capture
export type LeadIntakeRequest = {
  name: string;
  email: string;
  company?: string;
  profile?: Record<string, any>;
  intents?: string[];
  notes?: string; // free text inquiry
};

export type LeadIntakeResponse = {
  leadId: string;
  preScore?: number;
  stageHint?: string;
  rationale?: string;
  labels?: string[];
};

export async function leadIntake(payload: LeadIntakeRequest): Promise<LeadIntakeResponse> {
  const { data } = await http.post('/api/public/chat/intents', payload);
  return data as LeadIntakeResponse;
}

import api from './http';

export type AIPrompt = {
  key: string;
  text: string;
};

export async function listPrompts(): Promise<AIPrompt[]> {
  const res = await api.get('/api/v1/admin/ai-prompts');
  return res.data;
}

export async function updatePrompt(key: string, text: string): Promise<AIPrompt> {
  const res = await api.put(`/api/v1/admin/ai-prompts/${encodeURIComponent(key)}`, { text });
  return res.data;
}

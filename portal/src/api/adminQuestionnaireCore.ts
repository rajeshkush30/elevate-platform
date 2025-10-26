import http from './http';

export type Questionnaire = { id: string; name: string; version?: string };

// List all questionnaires
export async function listQuestionnaires(): Promise<Questionnaire[]> {
  const { data } = await http.get('/api/v1/admin/questionnaire');
  const arr = Array.isArray(data) ? data : [];
  return arr.map((q: any) => ({ id: String(q.id), name: String(q.name ?? ''), version: q.version ? String(q.version) : undefined }));
}

// Get questionnaire by id
export async function getQuestionnaire(id: string): Promise<Questionnaire> {
  const { data } = await http.get(`/api/v1/admin/questionnaire/${id}`);
  return { id: String(data.id), name: String(data.name ?? ''), version: data.version ? String(data.version) : undefined };
}

// Create questionnaire and return id
export async function createQuestionnaire(payload: { name: string; version?: string }): Promise<string> {
  const body: any = { name: payload.name, version: payload.version ?? undefined };
  const { data } = await http.post('/api/v1/admin/questionnaire', body);
  // Backend returns id as body
  return String(data);
}

export async function deleteQuestionnaire(id: string): Promise<void> {
  await http.delete(`/api/v1/admin/questionnaire/${id}`);
}

export default { listQuestionnaires, getQuestionnaire, createQuestionnaire, deleteQuestionnaire };

import http from './http';

export type Assessment = {
  id: string;
  name: string;
  description?: string;
};

export async function listAssessmentsByStage(stageId: string): Promise<Assessment[]> {
  const { data } = await http.get('/api/v1/admin/assessments', { params: { stageId: Number(stageId) } });
  const arr = Array.isArray(data) ? data : [];
  return arr.map((a: any) => ({ id: String(a.id), name: String(a.name ?? ''), description: a.description ? String(a.description) : undefined }));
}

export async function createAssessment(payload: { stageId: string; name: string; description?: string; questionnaireId?: string }): Promise<Assessment> {
  const body: any = {
    stageId: Number(payload.stageId),
    name: payload.name,
    description: payload.description ?? undefined,
  };
  if (payload.questionnaireId) body.questionnaireId = Number(payload.questionnaireId);
  const { data } = await http.post('/api/v1/admin/assessments', body, { params: { questionnaireId: body.questionnaireId } });
  return { id: String(data.id), name: String(data.name ?? ''), description: data.description ? String(data.description) : undefined };
}

export async function assignAssessment(assessmentId: string, payload: { clientIds: string[]; dueDate?: string }): Promise<void> {
  const body: any = {
    clientIds: (payload.clientIds || []).map((id) => Number(id)),
    dueDate: payload.dueDate || undefined, // expecting LocalDate (YYYY-MM-DD)
  };
  await http.post(`/api/v1/admin/assessments/${assessmentId}/assign`, body);
}

export async function updateAssessment(id: string, payload: { name?: string; description?: string; questionnaireId?: string }): Promise<Assessment> {
  const params: any = {};
  if (payload.questionnaireId) params.questionnaireId = Number(payload.questionnaireId);
  const body: any = { name: payload.name, description: payload.description };
  const { data } = await http.put(`/api/v1/admin/assessments/${id}`, body, { params });
  return { id: String(data.id), name: String(data.name ?? ''), description: data.description ? String(data.description) : undefined };
}

export async function deleteAssessment(id: string): Promise<void> {
  await http.delete(`/api/v1/admin/assessments/${id}`);
}

export default { listAssessmentsByStage, createAssessment, assignAssessment, updateAssessment, deleteAssessment };

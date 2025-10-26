import http from './http';

export type AdminQuestion = { id: string; text: string; type?: string; weight?: number; segmentId?: string };
export type AdminOption = { id: string; questionId: string; label: string; value?: string; order?: number };

export async function listQuestionsByQuestionnaire(questionnaireId: string): Promise<AdminQuestion[]> {
  const { data } = await http.get(`/api/v1/admin/questionnaire/${questionnaireId}/questions`);
  const arr = Array.isArray(data) ? data : [];
  return arr.map((q: any) => ({
    id: String(q.id),
    text: String(q.text ?? ''),
    type: q.type ? String(q.type) : undefined,
    weight: typeof q.weight === 'number' ? q.weight : undefined,
    segmentId: q.segment?.id ? String(q.segment.id) : undefined,
  }));
}

export async function createQuestion(payload: { questionnaireId: string; text: string; type?: string; weight?: number; segmentId?: string; optionsJson?: string }): Promise<string> {
  const body: any = {
    questionnaireId: Number(payload.questionnaireId),
    text: payload.text,
    type: payload.type,
    weight: payload.weight,
    segmentId: payload.segmentId ? Number(payload.segmentId) : undefined,
    optionsJson: payload.optionsJson,
  };
  const { data } = await http.post('/api/v1/admin/questionnaire/questions', body);
  return String(data);
}

// Options CRUD under AdminQuestionContentController
export async function listOptionsByQuestion(questionId: string): Promise<AdminOption[]> {
  const { data } = await http.get(`/api/v1/admin/questionnaire/questions/${questionId}/options`);
  const arr = Array.isArray(data) ? data : [];
  return arr.map((o: any) => ({
    id: String(o.id),
    questionId: String(o.questionId ?? questionId),
    label: String(o.label ?? ''),
    value: o.value != null ? String(o.value) : undefined,
    order: typeof o.order === 'number' ? o.order : undefined,
  }));
}

export async function createOption(questionId: string, payload: { label: string; value?: string; order?: number }): Promise<AdminOption> {
  const body: any = { label: payload.label, value: payload.value, order: payload.order };
  const { data } = await http.post(`/api/v1/admin/questionnaire/questions/${questionId}/options`, body);
  const o = data || {};
  return {
    id: String(o.id),
    questionId: String(o.questionId ?? questionId),
    label: String(o.label ?? payload.label),
    value: o.value != null ? String(o.value) : payload.value,
    order: typeof o.order === 'number' ? o.order : payload.order,
  };
}

export async function updateOption(optionId: string, payload: { label?: string; value?: string; order?: number }): Promise<AdminOption> {
  const body: any = { label: payload.label, value: payload.value, order: payload.order };
  const { data } = await http.put(`/api/v1/admin/questionnaire/options/${optionId}`, body);
  const o = data || {};
  return {
    id: String(o.id ?? optionId),
    questionId: String(o.questionId ?? ''),
    label: String(o.label ?? ''),
    value: o.value != null ? String(o.value) : undefined,
    order: typeof o.order === 'number' ? o.order : undefined,
  };
}

export async function deleteOption(optionId: string): Promise<void> {
  await http.delete(`/api/v1/admin/questionnaire/options/${optionId}`);
}

export default { listQuestionsByQuestionnaire, createQuestion, listOptionsByQuestion, createOption, updateOption, deleteOption };

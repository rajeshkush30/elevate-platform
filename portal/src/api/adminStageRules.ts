import http from './http';

export type StageRule = {
  id: string;
  questionnaireId?: string | null;
  minScore: number;
  maxScore: number;
  priority: number;
  targetStageId: string;
  targetStageName?: string;
};

const BASE = '/api/v1/admin/stage-rules';

export async function listStageRules(questionnaireId?: string): Promise<StageRule[]> {
  const res = await http.get(BASE, { params: questionnaireId ? { questionnaireId } : {} });
  const arr = Array.isArray(res.data) ? res.data : [];
  return arr.map((r: any) => ({
    id: String(r.id),
    questionnaireId: r.questionnaire?.id ? String(r.questionnaire.id) : null,
    minScore: Number(r.minScore ?? 0),
    maxScore: Number(r.maxScore ?? 0),
    priority: Number(r.priority ?? 0),
    targetStageId: r.targetStage?.id ? String(r.targetStage.id) : String(r.targetStageId ?? ''),
    targetStageName: r.targetStage?.name ?? r.targetStageName,
  }));
}

export async function createStageRule(payload: {
  questionnaireId?: string | null;
  minScore: number;
  maxScore: number;
  priority: number;
  targetStageId: string;
}): Promise<string> {
  const body: any = { ...payload };
  if (body.questionnaireId === '') body.questionnaireId = null;
  body.targetStageId = Number(body.targetStageId);
  if (body.questionnaireId != null) body.questionnaireId = Number(body.questionnaireId);
  const res = await http.post(BASE, body);
  return String(res.data);
}

export async function updateStageRule(id: string, payload: {
  questionnaireId?: string | null;
  minScore: number;
  maxScore: number;
  priority: number;
  targetStageId: string;
}): Promise<void> {
  const body: any = { ...payload };
  if (body.questionnaireId === '') body.questionnaireId = null;
  body.targetStageId = Number(body.targetStageId);
  if (body.questionnaireId != null) body.questionnaireId = Number(body.questionnaireId);
  await http.put(`${BASE}/${id}`, body);
}

export async function deleteStageRule(id: string): Promise<void> {
  await http.delete(`${BASE}/${id}`);
}

export default {
  listStageRules,
  createStageRule,
  updateStageRule,
  deleteStageRule,
};

import http from './http';

export type MyAssessment = {
  id: string;
  name: string;
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'SUBMITTED' | 'SCORED';
  dueDate?: string;
  score?: number;
};

export async function listMyAssessments(): Promise<MyAssessment[]> {
  const { data } = await http.get('/api/v1/client/assessments');
  const arr = Array.isArray(data) ? data : [];
  return arr.map((x: any) => ({
    id: String(x.id),
    name: String(x.assessment?.name ?? 'Assessment'),
    status: String(x.status) as MyAssessment['status'],
    dueDate: x.dueDate ? String(x.dueDate) : undefined,
    score: typeof x.score === 'number' ? x.score : undefined,
  }));
}

export type SaveAnswerItem = { questionId: number; answerText?: string | null; optionIds?: number[] };

export async function saveAnswers(clientAssessmentId: string, items: SaveAnswerItem[], submit: boolean): Promise<void> {
  const body = {
    answers: items.map(i => ({ questionId: Number(i.questionId), answerText: i.answerText ?? null, optionIds: (i.optionIds || []).map(Number) })),
    submit,
  } as any;
  await http.post(`/api/v1/client/assessments/${clientAssessmentId}/answers`, body);
}

export type AssessmentQuestion = { id: number; text: string; type?: string; options: { id: number; text: string }[]; existingAnswer?: { answerText?: string | null; optionIds?: number[] } };
export async function getAssessmentDetails(clientAssessmentId: string): Promise<AssessmentQuestion[]> {
  const { data } = await http.get(`/api/v1/client/assessments/${clientAssessmentId}/details`);
  const rawQs = Array.isArray((data as any)?.questions) ? (data as any).questions : [];
  return rawQs.map((q: any) => ({
    id: Number(q.id),
    text: String(q.text ?? q.title ?? ''),
    type: q.type ? String(q.type) : undefined,
    options: Array.isArray(q.options) ? q.options.map((o: any) => ({ id: Number(o.id), text: String(o.text ?? o.label ?? '') })) : [],
    existingAnswer: q.existingAnswer ? {
      answerText: q.existingAnswer.answerText ?? null,
      optionIds: Array.isArray(q.existingAnswer.optionIds) ? q.existingAnswer.optionIds.map((n: any) => Number(n)) : undefined,
    } : undefined,
  }));
}

export default { listMyAssessments, saveAnswers, getAssessmentDetails };

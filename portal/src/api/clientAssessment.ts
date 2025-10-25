import http from './http';

export type CreateAttemptRequest = {
  questionnaireId?: number;
};

export type SubmitAnswersRequest = {
  answers: Array<{ questionId: string | number; value: string }>;
};

export type AssessmentResult = {
  attemptId: number;
  stage: string;
  score: number;
  summary: string;
};

const base = '/api/client/assessment';

export async function createAttempt(payload: CreateAttemptRequest = {}): Promise<number> {
  const res = await http.post(`${base}/attempts`, payload);
  return res.data as number;
}

export async function submitAnswers(attemptId: number, payload: SubmitAnswersRequest): Promise<void> {
  await http.post(`${base}/attempts/${attemptId}/answers`, payload);
}

export async function finalizeAttempt(attemptId: number): Promise<AssessmentResult> {
  const res = await http.post(`${base}/attempts/${attemptId}/finalize`);
  return res.data as AssessmentResult;
}

export default {
  createAttempt,
  submitAnswers,
  finalizeAttempt,
};

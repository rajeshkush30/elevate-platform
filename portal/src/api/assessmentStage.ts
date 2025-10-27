import api from './http';

export type StageView = {
  stage: string | null;
  score: number | null;
  summary: string | null;
};

export async function getStage(clientAssessmentId: number | string): Promise<StageView> {
  const res = await api.get(`/api/v1/client/assessments/${clientAssessmentId}/stage`);
  return res.data;
}

import api from './http';

export type StrategyQuestion = {
  id: number;
  text: string;
  type: 'TEXT' | 'MCQ' | 'MCQ_MULTI' | 'SCALE';
  options?: Array<{ id: number; label: string }>;
};

export type StrategyForm = {
  version: string;
  questions: StrategyQuestion[];
};

export async function getForm(): Promise<StrategyForm> {
  const res = await api.get('/api/v1/client/strategy/form');
  return res.data;
}

export async function submitAnswers(payload: { answers: Array<{ questionId: number; answerText?: string; optionIds?: number[] }> }): Promise<{ status: 'OK' }> {
  const res = await api.post('/api/v1/client/strategy/submit', payload);
  return res.data;
}

export async function getFinalConsultation(): Promise<{ status: 'READY' | 'PENDING'; draft?: string; approved?: boolean }>{
  const res = await api.get('/api/v1/client/consultation/final');
  return res.data;
}

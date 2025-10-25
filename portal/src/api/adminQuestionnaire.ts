import http from './http';

export type SegmentSummary = { id: string; name: string; order?: number };
export type SegmentUpsert = { name: string; order?: number };
export type QuestionSummary = { id: string; segmentId: string; text: string; weight?: number; order?: number };
export type QuestionUpsert = { text: string; weight?: number; order?: number };
export type OptionSummary = { id: string; questionId: string; label: string; value: string; order?: number };
export type OptionUpsert = { label: string; value: string; order?: number };
export type ReorderItem = { id: string; order: number };

const SEGMENTS_BASE = '/api/v1/admin/questionnaire/segments';
const QUESTIONS_BASE = '/api/v1/admin/questionnaire';

// Segments
const listSegments = async (): Promise<SegmentSummary[]> => {
  const res = await http.get(SEGMENTS_BASE);
  const data = Array.isArray(res.data) ? res.data : [];
  return data.map((s: any) => ({ id: String(s.id), name: String(s.name ?? ''), order: s.order }));
};

const createSegment = async (payload: SegmentUpsert): Promise<SegmentSummary> => {
  const res = await http.post(SEGMENTS_BASE, payload);
  const s = res.data;
  return { id: String(s.id), name: String(s.name ?? ''), order: s.order };
};

const updateSegment = async (id: string, payload: SegmentUpsert): Promise<SegmentSummary> => {
  const res = await http.put(`${SEGMENTS_BASE}/${id}`, payload);
  const s = res.data;
  return { id: String(s.id), name: String(s.name ?? ''), order: s.order };
};

const deleteSegment = async (id: string): Promise<void> => {
  await http.delete(`${SEGMENTS_BASE}/${id}`);
};

const reorderSegments = async (items: ReorderItem[]): Promise<void> => {
  await http.patch(`${SEGMENTS_BASE}/reorder`, items.map(i => ({ id: Number(i.id), order: i.order })));
};

// Questions
const listQuestions = async (segmentId: string): Promise<QuestionSummary[]> => {
  const res = await http.get(`${QUESTIONS_BASE}/segments/${segmentId}/questions`);
  const arr = Array.isArray(res.data) ? res.data : [];
  return arr.map((q: any) => ({
    id: String(q.id),
    segmentId: String(q.segmentId),
    text: String(q.text ?? ''),
    weight: q.weight,
    order: q.order,
  }));
};

const createQuestion = async (segmentId: string, payload: QuestionUpsert): Promise<QuestionSummary> => {
  const res = await http.post(`${QUESTIONS_BASE}/segments/${segmentId}/questions`, payload);
  const q = res.data;
  return { id: String(q.id), segmentId: String(q.segmentId), text: String(q.text ?? ''), weight: q.weight, order: q.order };
};

const updateQuestion = async (questionId: string, payload: QuestionUpsert): Promise<QuestionSummary> => {
  const res = await http.put(`${QUESTIONS_BASE}/questions/${questionId}`, payload);
  const q = res.data;
  return { id: String(q.id), segmentId: String(q.segmentId), text: String(q.text ?? ''), weight: q.weight, order: q.order };
};

const deleteQuestion = async (questionId: string): Promise<void> => {
  await http.delete(`${QUESTIONS_BASE}/questions/${questionId}`);
};

const reorderQuestions = async (segmentId: string, items: ReorderItem[]): Promise<void> => {
  await http.patch(`${QUESTIONS_BASE}/segments/${segmentId}/questions/reorder`, items.map(i => ({ id: Number(i.id), order: i.order })));
};

// Options
const listOptions = async (questionId: string): Promise<OptionSummary[]> => {
  const res = await http.get(`${QUESTIONS_BASE}/questions/${questionId}/options`);
  const arr = Array.isArray(res.data) ? res.data : [];
  return arr.map((o: any) => ({
    id: String(o.id),
    questionId: String(o.questionId),
    label: String(o.label ?? ''),
    value: String(o.value ?? ''),
    order: o.order,
  }));
};

const createOption = async (questionId: string, payload: OptionUpsert): Promise<OptionSummary> => {
  const res = await http.post(`${QUESTIONS_BASE}/questions/${questionId}/options`, payload);
  const o = res.data;
  return { id: String(o.id), questionId: String(o.questionId), label: String(o.label ?? ''), value: String(o.value ?? ''), order: o.order };
};

const updateOption = async (optionId: string, payload: OptionUpsert): Promise<OptionSummary> => {
  const res = await http.put(`${QUESTIONS_BASE}/options/${optionId}`, payload);
  const o = res.data;
  return { id: String(o.id), questionId: String(o.questionId), label: String(o.label ?? ''), value: String(o.value ?? ''), order: o.order };
};

const deleteOption = async (optionId: string): Promise<void> => {
  await http.delete(`${QUESTIONS_BASE}/options/${optionId}`);
};

const reorderOptions = async (questionId: string, items: ReorderItem[]): Promise<void> => {
  await http.patch(`${QUESTIONS_BASE}/questions/${questionId}/options/reorder`, items.map(i => ({ id: Number(i.id), order: i.order })));
};

export default {
  listSegments, createSegment, updateSegment, deleteSegment,
  reorderSegments,
  listQuestions, createQuestion, updateQuestion, deleteQuestion,
  reorderQuestions,
  listOptions, createOption, updateOption, deleteOption,
  reorderOptions,
};

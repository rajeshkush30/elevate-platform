import api from './http';

export type CourseDTO = {
  courseId?: number;
  title?: string;
  description?: string;
  lessons: Array<{
    lessonId: number;
    title: string;
    type: 'VIDEO'|'PDF'|'LINK'|string;
    url: string;
    orderIndex: number;
    hasQuiz?: boolean;
  }>;
};

export async function getCourse(stageId: number): Promise<CourseDTO> {
  const res = await api.get(`/api/v1/client/training/course/${stageId}`);
  return res.data as CourseDTO;
}

export async function startLesson(lessonId: number, body?: { lastPositionSeconds?: number }) {
  await api.post(`/api/v1/client/training/lesson/${lessonId}/start`, body ?? {});
}

export async function completeLesson(lessonId: number, body?: { evidenceUrl?: string }) {
  await api.post(`/api/v1/client/training/lesson/${lessonId}/complete`, body ?? {});
}

export type ClientQuiz = {
  quizId: number | null;
  passScore: number | null;
  questions: Array<{
    id: number;
    text: string;
    type: 'MCQ'|'MCQ_MULTI'|string;
    orderIndex: number;
    options: Array<{ id: number; text: string; orderIndex: number }>;
  }>;
};

export async function getQuiz(lessonId: number): Promise<ClientQuiz> {
  const res = await api.get(`/api/v1/client/training/lesson/${lessonId}/quiz`);
  return res.data as ClientQuiz;
}

export async function submitQuiz(lessonId: number, answers: Array<{ questionId: number; optionIds: number[] }>): Promise<{ score: number; passed: boolean }> {
  const res = await api.post(`/api/v1/client/training/lesson/${lessonId}/quiz/submit`, { answers });
  return res.data as { score: number; passed: boolean };
}

import api from './http';

export async function listCourses(stageId?: number) {
  const res = await api.get('/api/v1/admin/lms/courses', { params: { stageId } });
  return res.data as Array<{ courseId: number; stageId: number; title: string; description?: string }>;
}

export async function createCourse(body: { stageId: number; title: string; description?: string }) {
  const res = await api.post('/api/v1/admin/lms/courses', body);
  return res.data as { courseId: number };
}

export async function updateCourse(courseId: number, body: { stageId?: number; title?: string; description?: string }) {
  await api.put(`/api/v1/admin/lms/courses/${courseId}`, body);
}

export async function deleteCourse(courseId: number) {
  await api.delete(`/api/v1/admin/lms/courses/${courseId}`);
}

export async function createLesson(courseId: number, body: { title: string; contentType: 'VIDEO'|'PDF'|'LINK'; contentUrl: string; orderIndex?: number; durationSeconds?: number }) {
  const res = await api.post(`/api/v1/admin/lms/courses/${courseId}/lessons`, body);
  return res.data as { lessonId: number };
}

export async function updateLesson(lessonId: number, body: { title?: string; contentType?: 'VIDEO'|'PDF'|'LINK'; contentUrl?: string; orderIndex?: number; durationSeconds?: number }) {
  await api.put(`/api/v1/admin/lms/lessons/${lessonId}`, body);
}

export async function deleteLesson(lessonId: number) {
  await api.delete(`/api/v1/admin/lms/lessons/${lessonId}`);
}

export async function createOrUpdateQuiz(lessonId: number, body: { passScore?: number }) {
  const res = await api.post(`/api/v1/admin/lms/lessons/${lessonId}/quiz`, body);
  return res.data as { quizId: number };
}

export async function addQuestion(quizId: number, body: { text: string; type: 'MCQ'|'MCQ_MULTI'; orderIndex?: number }) {
  const res = await api.post(`/api/v1/admin/lms/quiz/${quizId}/questions`, body);
  return res.data as { questionId: number };
}

export async function addOption(questionId: number, body: { text: string; correct?: boolean; orderIndex?: number }) {
  const res = await api.post(`/api/v1/admin/lms/questions/${questionId}/options`, body);
  return res.data as { optionId: number };
}

export async function deleteQuiz(quizId: number) {
  await api.delete(`/api/v1/admin/lms/quiz/${quizId}`);
}

export async function deleteQuestion(questionId: number) {
  await api.delete(`/api/v1/admin/lms/questions/${questionId}`);
}

export async function deleteOption(optionId: number) {
  await api.delete(`/api/v1/admin/lms/options/${optionId}`);
}

// Quiz tree retrieval and updates
export async function getQuizForLesson(lessonId: number) {
  const res = await api.get(`/api/v1/admin/lms/lessons/${lessonId}/quiz`);
  return res.data as { quizId: number | null; passScore: number | null; questions: Array<{ id: number; text: string; type: 'MCQ'|'MCQ_MULTI'|string; orderIndex: number; options: Array<{ id: number; text: string; correct: boolean; orderIndex: number }> }> };
}

export async function updateQuiz(quizId: number, body: { passScore?: number }) {
  await api.put(`/api/v1/admin/lms/quiz/${quizId}`, body);
}

export async function updateQuestion(questionId: number, body: { text?: string; type?: 'MCQ'|'MCQ_MULTI'; orderIndex?: number }) {
  await api.put(`/api/v1/admin/lms/questions/${questionId}`, body);
}

export async function updateOption(optionId: number, body: { text?: string; correct?: boolean; orderIndex?: number }) {
  await api.put(`/api/v1/admin/lms/options/${optionId}`, body);
}

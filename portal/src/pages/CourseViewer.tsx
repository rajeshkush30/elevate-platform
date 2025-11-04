import { useEffect, useMemo, useState } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import { Box, Button, Card, CardContent, Container, Divider, Grid, Link, Stack, TextField, Typography } from '@mui/material';
import { getCourse, startLesson as startLessonApi, completeLesson as completeLessonApi, getQuiz, submitQuiz, type CourseDTO } from '../api/clientCourse';
import { useToast } from '../components/ToastProvider';

const CourseViewer = () => {
  const { showToast } = useToast();
  const { stageId } = useParams();
  const [course, setCourse] = useState<CourseDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [evidence, setEvidence] = useState<Record<number, string>>({});
  const [selectedOpts, setSelectedOpts] = useState<Record<number, number[]>>({});
  const [activeQuizLesson, setActiveQuizLesson] = useState<number | null>(null);
  const [quiz, setQuiz] = useState<Awaited<ReturnType<typeof getQuiz>> | null>(null);

  const sid = useMemo(() => Number(stageId), [stageId]);

  const load = async () => {
    if (!sid) return;
    setLoading(true);
    try {
      const c = await getCourse(sid);
      setCourse(c);
    } catch (e: any) {
      showToast(e?.message || 'Failed to load course', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [sid]);

  const startLesson = async (lessonId: number) => {
    try { await startLessonApi(lessonId, {}); showToast('Lesson started', 'success'); }
    catch (e: any) { showToast(e?.message || 'Failed to start lesson', 'error'); }
  };

  const completeLesson = async (lessonId: number) => {
    try { await completeLessonApi(lessonId, { evidenceUrl: evidence[lessonId] }); showToast('Lesson completed', 'success'); }
    catch (e: any) { showToast(e?.message || 'Failed to complete lesson', 'error'); }
  };

  const openQuiz = async (lessonId: number) => {
    try { setActiveQuizLesson(lessonId); const q = await getQuiz(lessonId); setQuiz(q); }
    catch (e: any) { showToast(e?.message || 'Failed to load quiz', 'error'); }
  };

  const toggleOption = (qId: number, optId: number, multi: boolean) => {
    setSelectedOpts(prev => {
      const arr = prev[qId] || [];
      if (multi) {
        return { ...prev, [qId]: arr.includes(optId) ? arr.filter(x => x !== optId) : [...arr, optId] };
      }
      return { ...prev, [qId]: [optId] };
    });
  };

  const submitQuizAnswers = async (lessonId: number) => {
    try {
      const answers = (quiz?.questions || []).map(q => ({ questionId: q.id, optionIds: selectedOpts[q.id] || [] }));
      const res = await submitQuiz(lessonId, answers);
      showToast(`Score: ${res.score} - ${res.passed ? 'Passed' : 'Failed'}`, res.passed ? 'success' : 'warning');
    } catch (e: any) {
      showToast(e?.message || 'Failed to submit quiz', 'error');
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5">Course</Typography>
        <Link component={RouterLink} to="/training">Back to Training</Link>
      </Stack>

      {!course ? (
        <Typography color="text.secondary">{loading ? 'Loading...' : 'No course found for this stage.'}</Typography>
      ) : (
        <>
          <Typography variant="h6" gutterBottom>{course.title}</Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>{course.description}</Typography>
          <Divider sx={{ my: 2 }} />
          <Grid container spacing={2}>
            {course.lessons.map((l, idx) => (
              <Grid key={l.lessonId} item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="subtitle1">{idx+1}. {l.title}</Typography>
                    <Typography variant="caption" color="text.secondary">{l.type} • Order {l.orderIndex}</Typography>
                    <Box sx={{ mt: 1 }}>
                      <Link href={l.url} target="_blank" rel="noopener">Open content</Link>
                    </Box>
                    <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                      <Button size="small" variant="contained" onClick={() => startLesson(l.lessonId)}>Start</Button>
                      <TextField size="small" placeholder="Evidence URL (optional)" value={evidence[l.lessonId] || ''} onChange={(e) => setEvidence(prev => ({ ...prev, [l.lessonId]: e.target.value }))} />
                      <Button size="small" variant="outlined" onClick={() => completeLesson(l.lessonId)}>Complete</Button>
                      {l.hasQuiz && <Button size="small" onClick={() => openQuiz(l.lessonId)}>Open Quiz</Button>}
                    </Stack>

                    {/* Quiz inline for active lesson */}
                    {activeQuizLesson === l.lessonId && quiz && (
                      <Box sx={{ mt: 2, p: 2, border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                        <Typography variant="subtitle2">Quiz (Pass %: {quiz.passScore ?? 'N/A'})</Typography>
                        {quiz.questions.length === 0 && <Typography color="text.secondary">No questions yet.</Typography>}
                        {quiz.questions.map((q, i) => (
                          <Box key={q.id} sx={{ mt: 1 }}>
                            <Typography variant="body2">Q{i+1}. {q.text}</Typography>
                            <Stack sx={{ mt: 0.5 }}>
                              {q.options.map(op => (
                                <Button key={op.id} variant={(selectedOpts[q.id] || []).includes(op.id) ? 'contained' : 'outlined'} size="small" onClick={() => toggleOption(q.id, op.id, q.type === 'MCQ_MULTI')} sx={{ justifyContent: 'flex-start' }}>{op.text}</Button>
                              ))}
                            </Stack>
                          </Box>
                        ))}
                        <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                          <Button variant="contained" onClick={() => submitQuizAnswers(l.lessonId)}>Submit Answers</Button>
                          <Button variant="text" onClick={() => { setActiveQuizLesson(null); setQuiz(null); }}>Close</Button>
                        </Stack>
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </>
      )}
    </Container>
  );
};

export default CourseViewer;

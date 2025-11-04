import { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Grid, MenuItem, Select, TextField, Typography, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions } from '@mui/material';
import { listCourses, createCourse, updateCourse, deleteCourse, createLesson, updateLesson, deleteLesson, createOrUpdateQuiz, addQuestion, addOption, deleteQuiz, getQuizForLesson, updateQuiz, updateQuestion, updateOption, deleteQuestion, deleteOption } from '../api/adminLms';
import { getModuleTree } from '../api/catalog';
import { getCourse as getClientCourse } from '../api/clientCourse';
import { useToast } from '../components/ToastProvider';

const AdminLMS = () => {
  const [stages, setStages] = useState<Array<{ id: number; name: string }>>([]);
  const [stageId, setStageId] = useState<number | ''>('');
  const [courses, setCourses] = useState<Array<{ courseId: number; stageId: number; title: string; description?: string }>>([]);
  const [newCourse, setNewCourse] = useState({ title: '', description: '' });

  const reload = async (sid?: number) => {
    if (!sid) return;
    const cs = await listCourses(sid);
    setCourses(cs);
  };

  useEffect(() => {
    (async () => {
      const cat = await getModuleTree();
      const allStages: Array<{ id: number; name: string }> = [];
      cat.forEach((module: any) => module.segments.forEach((seg: any) => seg.stages.forEach((st: any) => allStages.push({ id: st.id, name: st.name }))));
      setStages(allStages);
    })();
  }, []);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>LMS Management</Typography>

      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
        <Typography>Select Stage:</Typography>
        <Select size="small" value={stageId} onChange={(e) => { const v = Number(e.target.value); setStageId(v); reload(v); }} displayEmpty>
          <MenuItem value=""><em>Choose...</em></MenuItem>
          {stages.map(s => <MenuItem key={s.id} value={s.id}>{s.name} (ID: {s.id})</MenuItem>)}
        </Select>
      </Box>

      {stageId && (
        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" sx={{ mb: 1 }}>Course for selected Stage</Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle1">Existing Course</Typography>
                  {courses.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">No course found for this stage.</Typography>
                  ) : (
                    courses.map(c => (
                      <Box key={c.courseId} sx={{ mt: 1, p: 1, border: '1px solid #eee', borderRadius: 1 }}>
                        <TextField fullWidth label="Title" size="small" sx={{ mb: 1 }} value={c.title} onChange={(e) => setCourses(prev => prev.map(x => x.courseId === c.courseId ? { ...x, title: e.target.value } : x))} />
                        <TextField fullWidth label="Description" size="small" sx={{ mb: 1 }} value={c.description || ''} onChange={(e) => setCourses(prev => prev.map(x => x.courseId === c.courseId ? { ...x, description: e.target.value } : x))} />
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Button variant="contained" size="small" onClick={async () => { await updateCourse(c.courseId, { title: c.title, description: c.description }); await reload(Number(stageId)); }}>Save</Button>
                          <Button variant="outlined" color="error" size="small" onClick={async () => { await deleteCourse(c.courseId); await reload(Number(stageId)); }}>Delete</Button>
                        </Box>
                        <LessonManager courseId={c.courseId} stageId={Number(stageId)} />
                      </Box>
                    ))
                  )}
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle1">Create Course</Typography>
                  <TextField fullWidth label="Title" size="small" sx={{ mt: 1 }} value={newCourse.title} onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })} />
                  <TextField fullWidth label="Description" size="small" sx={{ mt: 1 }} value={newCourse.description} onChange={(e) => setNewCourse({ ...newCourse, description: e.target.value })} />
                  <Button sx={{ mt: 1 }} variant="contained" onClick={async () => { if (!newCourse.title) return; await createCourse({ stageId: Number(stageId), title: newCourse.title, description: newCourse.description }); setNewCourse({ title: '', description: '' }); await reload(Number(stageId)); }}>Create</Button>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>
      )}
    </Box>
  );
};

const LessonManager = ({ courseId, stageId }: { courseId: number; stageId: number }) => {
  const [lessons, setLessons] = useState<Array<any>>([]);
  const [form, setForm] = useState({ title: '', contentType: 'VIDEO', contentUrl: '', orderIndex: 1, durationSeconds: 0 });

  const fetchLessons = async () => {
    // Use axios-based API to include Authorization header
    const data = await getClientCourse(stageId);
    setLessons(data.lessons || []);
  };

  useEffect(() => { fetchLessons(); }, [courseId, stageId]);

  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" sx={{ mb: 1 }}>Lessons</Typography>
      {lessons.map((l) => (
        <Card key={l.lessonId} sx={{ mb: 1 }}>
          <CardContent>
            <Grid container spacing={1}>
              <Grid item xs={12} md={3}><TextField fullWidth size="small" label="Title" value={l.title} onChange={(e) => setLessons(prev => prev.map(x => x.lessonId === l.lessonId ? { ...x, title: e.target.value } : x))} /></Grid>
              <Grid item xs={12} md={2}><TextField fullWidth size="small" label="Type" value={l.type} disabled /></Grid>
              <Grid item xs={12} md={4}><TextField fullWidth size="small" label="URL" value={l.url} onChange={(e) => setLessons(prev => prev.map(x => x.lessonId === l.lessonId ? { ...x, url: e.target.value } : x))} /></Grid>
              <Grid item xs={12} md={1}><TextField fullWidth size="small" type="number" label="#" value={l.orderIndex} onChange={(e) => setLessons(prev => prev.map(x => x.lessonId === l.lessonId ? { ...x, orderIndex: Number(e.target.value) } : x))} /></Grid>
              <Grid item xs={12} md={2}><TextField fullWidth size="small" type="number" label="Duration" value={l.durationSeconds || 0} onChange={(e) => setLessons(prev => prev.map(x => x.lessonId === l.lessonId ? { ...x, durationSeconds: Number(e.target.value) } : x))} /></Grid>
            </Grid>
            <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
              <Button size="small" variant="contained" onClick={async () => { await updateLesson(l.lessonId, { title: l.title, contentUrl: l.url, orderIndex: l.orderIndex, durationSeconds: l.durationSeconds }); await fetchLessons(); }}>Save</Button>
              <Button size="small" variant="outlined" color="error" onClick={async () => { await deleteLesson(l.lessonId); await fetchLessons(); }}>Delete</Button>
              <QuizManager lessonId={l.lessonId} />
            </Box>
          </CardContent>
        </Card>
      ))}

      <Card>
        <CardContent>
          <Typography variant="subtitle2">Add Lesson</Typography>
          <Grid container spacing={1} sx={{ mt: 1 }}>
            <Grid item xs={12} md={3}><TextField fullWidth size="small" label="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></Grid>
            <Grid item xs={12} md={2}>
              <Select fullWidth size="small" value={form.contentType} onChange={(e) => setForm({ ...form, contentType: e.target.value as any })}>
                <MenuItem value="VIDEO">VIDEO</MenuItem>
                <MenuItem value="PDF">PDF</MenuItem>
                <MenuItem value="LINK">LINK</MenuItem>
              </Select>
            </Grid>
            <Grid item xs={12} md={4}><TextField fullWidth size="small" label="URL" value={form.contentUrl} onChange={(e) => setForm({ ...form, contentUrl: e.target.value })} /></Grid>
            <Grid item xs={12} md={1}><TextField fullWidth size="small" type="number" label="#" value={form.orderIndex} onChange={(e) => setForm({ ...form, orderIndex: Number(e.target.value) })} /></Grid>
            <Grid item xs={12} md={2}><TextField fullWidth size="small" type="number" label="Duration" value={form.durationSeconds} onChange={(e) => setForm({ ...form, durationSeconds: Number(e.target.value) })} /></Grid>
          </Grid>
          <Button sx={{ mt: 1 }} variant="contained" onClick={async () => { if (!form.title || !form.contentUrl) return; await createLesson(courseId, form as any); setForm({ title: '', contentType: 'VIDEO', contentUrl: '', orderIndex: 1, durationSeconds: 0 }); await fetchLessons(); }}>Add</Button>
        </CardContent>
      </Card>
    </Box>
  );
};

const QuizManager = ({ lessonId }: { lessonId: number }) => {
  const [quizId, setQuizId] = useState<number | null>(null);
  const [passScore, setPassScore] = useState<number>(70);
  const [questions, setQuestions] = useState<Array<{ id: number; text: string; type: string; orderIndex: number; options: Array<{ id: number; text: string; correct: boolean; orderIndex: number }> }>>([]);
  const [newQ, setNewQ] = useState<{ text: string; type: 'MCQ'|'MCQ_MULTI'; orderIndex: number }>({ text: '', type: 'MCQ', orderIndex: 0 });
  const [newOpt, setNewOpt] = useState<Record<number, { text: string; correct: boolean; orderIndex: number }>>({});
  const { showToast } = useToast();

  // delete confirmation state
  const [confirm, setConfirm] = useState<{ open: boolean; type?: 'quiz'|'question'|'option'; id?: number }>({ open: false });

  // DnD helpers
  const [dragQIndex, setDragQIndex] = useState<number | null>(null);
  const [dragOptCtx, setDragOptCtx] = useState<{ qId: number; index: number } | null>(null);

  const load = async () => {
    const q = await getQuizForLesson(lessonId);
    setQuizId(q.quizId);
    setPassScore(q.passScore ?? 70);
    setQuestions(q.questions || []);
  };

  useEffect(() => { load(); }, [lessonId]);

  const ensureQuiz = async () => {
    if (quizId) return quizId;
    const res = await createOrUpdateQuiz(lessonId, { passScore });
    setQuizId(res.quizId);
    return res.quizId;
  };

  const persistQuestionOrder = async (arr: typeof questions) => {
    // Reassign orderIndex sequentially and persist
    try {
      for (let i = 0; i < arr.length; i++) {
        const q = arr[i];
        if (q.orderIndex !== i) await updateQuestion(q.id, { orderIndex: i });
      }
      showToast('Questions reordered', 'success');
    } catch {
      showToast('Failed to save order', 'error');
    }
  };

  const persistOptionOrder = async (qId: number, opts: { id: number; orderIndex: number; text: string; correct: boolean }[]) => {
    try {
      for (let i = 0; i < opts.length; i++) {
        const op = opts[i];
        if (op.orderIndex !== i) await updateOption(op.id, { orderIndex: i });
      }
      showToast('Options reordered', 'success');
    } catch {
      showToast('Failed to save option order', 'error');
    }
  };

  return (
    <Box sx={{ mt: 1, p: 1, border: '1px dashed', borderColor: 'divider', borderRadius: 1 }}>
      <Typography variant="subtitle2">Quiz</Typography>
      <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mt: 1 }}>
        <TextField size="small" type="number" label="Pass %" value={passScore} onChange={(e) => setPassScore(Number(e.target.value))} sx={{ width: 120 }} />
        <Button size="small" variant="contained" onClick={async () => { const id = await ensureQuiz(); await updateQuiz(id, { passScore }); showToast('Quiz saved', 'success'); }}>Save Quiz</Button>
        {quizId && <Button size="small" color="error" variant="outlined" onClick={() => setConfirm({ open: true, type: 'quiz', id: quizId! })}>Delete Quiz</Button>}
      </Box>

      {/* Questions list */}
      <Box onDragOver={(e) => e.preventDefault()}>
      {questions.map((q, idx) => (
        <Card key={q.id} sx={{ mt: 1 }} draggable onDragStart={() => setDragQIndex(idx)} onDrop={async () => {
          if (dragQIndex === null || dragQIndex === idx) return;
          const copy = [...questions];
          const [item] = copy.splice(dragQIndex, 1);
          copy.splice(idx, 0, item);
          // normalize orderIndex locally
          setQuestions(copy.map((x, i) => ({ ...x, orderIndex: i })));
          setDragQIndex(null);
          await persistQuestionOrder(copy);
        }}>
          <CardContent>
            <Grid container spacing={1}>
              <Grid item xs={12} md={5}><TextField fullWidth size="small" label={`Q${idx+1} Text`} value={q.text} onChange={(e) => setQuestions(prev => prev.map(x => x.id === q.id ? { ...x, text: e.target.value } : x))} /></Grid>
              <Grid item xs={12} md={2}>
                <Select fullWidth size="small" value={q.type} onChange={(e) => setQuestions(prev => prev.map(x => x.id === q.id ? { ...x, type: e.target.value as any } : x))}>
                  <MenuItem value="MCQ">MCQ</MenuItem>
                  <MenuItem value="MCQ_MULTI">MCQ_MULTI</MenuItem>
                </Select>
              </Grid>
              <Grid item xs={12} md={2}><TextField fullWidth size="small" type="number" label="Order" value={q.orderIndex} onChange={(e) => setQuestions(prev => prev.map(x => x.id === q.id ? { ...x, orderIndex: Number(e.target.value) } : x))} /></Grid>
              <Grid item xs={12} md={3}>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button size="small" variant="contained" onClick={async () => { await updateQuestion(q.id, { text: q.text, type: q.type as any, orderIndex: q.orderIndex }); showToast('Question saved', 'success'); await load(); }}>Save</Button>
                  <Button size="small" color="error" variant="outlined" onClick={() => setConfirm({ open: true, type: 'question', id: q.id })}>Delete</Button>
                </Box>
              </Grid>
            </Grid>

            {/* Options for question */}
            <Box sx={{ mt: 1, pl: 1 }} onDragOver={(e) => e.preventDefault()}>
              {q.options.map((op, i) => (
                <Grid key={op.id} container spacing={1} sx={{ mb: 0.5 }} draggable onDragStart={() => setDragOptCtx({ qId: q.id, index: i })} onDrop={async () => {
                  if (!dragOptCtx || dragOptCtx.qId !== q.id || dragOptCtx.index === i) return;
                  const updated = questions.map(qq => {
                    if (qq.id !== q.id) return qq;
                    const opts = [...qq.options];
                    const [item] = opts.splice(dragOptCtx.index, 1);
                    opts.splice(i, 0, item);
                    return { ...qq, options: opts.map((o, idx2) => ({ ...o, orderIndex: idx2 })) };
                  });
                  setQuestions(updated);
                  setDragOptCtx(null);
                  const newOpts = updated.find(qq => qq.id === q.id)!.options;
                  await persistOptionOrder(q.id, newOpts);
                }}>
                  <Grid item xs={12} md={6}><TextField fullWidth size="small" label={`Option ${i+1}`} value={op.text} onChange={(e) => setQuestions(prev => prev.map(x => x.id === q.id ? { ...x, options: x.options.map(o => o.id === op.id ? { ...o, text: e.target.value } : o) } : x))} /></Grid>
                  <Grid item xs={12} md={2}>
                    <Select fullWidth size="small" value={op.correct ? 'YES' : 'NO'} onChange={(e) => setQuestions(prev => prev.map(x => x.id === q.id ? { ...x, options: x.options.map(o => o.id === op.id ? { ...o, correct: e.target.value === 'YES' } : o) } : x))}>
                      <MenuItem value="YES">Correct</MenuItem>
                      <MenuItem value="NO">Incorrect</MenuItem>
                    </Select>
                  </Grid>
                  <Grid item xs={12} md={2}><TextField fullWidth size="small" type="number" label="Order" value={op.orderIndex} onChange={(e) => setQuestions(prev => prev.map(x => x.id === q.id ? { ...x, options: x.options.map(o => o.id === op.id ? { ...o, orderIndex: Number(e.target.value) } : o) } : x))} /></Grid>
                  <Grid item xs={12} md={2}>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Button size="small" onClick={async () => { await updateOption(op.id, { text: op.text, correct: op.correct, orderIndex: op.orderIndex }); showToast('Option saved', 'success'); await load(); }}>Save</Button>
                      <Button size="small" color="error" onClick={() => setConfirm({ open: true, type: 'option', id: op.id })}>Delete</Button>
                    </Box>
                  </Grid>
                </Grid>
              ))}

              {/* Add option */}
              <Grid container spacing={1} sx={{ mt: 1 }}>
                <Grid item xs={12} md={6}><TextField fullWidth size="small" label="New option" value={newOpt[q.id]?.text || ''} onChange={(e) => setNewOpt(prev => ({ ...prev, [q.id]: { ...(prev[q.id]||{ text:'', correct:false, orderIndex:0 }), text: e.target.value } }))} /></Grid>
                <Grid item xs={12} md={2}>
                  <Select fullWidth size="small" value={(newOpt[q.id]?.correct ? 'YES' : 'NO')} onChange={(e) => setNewOpt(prev => ({ ...prev, [q.id]: { ...(prev[q.id]||{ text:'', correct:false, orderIndex:0 }), correct: e.target.value === 'YES' } }))}>
                    <MenuItem value="YES">Correct</MenuItem>
                    <MenuItem value="NO">Incorrect</MenuItem>
                  </Select>
                </Grid>
                <Grid item xs={12} md={2}><TextField fullWidth size="small" type="number" label="Order" value={newOpt[q.id]?.orderIndex || 0} onChange={(e) => setNewOpt(prev => ({ ...prev, [q.id]: { ...(prev[q.id]||{ text:'', correct:false, orderIndex:0 }), orderIndex: Number(e.target.value) } }))} /></Grid>
                <Grid item xs={12} md={2}><Button fullWidth size="small" variant="contained" onClick={async () => { if (!quizId) return; const no = newOpt[q.id]; if (!no?.text) { showToast('Option text required', 'warning'); return; } await addOption(q.id, { text: no.text, correct: no.correct, orderIndex: no.orderIndex }); setNewOpt(prev => ({ ...prev, [q.id]: { text:'', correct:false, orderIndex:0 } })); showToast('Option added', 'success'); await load(); }}>Add</Button></Grid>
              </Grid>
            </Box>
          </CardContent>
        </Card>
      ))}
      </Box>

      {/* Add question */}
      <Card sx={{ mt: 1 }}>
        <CardContent>
          <Typography variant="subtitle2">Add question</Typography>
          <Grid container spacing={1} sx={{ mt: 0.5 }}>
            <Grid item xs={12} md={6}><TextField fullWidth size="small" label="Question text" value={newQ.text} onChange={(e) => setNewQ({ ...newQ, text: e.target.value })} /></Grid>
            <Grid item xs={12} md={2}>
              <Select fullWidth size="small" value={newQ.type} onChange={(e) => setNewQ({ ...newQ, type: e.target.value as any })}>
                <MenuItem value="MCQ">MCQ</MenuItem>
                <MenuItem value="MCQ_MULTI">MCQ_MULTI</MenuItem>
              </Select>
            </Grid>
            <Grid item xs={12} md={2}><TextField fullWidth size="small" type="number" label="Order" value={newQ.orderIndex} onChange={(e) => setNewQ({ ...newQ, orderIndex: Number(e.target.value) })} /></Grid>
            <Grid item xs={12} md={2}><Button fullWidth size="small" variant="contained" onClick={async () => { const id = await ensureQuiz(); if (!newQ.text) { showToast('Question text required', 'warning'); return; } await addQuestion(id, newQ); setNewQ({ text:'', type:'MCQ', orderIndex:0 }); showToast('Question added', 'success'); await load(); }}>Add</Button></Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Delete confirmation dialog */}
      <Dialog open={confirm.open} onClose={() => setConfirm({ open: false })}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this {confirm.type}? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm({ open: false })}>Cancel</Button>
          <Button color="error" variant="contained" onClick={async () => {
            try {
              if (confirm.type === 'quiz' && quizId) { await deleteQuiz(quizId); setQuizId(null); setQuestions([]); }
              if (confirm.type === 'question' && confirm.id) { await deleteQuestion(confirm.id); await load(); }
              if (confirm.type === 'option' && confirm.id) { await deleteOption(confirm.id); await load(); }
              showToast('Deleted', 'success');
            } catch { showToast('Delete failed', 'error'); }
            setConfirm({ open: false });
          }}>Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminLMS;

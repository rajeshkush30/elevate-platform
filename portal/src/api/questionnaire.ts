import axios from 'axios';

export type Option = { label: string; value: string };
export type Question = { id: string; text: string; options?: Option[]; weight?: number };
export type Segment = { id: string; name: string; order?: number; questions: Question[] };
export type QuestionnaireResponse = { submissionId: string; stage: string; score: number; summary: string };

const API_BASE = '/api/v1/questionnaire';

// Simple mock questionnaire used as fallback
const MOCK_QUESTIONS: Question[] = [
  {
    id: 'q1',
    text: 'How clear is your value proposition to customers?',
    options: [
      { label: 'Not clear', value: '0' },
      { label: 'Somewhat clear', value: '1' },
      { label: 'Clear', value: '2' },
    ],
    weight: 1,
  },
  {
    id: 'q2',
    text: 'How strong is your sales pipeline?',
    options: [
      { label: 'Weak', value: '0' },
      { label: 'Moderate', value: '1' },
      { label: 'Strong', value: '2' },
    ],
    weight: 1,
  },
  {
    id: 'q3',
    text: 'Do you have documented processes and SOPs?',
    options: [
      { label: 'No', value: '0' },
      { label: 'Partial', value: '1' },
      { label: 'Yes', value: '2' },
    ],
    weight: 1,
  },
];

const getQuestions = async (): Promise<Question[]> => {
  try {
    const res = await axios.get(API_BASE + '/questions');
    if (res?.data) {
      // Server returns [{ id: number, text: string, options: string[], weight?: number }]
      // where each option string may be "Label:Value" or just label
      const mapped: Question[] = (res.data as any[]).map((q) => {
        const options: Option[] | undefined = Array.isArray(q.options)
          ? (q.options as string[]).map((s: string) => {
              const [label, value] = s.includes(':') ? s.split(':', 2) : [s, '0'];
              return { label: String(label).trim(), value: String(value ?? '0').trim() };
            })
          : undefined;
        return {
          id: String(q.id),
          text: String(q.text ?? ''),
          options,
          weight: typeof q.weight === 'number' ? q.weight : undefined,
        };
      });
      return mapped;
    }
    return MOCK_QUESTIONS;
  } catch (e) {
    // Fallback to mock
    return MOCK_QUESTIONS;
  }
};

// Fetch segments with nested questions. Falls back to a single default segment
// using the flat questions endpoint if segments are unavailable.
const getSegments = async (): Promise<Segment[]> => {
  try {
    const res = await axios.get(API_BASE + '/segments');
    if (res?.data && Array.isArray(res.data)) {
      const segments: Segment[] = (res.data as any[]).map((seg) => ({
        id: String(seg.id),
        name: String(seg.name ?? ''),
        order: typeof seg.order === 'number' ? seg.order : undefined,
        questions: Array.isArray(seg.questions)
          ? seg.questions.map((q: any) => ({
              id: String(q.id),
              text: String(q.text ?? ''),
              weight: typeof q.weight === 'number' ? q.weight : undefined,
              options: Array.isArray(q.options)
                ? (q.options as string[]).map((s: string) => {
                    const [label, value] = s.includes(':') ? s.split(':', 2) : [s, '0'];
                    return { label: String(label).trim(), value: String(value ?? '0').trim() };
                  })
                : undefined,
            }))
          : [],
      }));
      return segments;
    }
    const qs = await getQuestions();
    return [{ id: 'default', name: 'Assessment', order: 1, questions: qs }];
  } catch (e) {
    const qs = await getQuestions();
    return [{ id: 'default', name: 'Assessment', order: 1, questions: qs }];
  }
};

const submitAnswers = async (payload: { userId: string; answers: Array<{ questionId: string; value: string }> }): Promise<QuestionnaireResponse> => {
  try {
    const res = await axios.post(API_BASE + '/submit', payload);
    return res.data as QuestionnaireResponse;
  } catch (e) {
    // Do a local scoring fallback
    const total = payload.answers.reduce((acc, a) => acc + Number(a.value || 0), 0);
    let stage = 'EARLY';
    if (total >= 5) stage = 'MATURE';
    else if (total >= 3) stage = 'GROWTH';

    const summary = `Calculated stage: ${stage}. Score: ${total}. Recommendations: Focus on core gaps.`;
    return { submissionId: 'local-' + Date.now(), stage, score: total, summary };
  }
};

export default { getQuestions, getSegments, submitAnswers };

import http from './http';

export type SubmissionItem = {
  id: string;
  userEmail: string;
  stage: string;
  score: number;
  status: 'PENDING' | 'COMPLETED';
  createdAt: string;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export async function listSubmissions(params: {
  page?: number;
  size?: number;
  sort?: string;
  query?: string;
  stage?: string;
  status?: string;
  from?: string; // ISO date/time
  to?: string;   // ISO date/time
}): Promise<PageResponse<SubmissionItem>> {
  const sp = new URLSearchParams();
  if (params.page != null) sp.set('page', String(params.page));
  if (params.size != null) sp.set('size', String(params.size));
  if (params.sort) sp.set('sort', params.sort);
  if (params.query) sp.set('query', params.query);
  if (params.stage) sp.set('stage', params.stage);
  if (params.status) sp.set('status', params.status);
  if (params.from) sp.set('from', params.from);
  if (params.to) sp.set('to', params.to);
  const { data } = await http.get(`/api/v1/admin/submissions?${sp.toString()}`);
  const content = Array.isArray(data.content) ? data.content.map((x: any) => ({
    id: String(x.id),
    userEmail: String(x.userEmail ?? ''),
    stage: String(x.stage ?? ''),
    score: Number(x.score ?? 0),
    status: String(x.status ?? 'PENDING'),
    createdAt: String(x.createdAt ?? ''),
  })) : [];
  return {
    content,
    totalElements: Number(data.totalElements ?? content.length),
    totalPages: Number(data.totalPages ?? 1),
    number: Number(data.number ?? (params.page ?? 0)),
    size: Number(data.size ?? (params.size ?? content.length)),
  };
}

export default { listSubmissions };

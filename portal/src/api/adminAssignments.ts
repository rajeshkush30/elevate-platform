import http from './http';

export type CreateAssignmentPayload = {
  userId: string | number;
  moduleId: string | number;
  dueAt?: string; // ISO string
};

export type AssignmentItem = {
  id: string;
  userId: string;
  userEmail: string;
  moduleId: string;
  moduleName: string;
  dueAt?: string;
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED';
  createdAt: string;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export async function createAssignment(payload: CreateAssignmentPayload): Promise<void> {
  const body: any = { ...payload };
  body.userId = Number(body.userId);
  body.moduleId = Number(body.moduleId);
  // backend expects LocalDateTime; send ISO without timezone if needed
  if (body.dueAt) {
    // ensure format 'YYYY-MM-DDTHH:mm:ss'
    const d = new Date(body.dueAt);
    const pad = (n: number) => String(n).padStart(2, '0');
    body.dueAt = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
  }
  await http.post('/api/v1/admin/assignments', body);
}

export async function listAssignments(params: {
  page?: number;
  size?: number;
  sort?: string;
  query?: string;
  status?: string;
  from?: string; // ISO
  to?: string;   // ISO
}): Promise<PageResponse<AssignmentItem>> {
  const searchParams = new URLSearchParams();
  if (params.page != null) searchParams.set('page', String(params.page));
  if (params.size != null) searchParams.set('size', String(params.size));
  if (params.sort) searchParams.set('sort', params.sort);
  if (params.query) searchParams.set('query', params.query);
  if (params.status) searchParams.set('status', params.status);
  if (params.from) searchParams.set('from', params.from);
  if (params.to) searchParams.set('to', params.to);
  const { data } = await http.get(`/api/v1/admin/assignments?${searchParams.toString()}`);
  // normalize to strings
  const content = Array.isArray(data.content) ? data.content.map((x: any) => ({
    id: String(x.id),
    userId: String(x.userId),
    userEmail: String(x.userEmail ?? ''),
    moduleId: String(x.moduleId),
    moduleName: String(x.moduleName ?? ''),
    dueAt: x.dueAt ? String(x.dueAt) : undefined,
    status: String(x.status),
    createdAt: String(x.createdAt),
  })) : [];
  return {
    content,
    totalElements: Number(data.totalElements ?? content.length),
    totalPages: Number(data.totalPages ?? 1),
    number: Number(data.number ?? (params.page ?? 0)),
    size: Number(data.size ?? (params.size ?? content.length)),
  };
}

export default { createAssignment, listAssignments };

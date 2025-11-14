import http from './http';

export type CatalogModule = { id: string; name: string };
export type CatalogStage = { id: string; name: string };
export type CatalogSegment = { id: string; name: string; stages?: CatalogStage[] };

export type ModuleTreeNode = {
  id: string;
  name: string;
  segments?: CatalogSegment[]; // stages are nested under segments per backend
};

export async function getModuleTree(): Promise<ModuleTreeNode[]> {
  const { data } = await http.get('/api/v1/admin/modules/tree');
  const normalize = (n: any): ModuleTreeNode => ({
    id: String(n.id),
    name: String(n.name ?? n.title ?? 'Unnamed'),
    segments: Array.isArray(n.segments)
      ? n.segments.map((s: any) => ({
          id: String(s.id),
          name: String(s.name ?? ''),
          stages: Array.isArray(s.stages)
            ? s.stages.map((t: any) => ({ id: String(t.id), name: String(t.name ?? '') }))
            : [],
        }))
      : [],
  });
  return Array.isArray(data) ? data.map(normalize) : [];
}

// Modules
export async function createModule(payload: { name: string; description?: string; isActive?: boolean }): Promise<void> {
  const body: any = { ...payload };
  await http.post('/api/v1/admin/modules', body);
}
export async function updateModule(id: string, payload: { name: string; description?: string; isActive?: boolean }): Promise<void> {
  const body: any = { ...payload };
  await http.put(`/api/v1/admin/modules/${id}`, body);
}
export async function deleteModule(id: string): Promise<void> {
  await http.delete(`/api/v1/admin/modules/${id}`);
}

// Segments
export async function createSegment(payload: { name: string; moduleId: string; description?: string; isActive?: boolean }): Promise<void> {
  const body: any = { ...payload };
  if (body.moduleId != null && body.moduleId !== '') body.moduleId = Number(body.moduleId);
  await http.post('/api/v1/admin/segments', body);
}
export async function updateSegment(id: string, payload: { name: string; moduleId?: string; description?: string; isActive?: boolean }): Promise<void> {
  const body: any = { ...payload };
  if (body.moduleId != null && body.moduleId !== '') body.moduleId = Number(body.moduleId);
  await http.put(`/api/v1/admin/segments/${id}`, body);
}
export async function deleteSegment(id: string): Promise<void> {
  await http.delete(`/api/v1/admin/segments/${id}`);
}

// Stages
export type StageType = 'TRAINING' | 'ASSESSMENT' | 'CONSULTATION' | 'SUMMARY';
export async function createStage(payload: { name: string; segmentId: string; type: StageType; description?: string; contentUrl?: string; lmsCourseId?: string; aiPromptTemplate?: string; durationMinutes?: number; isActive?: boolean }): Promise<void> {
  const body: any = { ...payload };
  body.segmentId = Number(body.segmentId);
  await http.post('/api/v1/admin/stages', body);
}
export async function updateStage(id: string, payload: { name?: string; segmentId?: string; type?: StageType; description?: string; contentUrl?: string; lmsCourseId?: string; aiPromptTemplate?: string; durationMinutes?: number; isActive?: boolean }): Promise<void> {
  const body: any = { ...payload };
  if (body.segmentId != null && body.segmentId !== '') body.segmentId = Number(body.segmentId);
  await http.put(`/api/v1/admin/stages/${id}`, body);
}

// Server expects ReorderRequest with items: [{type, id, orderIndex}]
export async function reorderItems(items: Array<{ type: 'modules'|'segments'|'stages'; id: string; orderIndex: number }>): Promise<void> {
  const body = { items: items.map(i => ({ ...i, id: Number(i.id) })) } as any;
  await http.post('/api/v1/admin/reorder', body);
}
export async function deleteStage(id: string): Promise<void> {
  await http.delete(`/api/v1/admin/stages/${id}`);
}

export default {
  getModuleTree,
  createModule,
  updateModule,
  deleteModule,
  createSegment,
  updateSegment,
  deleteSegment,
  createStage,
  updateStage,
  deleteStage,
  reorderItems,
};

import api from './http';

export type TrainingModuleStatus = {
  name: string;
  externalId?: string;
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED';
  completion?: number;
};

export async function getTrainingStatus(): Promise<{ modules: TrainingModuleStatus[] }> {
  const res = await api.get('/api/v1/client/training/status');
  return res.data;
}

// Assigned tree structure mirrors backend ModuleTreeResponse
export type AssignedTree = Array<{
  name: string; // module name
  segments: Array<{
    name: string;
    stages: Array<{
      id?: number;                 // stageId if present from backend
      name: string;
      lmsCourseId?: string | null; // optional external course id
    }>;
  }>;
}>;

export async function getAssignedTree(): Promise<AssignedTree> {
  const res = await api.get('/api/v1/client/training/assigned');
  return res.data;
}

export type StageStartResponse = {
  launchUrl?: string | null;
};

export async function startStage(stageId: number): Promise<StageStartResponse> {
  const res = await api.post(`/api/v1/client/training/stage/${stageId}/start`);
  return res.data;
}

export async function completeStage(stageId: number, payload: { notes?: string } = {}): Promise<void> {
  await api.post(`/api/v1/client/training/stage/${stageId}/complete`, payload);
}

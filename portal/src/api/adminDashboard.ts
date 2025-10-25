import http from './http';

export type AdminSummary = {
  totalClients: number;
  activeClients: number;
  totalQuestionnaires: number;
  totalAssignments: number;
  pendingAssessments: number;
  completedAssessments: number;
};

const getSummary = async (): Promise<AdminSummary> => {
  const res = await http.get('/api/v1/admin/dashboard/summary');
  return res.data as AdminSummary;
};

export default { getSummary };

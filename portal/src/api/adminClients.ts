import http from './http';

export type Client = {
  id: string;
  firstName?: string;
  lastName?: string;
  email: string;
  role?: string;
  isActive?: boolean;
  isEmailVerified?: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type CreateClientResponse = {
  user: Client;
  temporaryPassword?: string;
};

const BASE = '/api/v1/admin/clients';
const SEARCH = `${BASE}/search`;

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page index
  size: number;
};

export type SearchParams = {
  query?: string;
  page?: number; // 0-based
  size?: number;
  sort?: string; // e.g. 'createdAt,desc'
};

export const listClients = async (): Promise<Client[]> => {
  const res = await http.get(BASE);
  const arr = Array.isArray(res.data) ? res.data : [];
  return arr.map((c: any) => ({
    id: String(c.id),
    firstName: c.firstName,
    lastName: c.lastName,
    email: c.email,
    role: c.role,
    isActive: !!(c.isActive ?? c.active),
    isEmailVerified: !!(c.isEmailVerified ?? c.emailVerified),
    createdAt: c.createdAt,
    updatedAt: c.updatedAt,
  }));
};

export const searchClients = async (params: SearchParams): Promise<Page<Client>> => {
  const res = await http.get(SEARCH, { params });
  const p = res.data;
  const content = (Array.isArray(p.content) ? p.content : []).map((c: any) => ({
    id: String(c.id),
    firstName: c.firstName,
    lastName: c.lastName,
    email: c.email,
    role: c.role,
    isActive: !!(c.isActive ?? c.active),
    isEmailVerified: !!(c.isEmailVerified ?? c.emailVerified),
    createdAt: c.createdAt,
    updatedAt: c.updatedAt,
  }));
  return {
    content,
    totalElements: p.totalElements ?? content.length,
    totalPages: p.totalPages ?? 1,
    number: p.number ?? (params.page || 0),
    size: p.size ?? (params.size || content.length),
  };
};

export const getClient = async (id: string): Promise<Client> => {
  const { data: c } = await http.get(`${BASE}/${id}`);
  return {
    id: String(c.id),
    firstName: c.firstName,
    lastName: c.lastName,
    email: c.email,
    role: c.role,
    isActive: !!(c.isActive ?? c.active),
    isEmailVerified: !!(c.isEmailVerified ?? c.emailVerified),
    createdAt: c.createdAt,
    updatedAt: c.updatedAt,
  };
};

export const createClient = async (payload: Partial<Client>): Promise<CreateClientResponse> => {
  const { data } = await http.post(BASE, payload);
  return {
    user: {
      id: String(data.user.id),
      firstName: data.user.firstName,
      lastName: data.user.lastName,
      email: data.user.email,
      role: data.user.role,
      isActive: !!(data.user.isActive ?? data.user.active),
      isEmailVerified: !!(data.user.isEmailVerified ?? data.user.emailVerified),
      createdAt: data.user.createdAt,
      updatedAt: data.user.updatedAt,
    },
    temporaryPassword: data.temporaryPassword,
  };
};

export const updateClient = async (id: string, payload: Partial<Client>): Promise<Client> => {
  // Align outgoing payload with backend field names
  const body: any = { ...payload };
  if (payload.isActive !== undefined) { body.active = payload.isActive; body.isActive = payload.isActive; }
  if (payload.isEmailVerified !== undefined) { body.emailVerified = payload.isEmailVerified; body.isEmailVerified = payload.isEmailVerified; }
  delete body.isActive;
  delete body.isEmailVerified;
  const { data: c } = await http.put(`${BASE}/${id}`, body);
  return {
    id: String(c.id),
    firstName: c.firstName,
    lastName: c.lastName,
    email: c.email,
    role: c.role,
    isActive: !!(c.isActive ?? c.active),
    isEmailVerified: !!(c.isEmailVerified ?? c.emailVerified),
    createdAt: c.createdAt,
    updatedAt: c.updatedAt,
  };
};

export const deleteClient = async (id: string): Promise<void> => {
  await http.delete(`${BASE}/${id}`);
};

export const resendInvite = async (id: string): Promise<void> => {
  await http.post(`${BASE}/${id}/resend-invite`, {});
};

export default {
  listClients,
  searchClients,
  getClient,
  createClient,
  updateClient,
  deleteClient,
  resendInvite,
};

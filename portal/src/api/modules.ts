import http from './http';

export type ModuleNode = {
  id: string;
  name: string;
  // optional nested structure; depends on backend response shape
  segments?: Array<{ id: string; name: string }>;
  stages?: Array<{ id: string; name: string }>;
  children?: ModuleNode[];
};

export async function getModuleTree(): Promise<ModuleNode[]> {
  const res = await http.get('/api/v1/admin/modules/tree');
  const data = Array.isArray(res.data) ? res.data : [];
  // perform a light normalization to strings
  const normalize = (node: any): ModuleNode => ({
    id: String(node.id),
    name: String(node.name ?? node.title ?? 'Unnamed'),
    segments: Array.isArray(node.segments) ? node.segments.map((s: any) => ({ id: String(s.id), name: String(s.name ?? '') })) : undefined,
    stages: Array.isArray(node.stages) ? node.stages.map((s: any) => ({ id: String(s.id), name: String(s.name ?? '') })) : undefined,
    children: Array.isArray(node.children) ? node.children.map((c: any) => normalize(c)) : undefined,
  });
  return data.map(normalize);
}

export function flattenModules(tree: ModuleNode[]): Array<{ id: string; name: string }> {
  const out: Array<{ id: string; name: string }> = [];
  const walk = (n: ModuleNode) => {
    out.push({ id: n.id, name: n.name });
    (n.children || []).forEach(walk);
  };
  tree.forEach(walk);
  return out;
}

export function flattenStages(tree: ModuleNode[]): Array<{ id: string; name: string }> {
  const out: Array<{ id: string; name: string }> = [];
  const walk = (n: ModuleNode) => {
    if (Array.isArray(n.stages)) {
      n.stages.forEach((s) => out.push({ id: s.id, name: s.name }));
    }
    (n.children || []).forEach(walk);
  };
  tree.forEach(walk);
  return out;
}

export default { getModuleTree, flattenModules, flattenStages };

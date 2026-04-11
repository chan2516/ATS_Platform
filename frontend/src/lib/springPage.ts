import type { Page } from '../types/jobs'

/** Supports Spring Data {@code PagedModel} ({@code page} metadata) and legacy flat {@code Page} JSON. */
export function normalizeSpringPage<T>(raw: unknown): Page<T> {
  const o = raw as Record<string, unknown>
  if (o?.page && typeof o.page === 'object') {
    const p = o.page as Record<string, unknown>
    return {
      content: (o.content as T[]) ?? [],
      size: Number(p.size ?? 0),
      number: Number(p.number ?? 0),
      totalElements: Number(p.totalElements ?? 0),
      totalPages: Number(p.totalPages ?? 0),
    }
  }
  return {
    content: (o?.content as T[]) ?? [],
    size: Number(o?.size ?? 0),
    number: Number(o?.number ?? 0),
    totalElements: Number(o?.totalElements ?? 0),
    totalPages: Number(o?.totalPages ?? 0),
  }
}

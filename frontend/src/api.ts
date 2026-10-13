export type ShortLink = {
  shortCode: string
  shortUrl: string
  originalUrl: string
  createdAt: string
  expiresAt: string | null
}

export type ShortLinkListItem = ShortLink & {
  clickCount: number
}

export type ShortenRequest = {
  url: string
  expiresAt?: string
}

export type Stats = {
  shortCode: string
  shortUrl: string
  originalUrl: string
  expiresAt: string | null
  totalClicks: number
  // 30 UTC days ending today, oldest first, zero-count days included
  clicksByDay: { date: string; count: number }[]
  topReferrers: { referrer: string; count: number }[]
}

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init)
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(body?.error ?? `request failed with status ${response.status}`, response.status)
  }
  return body as T
}

export function shortenUrl(body: ShortenRequest): Promise<ShortLink> {
  return request('/api/shorten', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export function listUrls(): Promise<ShortLinkListItem[]> {
  return request('/api/urls')
}

export function getStats(code: string): Promise<Stats> {
  return request(`/api/stats/${encodeURIComponent(code)}`)
}

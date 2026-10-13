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

export class ApiError extends Error {}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init)
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(body?.error ?? `request failed with status ${response.status}`)
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

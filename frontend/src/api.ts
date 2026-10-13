export type ShortLink = {
  shortCode: string
  shortUrl: string
  originalUrl: string
  createdAt: string
  expiresAt: string | null
}

export type ShortenRequest = {
  url: string
  expiresAt?: string
}

export class ApiError extends Error {}

export async function shortenUrl(request: ShortenRequest): Promise<ShortLink> {
  const response = await fetch('/api/shorten', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(body?.error ?? `request failed with status ${response.status}`)
  }
  return body as ShortLink
}

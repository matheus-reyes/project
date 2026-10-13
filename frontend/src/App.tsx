import { type FormEvent, useState } from 'react'
import { ApiError, type ShortLink, shortenUrl } from './api'

function App() {
  const [url, setUrl] = useState('')
  const [expiresAt, setExpiresAt] = useState('')
  const [created, setCreated] = useState<ShortLink | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setCreated(null)
    try {
      // datetime-local has no zone: read it as local time and send it as UTC ISO
      const link = await shortenUrl({
        url,
        expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined,
      })
      setCreated(link)
      setUrl('')
      setExpiresAt('')
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'could not reach the server')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="page">
      <h1>URL Shortener</h1>
      <form className="shorten-form" onSubmit={handleSubmit}>
        <label>
          URL
          <input
            type="text"
            placeholder="https://example.com"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
          />
        </label>
        <label>
          Expires at (optional)
          <input
            type="datetime-local"
            value={expiresAt}
            onChange={(e) => setExpiresAt(e.target.value)}
          />
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Shortening…' : 'Shorten'}
        </button>
      </form>
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      {created && (
        <p className="result">
          Short URL:{' '}
          <a href={created.shortUrl} target="_blank" rel="noreferrer">
            {created.shortUrl}
          </a>
        </p>
      )}
    </main>
  )
}

export default App

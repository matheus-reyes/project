import { type FormEvent, useEffect, useState } from 'react'
import { ApiError, listUrls, type ShortLink, type ShortLinkListItem, shortenUrl } from './api'
import LinkTable from './LinkTable'

type LinkList = {
  items: ShortLinkListItem[]
  fetchedAt: number
}

function App() {
  const [url, setUrl] = useState('')
  const [expiresAt, setExpiresAt] = useState('')
  const [created, setCreated] = useState<ShortLink | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [links, setLinks] = useState<LinkList | null>(null)
  const [listError, setListError] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let ignore = false
    function load() {
      listUrls()
        .then((items) => {
          if (ignore) return
          setLinks({ items, fetchedAt: Date.now() })
          setListError(null)
        })
        .catch(() => {
          if (!ignore) setListError('could not load links')
        })
    }
    load()
    // Clicks happen in other tabs, so reload the counts when the user comes back
    window.addEventListener('focus', load)
    return () => {
      ignore = true
      window.removeEventListener('focus', load)
    }
  }, [refreshKey])

  function refresh() {
    setRefreshKey((key) => key + 1)
  }

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
      refresh()
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
      <section className="links">
        <div className="links-header">
          <h2>Links</h2>
          <button type="button" onClick={refresh}>
            Refresh
          </button>
        </div>
        {listError && (
          <p className="error" role="alert">
            {listError}
          </p>
        )}
        {links ? (
          <LinkTable links={links.items} fetchedAt={links.fetchedAt} />
        ) : (
          !listError && <p className="empty">Loading…</p>
        )}
      </section>
    </main>
  )
}

export default App

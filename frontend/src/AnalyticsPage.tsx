import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError, getStats, type Stats } from './api'
import ClicksChart from './ClicksChart'

// Keyed by code so a stale result never shows while another code is loading
type Result =
  | { code: string; status: 'ok'; stats: Stats; fetchedAt: number }
  | { code: string; status: 'not-found' }
  | { code: string; status: 'error'; message: string }

function AnalyticsPage() {
  const { code = '' } = useParams()
  const [result, setResult] = useState<Result | null>(null)

  useEffect(() => {
    let ignore = false
    getStats(code)
      .then((stats) => {
        if (!ignore) setResult({ code, status: 'ok', stats, fetchedAt: Date.now() })
      })
      .catch((e) => {
        if (ignore) return
        if (e instanceof ApiError && e.status === 404) {
          setResult({ code, status: 'not-found' })
        } else {
          setResult({ code, status: 'error', message: e instanceof ApiError ? e.message : 'could not reach the server' })
        }
      })
    return () => {
      ignore = true
    }
  }, [code])

  const current = result?.code === code ? result : null

  return (
    <main className="page">
      <p>
        <Link to="/">← All links</Link>
      </p>
      {current === null && <p className="empty">Loading…</p>}
      {current?.status === 'not-found' && (
        <>
          <h1>Link not found</h1>
          <p className="empty">
            There is no short link with the code <code>{code}</code>.
          </p>
        </>
      )}
      {current?.status === 'error' && (
        <p className="error" role="alert">
          {current.message}
        </p>
      )}
      {current?.status === 'ok' && <StatsView stats={current.stats} fetchedAt={current.fetchedAt} />}
    </main>
  )
}

function StatsView({ stats, fetchedAt }: { stats: Stats; fetchedAt: number }) {
  const expired = stats.expiresAt !== null && Date.parse(stats.expiresAt) <= fetchedAt

  return (
    <>
      <header className="analytics-header">
        <h1>
          <code>{stats.shortCode}</code>
          {expired && <span className="badge">expired</span>}
        </h1>
        <p className="original-url-full">
          <a href={stats.originalUrl} target="_blank" rel="noreferrer">
            {stats.originalUrl}
          </a>
        </p>
        <p className="empty">
          Short URL:{' '}
          <a href={stats.shortUrl} target="_blank" rel="noreferrer">
            {stats.shortUrl}
          </a>
        </p>
      </header>

      <section className="tile">
        <span className="tile-label">Total clicks</span>
        <span className="tile-value">{stats.totalClicks}</span>
      </section>

      <section className="card">
        <h2>Clicks per day (last 30 days)</h2>
        {stats.totalClicks === 0 ? (
          <p className="empty">No clicks yet. Share the short URL to start collecting data.</p>
        ) : (
          <ClicksChart days={stats.clicksByDay} />
        )}
      </section>

      <section className="card">
        <h2>Top referrers</h2>
        {stats.topReferrers.length === 0 ? (
          <p className="empty">No referrers yet.</p>
        ) : (
          <table className="link-table">
            <thead>
              <tr>
                <th>Referrer</th>
                <th className="numeric">Clicks</th>
              </tr>
            </thead>
            <tbody>
              {stats.topReferrers.map((row) => (
                <tr key={row.referrer}>
                  <td>{row.referrer}</td>
                  <td className="numeric">{row.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  )
}

export default AnalyticsPage

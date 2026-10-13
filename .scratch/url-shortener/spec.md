# URL Shortener: prototype spec

Status: ready-for-agent

Prototype built and tested in about 45 minutes. Domain terms follow `GLOSSARY.md`.

## Architecture

- Two processes:
  - `backend/`: Kotlin + Spring Boot on `:8080`. Serves `/api/*` and `GET /{code}`.
  - `frontend/`: React + TypeScript + Vite on `:5173`. Proxies `/api` to `:8080`, so no CORS setup is needed.
- `make dev` at the repo root starts both. `make test` runs every test.
- Persistence: in-memory repositories (`ConcurrentHashMap`) behind interfaces, so a database can replace them later.
- Time comes from an injected `java.time.Clock`, so expiration and per-day aggregation are deterministic in tests.
- `shortUrl` is built from the `app.base-url` property (default `http://localhost:8080`).

## API contract

| Method | Path | Success | Errors |
|---|---|---|---|
| POST | `/api/shorten` | 201 `ShortLinkResponse` | 400 invalid URL (not http/https), 400 `expiresAt` in the past |
| GET | `/{code}` | 302 to the Original URL, records a Click | 404 unknown, 410 expired (no Click recorded) |
| GET | `/api/urls` | 200 list, newest first, with `clickCount` | - |
| GET | `/api/stats/{code}` | 200 `StatsResponse` | 404 unknown |

```jsonc
// POST /api/shorten request
{ "url": "https://example.com", "expiresAt": "2026-12-31T23:59:59Z" } // expiresAt optional

// ShortLinkResponse (list items add "clickCount")
{ "shortCode": "aB3xK9q", "shortUrl": "http://localhost:8080/aB3xK9q",
  "originalUrl": "https://example.com", "createdAt": "...", "expiresAt": null }

// StatsResponse
{ "shortCode": "aB3xK9q", "totalClicks": 12,
  "clicksByDay": [{ "date": "2026-09-14", "count": 0 }, "... 30 entries, oldest first"],
  "topReferrers": [{ "referrer": "direct", "count": 7 }, { "referrer": "google.com", "count": 5 }] }

// Error body
{ "error": "url must use http or https" }
```

## Rules

- **Short Code:** 7 base62 characters from `SecureRandom`. On collision, generate a new one.
- **Duplicates:** every POST creates a new Short Link, even for a URL that was already shortened.
- **Redirect:** 302, not the 301 in the brief. Browsers cache a 301, so repeat visits would never reach the server and would not be counted. The README documents this.
- **Expired Link:**
  - the redirect returns 410 and records no Click;
  - the link stays in `/api/urls` and keeps its stats.
- **Click data:**
  - timestamp from the `Clock`;
  - `Referer` header reduced to its host, or `direct` when missing or unparseable;
  - `User-Agent` header;
  - `request.remoteAddr`. `X-Forwarded-For` is not trusted without a known proxy.
- **Stats:**
  - `clicksByDay` covers the last 30 UTC days including today, with zero-count days;
  - `topReferrers` lists the top 5 by count.
- **Analytics** is public to anyone who knows the Short Code.

## Out of scope

Authentication, link ownership, custom codes, a database, frontend tests, deployment.

## Slices

| # | Issue | Delivers |
|---|---|---|
| 01 | `issues/01-shorten-and-redirect.md` | Walking skeleton: create a link in the UI and follow it |
| 02 | `issues/02-click-tracking-and-list.md` | Record Clicks, list links with counts |
| 03 | `issues/03-analytics-page.md` | Stats endpoint and the visual Analytics page |
| 04 | `issues/04-seed-data-and-readme.md` | Mock data on startup and the README |

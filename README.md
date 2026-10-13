# URL Shortener

Shortens URLs, redirects visitors, and records every click so each link has an analytics page. The domain terms used here (Short Link, Short Code, Click, Referrer) are defined in [`GLOSSARY.md`](GLOSSARY.md).

## Prerequisites

- JDK 17. Gradle comes with the wrapper; no global install is needed.
- Node.js 20.19+ or 22.12+, with npm.
- Docker with Compose v2. `make dev` runs Postgres in a container, and the tests start their own with Testcontainers.
- `make`.

## Run

```sh
make install   # download backend dependencies and install frontend packages
make dev       # Postgres on :5432, API on http://localhost:8080, web app on http://localhost:5173
```

Open http://localhost:5173. `Ctrl+C` stops the API and the web app; Postgres keeps running, and its data lives in a Docker volume. `make db-stop` stops it, and `make db-reset` deletes the volume.

On startup Flyway applies the migrations in `backend/src/main/resources/db/migration`, then the backend loads demo data: three links, one of them expired, with about 190 clicks spread over the last 30 days. The seed only inserts links that are missing, so a restart adds nothing. The demo dates are relative to the first start; run `make db-reset` to bring them back to the last 30 days. Set `app.seed.enabled=false` in `backend/src/main/resources/application.properties` to start empty.

## Test and lint

```sh
make test   # backend unit, repository and MockMvc tests; needs Docker running
make lint   # ktlint for the backend, ESLint for the frontend
```

## Demo in three steps

1. Run `make dev` and open http://localhost:5173. The list shows the three seeded links with their click counts; `expired` carries the "expired" badge.
2. Click the `kotlin1` code. The Analytics page shows the total, 30 days of bars, and four referrers: `direct`, `google.com`, `twitter.com` and `linkedin.com`.
3. Shorten `https://example.com` on the Home page, then run:

   ```sh
   curl -i -H "Referer: https://news.ycombinator.com/" http://localhost:8080/<code>
   ```

   The response is a `302` to example.com. Back on the Home page the link shows one click, and its Analytics page lists `news.ycombinator.com`.

## API

| Method | Path | Success | Errors |
|---|---|---|---|
| POST | `/api/shorten` with `{ "url", "expiresAt"? }` | `201` with the Short Link | `400` for a URL that is not http/https, or an `expiresAt` in the past |
| GET | `/{code}` | `302` to the original URL; records a Click | `404` unknown code, `410` expired link (no Click recorded) |
| GET | `/api/urls` | `200`, newest first, each with `clickCount` | |
| GET | `/api/stats/{code}` | `200` with `totalClicks`, `clicksByDay` (30 UTC days) and `topReferrers` (top 5) | `404` unknown code |

Errors come back as `{ "error": "..." }`. The full contract is in [`.scratch/url-shortener/spec.md`](.scratch/url-shortener/spec.md).

## Stack and why

- **Backend: Kotlin + Spring Boot.** Spring Boot gives routing, JSON, validation and an integration-test harness (MockMvc) with no setup, which matters in a time-boxed build. Kotlin keeps the models short: data classes, null safety, and expression bodies.
- **Frontend: React + TypeScript + Vite.** Vite starts in under a second and proxies `/api` to the backend, so the browser sees one origin and no CORS setup is needed. TypeScript keeps the API types in step with the backend responses.
- **Postgres with plain SQL.** The data is two tables and a handful of queries, so `JdbcClient` with hand-written SQL is enough; JPA would add entity mapping and lazy-loading rules without removing any code. Flyway versions the schema.
- **No chart library.** The per-day chart is 30 CSS bars with a hover tooltip. A library would add weight for a single chart.

## Design decisions

### Storage behind interfaces

Short Links and Clicks live in Postgres, in the `short_links` and `clicks` tables, behind `ShortLinkRepository` and `ClickRepository`. The first version kept them in memory; the switch to Postgres replaced only those two classes, and the services did not change. The in-memory versions now live in the test sources, so the service unit tests run without a database. The JDBC repositories and the API tests run against a real Postgres started by Testcontainers.

A new code is stored with `INSERT ... ON CONFLICT DO NOTHING`, so two requests can never get the same code, even across instances. Time comes from an injected `java.time.Clock`, so expiry and per-day grouping are tested with a fixed clock.

### 302 instead of 301

The brief asks for a 301. A 301 is permanent, so browsers cache it and later visits go straight to the destination without reaching the server. Those visits would never be counted, which defeats the analytics. A 302 sends every visit through the server.

### Other rules

- Short Codes are 7 base62 characters from `SecureRandom`, regenerated on collision.
- Every POST creates a new Short Link, even for a URL that was already shortened.
- The Referrer is reduced to its host without `www.`; a missing or unparseable header counts as `direct`.
- Days are UTC calendar days.

## Known limits

- Stats load every Click of the link and group them in the service. That is fine for thousands of clicks per link; past that, the grouping belongs in SQL. The list also counts clicks with one query per link.
- No authentication: anyone with a Short Code can see its analytics.
- The client IP is `request.remoteAddr`. Behind a proxy that is the proxy's address; `X-Forwarded-For` is not trusted without a known proxy in front.
- No rate limiting, and no check of the destination URL against a blocklist.
- The frontend has no automated tests.

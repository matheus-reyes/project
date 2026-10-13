# 02: Click tracking and link list

Status: ready-for-agent
Blocked by: 01
Budget: ~10 min

## Goal

Every redirect records a Click, and the Home page lists all Short Links with their Click counts.

## Tasks

1. Add the `Click` model (`shortCode`, `timestamp`, `referrer`, `userAgent`, `ip`), a `ClickRepository` interface, and its in-memory implementation.
2. `GET /{code}` records a Click before returning the 302. It records nothing on 404 or 410.
   - The `Referer` header becomes its host, or `direct` when it is missing or unparseable.
   - The IP comes from `request.remoteAddr`.
3. `GET /api/urls` returns every Short Link, newest first, each with a `clickCount`. Expired links stay in the list.
4. Home page: below the form, a table with the code, Original URL, Click count, an "expired" badge when `expiresAt` has passed, and the link to open. It refreshes after a new link is created.

## Tests

- Unit:
  - a `Referer` URL becomes its host;
  - a blank or garbage `Referer` becomes `direct`.
- MockMvc:
  - a redirect with `Referer: https://google.com/search?q=x` records one Click with `google.com`, the user agent, and the IP;
  - a 410 or 404 records nothing;
  - `GET /api/urls` orders links by `createdAt` descending and reports the right `clickCount`.

## Acceptance (how to validate)

1. `make dev`, then create two links in the UI.
2. Open the first link three times. Refresh the Home page: it shows `3` for that link and `0` for the other, and the newest link is on top.
3. Create a link that expires in one minute, wait, and open it. The browser gets `410`, and the list shows the "expired" badge with the count unchanged.
4. `make test` passes.

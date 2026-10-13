# 03: Analytics page

Status: ready-for-agent
Blocked by: 02
Budget: ~12 min

## Goal

The creator of a Short Link opens `/analytics/{code}` and sees the total Clicks, Clicks per day over the last 30 days, and the top Referrers. They can share the page by its URL.

## Tasks

1. `GET /api/stats/{code}` returns `StatsResponse` with:
   - `totalClicks`;
   - `clicksByDay` for exactly 30 UTC days ending today, oldest first, including zero-count days;
   - `topReferrers`, the top 5 by count.

   It returns 404 when the code is unknown and works for Expired Links.
2. Add `react-router-dom` with the routes `/` and `/analytics/:code`. Each row of the Home table links to its Analytics page.
3. Analytics page:
   - a header with the code, the Original URL, and the "expired" badge when the link has expired;
   - a total Clicks tile;
   - a CSS bar chart of `clicksByDay` with a tooltip or label per bar;
   - a top Referrers table;
   - loading, not-found, and empty states.

## Tests

- Unit (fixed `Clock`):
  - Clicks on day 1, day 15, and day 31 ago: the day-31 Click falls outside `clicksByDay` but counts in `totalClicks`;
  - the result always has 30 entries, and the right ones are zero;
  - a Click at 23:59 UTC and one at 00:01 UTC land on different days;
  - `topReferrers` is sorted, capped at 5, and includes `direct`.
- MockMvc:
  - stats returns 200 with the expected shape;
  - stats returns 404 for an unknown code.

## Acceptance (how to validate)

1. `make dev`, create a link, and open it a few times. To add a Referrer, run `curl -H "Referer: https://google.com" localhost:8080/<code>` once.
2. Click the link's row on the Home page. The page shows the total, today's bar, and `direct` and `google.com` in the top Referrers.
3. Copy the URL into a new tab. The same page loads.
4. `/analytics/zzzzzzz` shows the not-found state.
5. `make test` passes.

# 04: Seed data and README

Status: ready-for-agent
Blocked by: 03
Budget: ~8 min

## Goal

A fresh `make dev` already shows realistic data on the Home and Analytics pages. The README lets a new person set up, run, test, and understand the choices.

## Tasks

1. Add a `SeedDataLoader` (an `ApplicationRunner`) gated by `app.seed.enabled`. The property defaults to `true` and is set to `false` in the test profile, so tests start empty.
   - It loads three Short Links, one of them expired.
   - It loads Clicks spread over the last 30 days with a mix of Referrers (`direct`, `google.com`, `twitter.com`, `linkedin.com`), deterministic for a fixed seed.
2. Write the `README.md`:
   - prerequisites (JDK 17, Node);
   - `make install`, `make dev`, `make test`;
   - the stack rationale;
   - why the data lives in memory, behind interfaces, for now;
   - the 302 vs 301 decision;
   - known limits;
   - a three-step demo script.

## Tests

- MockMvc with `app.seed.enabled=true`: after startup, `/api/urls` returns 3 links and one of them has Clicks.
- The existing tests still pass with the seed disabled.

## Acceptance (how to validate)

1. Run `make dev` from a fresh start. The Home page lists three links with counts, one marked expired.
2. Open the Analytics page of a seeded link. Bars spread across the 30 days, and four Referrers appear.
3. Follow the README demo script from a clean clone. It works without extra steps.
4. `make test` and `make lint` pass.

# 01: Shorten and redirect (walking skeleton)

Status: ready-for-agent
Blocked by: none
Budget: ~15 min

## Goal

A user creates a Short Link in the browser and is redirected when they open it. This slice runs through every layer: build, lint, tests, API, UI, and the one-command run.

## Tasks

1. Scaffold `backend/` with Spring Boot (web, validation, test), Gradle wrapper, `jvmToolchain(17)`, and the ktlint plugin.
2. Scaffold `frontend/` with Vite + React + TypeScript and ESLint with `eslint-plugin-react-hooks`. Proxy `/api` to `:8080`. Fill the TODOs in `frontend/CLAUDE.md` (npm, commands).
3. Add a root `Makefile` with these targets:
   - `install`;
   - `dev`, which runs both services and stops both on Ctrl+C;
   - `test`, which runs `./gradlew test`;
   - `lint`.
4. Provide a `Clock` bean, the `ShortLink` model, a `ShortLinkRepository` interface, and its in-memory implementation.
5. Add a `ShortCodeGenerator`: 7 base62 characters from `SecureRandom`, retrying on collision.
6. `POST /api/shorten`:
   - validates http/https, a non-blank host, and that `expiresAt` is in the future;
   - returns 201 with `ShortLinkResponse`;
   - returns 400 with `{ "error": ... }` on invalid input.
7. `GET /{code}` returns 302 with `Location`, 404 when the code is unknown, and 410 when the link has expired.
8. Home page: a form with the URL and an optional `expiresAt` (`datetime-local`, sent as UTC ISO). It shows the created `shortUrl` as a clickable link, or the API error.

## Tests

- Unit:
  - the generator produces 7 alphanumeric characters;
  - the service rejects `ftp://`, `javascript:` and malformed URLs;
  - the service rejects a past `expiresAt` (fixed `Clock`);
  - expiry is evaluated against the `Clock`.
- MockMvc:
  - POST returns 201 with the full body;
  - POST returns 400 for a bad URL and for a past `expiresAt`;
  - GET returns 302 with the `Location` header, 404 for an unknown code, and 410 for an expired link.

## Acceptance (how to validate)

1. `make install && make dev`, then open `http://localhost:5173`.
2. Shorten `https://example.com` and click the returned link. The browser lands on example.com.
3. Shorten `ftp://x.com`. The form shows the error.
4. `curl -i localhost:8080/zzzzzzz` returns `404`.
5. `make test` and `make lint` pass.

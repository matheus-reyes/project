# Project instructions

## Layout
- `backend/`: API service (Kotlin + Spring Boot). Rules in `backend/CLAUDE.md`
- `frontend/`: web app (React). Rules in `frontend/CLAUDE.md`
- The two are separate services with separate builds and rules. Do not apply one service's conventions to the other

## Working across services
- Run each service's commands from its own directory
- A change to the API contract touches both services: update the backend and the frontend client together
<!-- TODO: where the API contract lives (e.g. OpenAPI spec path) -->
<!-- TODO: how to run both locally (e.g. `docker compose up`) -->

## Shared rules
- Language: English for everything in the repo: code, comments, commit messages, docs, ADRs and PRDs
<!-- TODO: commit convention, branch naming -->

## Docs
- ADRs go in `docs/adr/`, PRDs in `docs/prd/`; naming and lifecycle rules are in `docs/README.md`
- New document: copy the folder's `0000-template.md` and use the next free number
- Never rewrite an accepted ADR; supersede it with a new one
- Before a change that contradicts an accepted ADR, stop and ask

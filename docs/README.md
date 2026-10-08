# Docs

Project decisions and requirements. Everything here is written in English.

- `adr/`: Architecture Decision Records. One file per decision.
- `prd/`: Product Requirements Documents. One file per feature or initiative.

## Naming

- ADR: `adr/NNNN-short-title.md`, numbered in sequence (`0001-use-spring-boot.md`)
- PRD: `prd/NNNN-short-title.md`, numbered in sequence (`0001-user-login.md`)
- Start a new document by copying the `0000-template.md` of its folder

## ADR lifecycle

- Status: `Proposed`, `Accepted`, `Rejected`, `Deprecated` or `Superseded by ADR-NNNN`
- Do not rewrite an accepted ADR. To change a decision, write a new ADR and mark the old one `Superseded by ADR-NNNN`

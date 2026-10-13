# Frontend instructions

## Stack
- React web app; run every command below from `frontend/`
- Language: TypeScript
- Build tool: Vite (dev server on `:5173`, proxies `/api` to the backend on `:8080`)
- Package manager: npm
- Node: 20.19+ or 22.12+ (required by Vite)

## Commands
- Install: `npm ci`
- Dev server: `npm run dev`
- Build (type-checks first): `npm run build`
- Lint: `npm run lint`
- Tests: none for now; frontend tests are out of scope for the prototype

## Linting
- ESLint must include `eslint-plugin-react-hooks` with its recommended config; it enforces the Rules of Hooks and checks effect dependencies
- Run the lint script before finishing any change and fix every violation; do not disable a react-hooks rule without a reason in a comment
- Keep `<StrictMode>` around the app root; it renders components twice in development to expose impure code

## Rules of React
<!-- Condensed from the official Rules of React: https://react.dev/reference/rules -->
<!-- These are rules, not guidelines: breaking them causes bugs -->

### Components and Hooks must be pure
- Components are idempotent: the same props, state and context always produce the same output
- No side effects during render; put them in event handlers or `useEffect`. React may render a component more than once
- Props and state are immutable snapshots for a render; never mutate them, update state through its setter
- Values passed to a Hook, and values a Hook returns, are immutable; do not modify them
- Do not mutate a value after using it in JSX; do the mutation before the JSX is created

### React calls Components and Hooks
- Use components only in JSX (`<Profile />`); never call a component as a regular function
- Never pass Hooks around as regular values; call them only inside components or custom Hooks

### Rules of Hooks
- Call Hooks only at the top level: not inside loops, conditions or nested functions, and always before any early return
- Call Hooks only from React function components or custom Hooks; never from regular JavaScript functions

## Conventions
- Call the backend only through `src/api.ts`, with relative `/api/...` paths; the API returns errors as `{ "error": "..." }`
- Local component state with `useState`; no state library
- Plain CSS in `src/index.css`; no CSS framework
<!-- TODO: component and hook patterns as the app grows -->

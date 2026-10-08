# Frontend instructions

## Stack
- React web app; run every command below from `frontend/`
<!-- TODO: language (TypeScript?), build tool (Vite?), package manager (npm/pnpm/yarn), Node version -->

## Commands
<!-- TODO: install, dev server, build, test all, test one file, lint, format -->

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
<!-- TODO: component and hook patterns, state management, styling, how to call the backend API -->

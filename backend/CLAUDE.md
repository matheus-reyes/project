# Backend instructions

## Stack
- Kotlin + Spring Boot service; run every command below from `backend/`
- Language: Kotlin
- JDK: 17 (set `kotlin { jvmToolchain(17) }` in `build.gradle.kts` so Java and Kotlin share one target)
- Build: Gradle through the wrapper (`./gradlew`); do not assume a global Gradle install

## Commands
- Build: `./gradlew build`
- Test all: `./gradlew test`
- Test one class: `./gradlew test --tests "<fully.qualified.ClassName>"`
- Lint: `./gradlew ktlintCheck` (ktlint through the `org.jlleitschuh.gradle.ktlint` plugin; do not assume a global ktlint install)
- Auto-fix lint: `./gradlew ktlintFormat`
- Run `./gradlew ktlintCheck` before finishing any Kotlin change and fix every violation; do not suppress rules without a reason in a comment

## Kotlin conventions
<!-- Condensed from the official Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html -->

### Formatting
- 4 spaces, no tabs
- Trailing commas at declaration sites (parameters, enum entries, multiline constructors)
- Omit semicolons, redundant `Unit` return types and redundant modifiers

### Language use
- `val` by default; `var` only when the value changes
- Declare collections as immutable interfaces (`List`, `Set`, `Map`) and create them with `listOf`, `setOf`, `mapOf`
- Single-expression functions use an expression body
- Use `if`, `when` and `try` as expressions when they produce a value
- `if` for binary conditions; `when` for three or more branches
- Default parameter values instead of overloads
- Named arguments for Boolean parameters and for parameters of the same primitive type
- Higher-order functions (`filter`, `map`) instead of loops; a plain `for` instead of `forEach`
- Values coming from Java (platform types): declare the Kotlin type explicitly on public functions and on properties

### Naming and files
- Classes and objects in UpperCamelCase; functions, properties and locals in lowerCamelCase
- `const val` and deeply immutable constants in SCREAMING_SNAKE_CASE
- Avoid vague names such as `Manager`, `Util` and `Wrapper`
- A file with one class is named after it (`Foo.kt`); with several declarations, use a descriptive name
- Test method names may use backticks to describe behavior (tests only)
